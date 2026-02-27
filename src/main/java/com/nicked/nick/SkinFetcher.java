package com.nicked.nick;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nicked.NickedPlugin;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Fetches and caches Minecraft player skin data from the Mojang API.
 *
 * <p>All HTTP calls are dispatched asynchronously via the Bukkit scheduler.
 * Callbacks are always invoked on the main thread.</p>
 *
 * <p>The cache uses the nick name (lower-case) as the key and respects a
 * configurable TTL to avoid hammering the Mojang rate limit
 * (600 requests / 10 min).</p>
 */
public final class SkinFetcher {

    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final NickedPlugin plugin;
    private final long cacheTtlMs;
    private final Map<String, CachedSkin> cache = new ConcurrentHashMap<>();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();

    public SkinFetcher(NickedPlugin plugin, int cacheTtlMinutes) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        if (cacheTtlMinutes <= 0) {
            throw new IllegalArgumentException("cacheTtlMinutes must be positive, got: " + cacheTtlMinutes);
        }
        this.cacheTtlMs = (long) cacheTtlMinutes * 60 * 1_000;
    }

    /**
     * Fetches skin data for the given username asynchronously.
     * The callback is always invoked on the main server thread.
     *
     * @param username the Minecraft username to look up
     * @param callback receives {@code Optional.empty()} on failure
     */
    public void fetchSkin(String username, Consumer<Optional<SkinData>> callback) {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(callback, "callback");

        String key = username.toLowerCase();
        CachedSkin cached = cache.get(key);
        if (cached != null && !cached.isExpired(cacheTtlMs)) {
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(Optional.of(cached.data())));
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Optional<SkinData> result = doFetch(username);
            result.ifPresent(skin -> cache.put(key, new CachedSkin(skin, System.currentTimeMillis())));
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(result));
        });
    }

    private Optional<SkinData> doFetch(String username) {
        try {
            String uuid = fetchUUID(username);
            if (uuid == null) {
                plugin.getLogger().log(Level.WARNING,
                        "Skin fetch: could not resolve UUID for username ''{0}''", username);
                return Optional.empty();
            }
            return fetchTextures(uuid, username);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            plugin.getLogger().log(Level.WARNING,
                    "Skin fetch: I/O error fetching skin for ''" + username + "''", e);
            return Optional.empty();
        }
    }

    private String fetchUUID(String username) throws IOException, InterruptedException {
        JsonObject response = getJson(UUID_URL + username);
        if (response == null || !response.has("id")) {
            return null;
        }
        return response.get("id").getAsString();
    }

    private Optional<SkinData> fetchTextures(String uuid, String username)
            throws IOException, InterruptedException {
        JsonObject profile = getJson(PROFILE_URL + uuid + "?unsigned=false");
        if (profile == null || !profile.has("properties")) {
            plugin.getLogger().log(Level.WARNING,
                    "Skin fetch: no properties in profile response for ''{0}''", username);
            return Optional.empty();
        }

        for (var element : profile.getAsJsonArray("properties")) {
            JsonObject prop = element.getAsJsonObject();
            if ("textures".equals(prop.get("name").getAsString())) {
                String value = prop.get("value").getAsString();
                String signature = prop.has("signature") ? prop.get("signature").getAsString() : "";
                return Optional.of(new SkinData(value, signature));
            }
        }

        plugin.getLogger().log(Level.WARNING,
                "Skin fetch: no textures property found for ''{0}''", username);
        return Optional.empty();
    }

    /**
     * Performs a synchronous GET request and returns the parsed JSON object,
     * or {@code null} for 204/404 responses.
     */
    private JsonObject getJson(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();

        if (status == 204 || status == 404) {
            return null;
        }
        if (status != 200) {
            throw new IOException("Unexpected HTTP status " + status + " for URL: " + url);
        }

        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    private record CachedSkin(SkinData data, long fetchedAtMs) {
        boolean isExpired(long ttlMs) {
            return System.currentTimeMillis() - fetchedAtMs > ttlMs;
        }
    }
}
