package com.nicked.nick;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.User;
import com.nicked.NickedPlugin;
import com.nicked.api.NickInfo;
import com.nicked.api.NickedAPI;
import com.nicked.api.event.NickApplyEvent;
import com.nicked.api.event.NickChangeEvent;
import com.nicked.api.event.NickRandomSelectEvent;
import com.nicked.api.event.NickRemoveEvent;
import com.nicked.api.event.NickResolveEvent;
import com.nicked.config.NickedConfig;
import com.nicked.packet.PlayerRefresher;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Central orchestrator for all nick operations.
 * Implements {@link NickedAPI} so it can be exposed directly as the public API.
 */
public final class NickManager implements NickedAPI {

    private static final Pattern VALID_NAME = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");
    private static final Random RANDOM = new Random();

    private final NickedPlugin plugin;
    private final NickedConfig config;
    private final SkinFetcher skinFetcher;
    private final NickStorage storage;
    private final PlayerRefresher refresher;

    private final Map<UUID, NickData> activeNicks = new ConcurrentHashMap<>();
    private final Map<UUID, NickData> pendingRestores = new ConcurrentHashMap<>();

    private GameProfileModifier gameProfileModifier;

    public NickManager(
            NickedPlugin plugin,
            NickedConfig config,
            SkinFetcher skinFetcher,
            NickStorage storage,
            PlayerRefresher refresher
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.config = Objects.requireNonNull(config, "config");
        this.skinFetcher = Objects.requireNonNull(skinFetcher, "skinFetcher");
        this.storage = Objects.requireNonNull(storage, "storage");
        this.refresher = Objects.requireNonNull(refresher, "refresher");
    }

    /** Loads persisted nicks into memory (called once on startup). */
    public void loadPersisted() {
        if (!config.isPersistAcrossRestarts()) {
            return;
        }
        Map<UUID, NickData> loaded = storage.loadAll();
        pendingRestores.putAll(loaded);
        plugin.getLogger().info("Loaded " + loaded.size() + " persisted nick(s) from storage.");
    }

    /**
     * Called when a player joins: applies any pending restored nicks.
     * Deferred by 5 ticks to ensure the player's connection is fully initialised
     * and PacketEvents has registered their {@link com.github.retrooper.packetevents.protocol.player.User}.
     */
    public void onPlayerJoin(Player player) {
        Objects.requireNonNull(player, "player");

        NickData restored = pendingRestores.remove(player.getUniqueId());
        if (restored == null && config.isPersistAcrossLogout()) {
            restored = activeNicks.get(player.getUniqueId());
        }
        if (restored == null) {
            return;
        }

        NickData forRestore = restored;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            if (forRestore.nickedSkin() != null) {
                applyNickWithSkin(player, forRestore.nickedName(), forRestore.nickedSkin(),
                        forRestore.originalSkin(), NickCause.RESTART_RESTORE);
            } else {
                skinFetcher.fetchSkin(forRestore.nickedName(), skinOpt ->
                        applyNickWithSkin(player, forRestore.nickedName(),
                                skinOpt.orElse(null), forRestore.originalSkin(),
                                NickCause.RESTART_RESTORE));
            }
        }, 5L);
    }

    /** Called when a player quits. Clears or retains nick data per config. */
    public void onPlayerQuit(Player player) {
        Objects.requireNonNull(player, "player");
        if (!config.isPersistAcrossLogout()) {
            activeNicks.remove(player.getUniqueId());
        }
    }

    // -------------------------------------------------------------------------
    // NickedAPI implementation
    // -------------------------------------------------------------------------

    @Override
    public String getDisplayName(UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        NickData data = activeNicks.get(uuid);
        String resolved = data != null ? data.nickedName() : resolveRealName(uuid);

        NickResolveEvent event = new NickResolveEvent(uuid, resolveRealName(uuid), resolved);
        Bukkit.getPluginManager().callEvent(event);
        return event.getResolvedName();
    }

    @Override
    public boolean isNicked(UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        return activeNicks.containsKey(uuid);
    }

    @Override
    public Optional<NickInfo> getNickInfo(UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        NickData data = activeNicks.get(uuid);
        if (data == null) {
            return Optional.empty();
        }
        return Optional.of(new NickInfo(
                data.realUUID(), data.realName(), data.nickedName(),
                data.nickedSkin(), data.cause(), data.timestamp()));
    }

    @Override
    public void nickPlayer(UUID uuid, String newName, NickCause cause) {
        Objects.requireNonNull(uuid, "uuid");
        Objects.requireNonNull(newName, "newName");
        Objects.requireNonNull(cause, "cause");

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            plugin.getLogger().log(Level.WARNING,
                    "nickPlayer called for offline player {0}; ignoring.", uuid);
            return;
        }

        NickData existing = activeNicks.get(uuid);
        String oldNick = existing != null ? existing.nickedName() : null;

        NickChangeEvent changeEvent = new NickChangeEvent(player, oldNick, newName, cause);
        Bukkit.getPluginManager().callEvent(changeEvent);
        if (changeEvent.isCancelled()) {
            return;
        }

        SkinData originalSkin = existing != null
                ? existing.originalSkin()
                : captureCurrentSkin(player);

        // Store a placeholder immediately so isNicked() returns true synchronously
        // and chat already shows the nick name before skin loads.
        NickData placeholder = new NickData(
                uuid, player.getName(), newName,
                originalSkin, null, cause, System.currentTimeMillis());
        activeNicks.put(uuid, placeholder);
        setBukkitDisplayName(player, newName);

        // Defer the full visual refresh until the skin is ready — prevents a brief
        // flash of the real skin that would occur from two separate refresh calls.
        skinFetcher.fetchSkin(newName, skinOpt -> {
            NickData current = activeNicks.get(uuid);
            if (current == null || !current.nickedName().equals(newName)) {
                return;
            }

            SkinData skin = skinOpt.orElse(null);
            if (skin == null) {
                plugin.getLogger().log(Level.WARNING,
                        "Could not fetch skin for nick ''{0}''; nick active without skin change.", newName);
                if (player.isOnline()) {
                    plugin.getMessagesConfig().send(player, "skin_fetch_failed", "nick", newName);
                }
            }

            NickData withSkin = new NickData(
                    current.realUUID(), current.realName(), current.nickedName(),
                    current.originalSkin(), skin, current.cause(), current.timestamp());
            activeNicks.put(uuid, withSkin);

            if (config.isPersistAcrossRestarts() || config.isPersistAcrossLogout()) {
                storage.save(withSkin);
            }
            if (gameProfileModifier != null) {
                gameProfileModifier.applyNick(player, newName, skin);
            }
            refresher.refresh(player, skin, newName);

            NickApplyEvent applyEvent = new NickApplyEvent(
                    player,
                    new NickInfo(withSkin.realUUID(), withSkin.realName(), withSkin.nickedName(),
                            withSkin.nickedSkin(), withSkin.cause(), withSkin.timestamp()),
                    cause);
            Bukkit.getPluginManager().callEvent(applyEvent);
        });
    }

    @Override
    public void unnickPlayer(UUID uuid, NickCause cause) {
        Objects.requireNonNull(uuid, "uuid");
        Objects.requireNonNull(cause, "cause");

        NickData data = activeNicks.get(uuid);
        if (data == null) {
            return;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            activeNicks.remove(uuid);
            if (config.isPersistAcrossRestarts()) {
                storage.remove(uuid);
            }
            return;
        }

        NickRemoveEvent removeEvent = new NickRemoveEvent(player, data.nickedName(), cause);
        Bukkit.getPluginManager().callEvent(removeEvent);
        if (removeEvent.isCancelled()) {
            return;
        }

        activeNicks.remove(uuid);
        if (config.isPersistAcrossRestarts()) {
            storage.remove(uuid);
        }

        restoreBukkitDisplayName(player);

        if (gameProfileModifier != null) {
            gameProfileModifier.restoreNick(player);
        }
        refresher.refresh(player, data.originalSkin(), data.realName());
    }

    @Override
    public String getRealName(UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        return resolveRealName(uuid);
    }

    // -------------------------------------------------------------------------
    // Extra helpers exposed to commands
    // -------------------------------------------------------------------------

    /**
     * Nicks the player with a randomly selected name, firing {@link NickRandomSelectEvent}.
     */
    public void nickRandom(Player player, NickCause cause) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(cause, "cause");

        String selected = pickRandomName(player);
        if (selected == null) {
            return;
        }

        NickRandomSelectEvent selectEvent = new NickRandomSelectEvent(player, selected, cause);
        Bukkit.getPluginManager().callEvent(selectEvent);
        if (selectEvent.isCancelled()) {
            return;
        }

        nickPlayer(player.getUniqueId(), selectEvent.getSelectedName(), cause);
    }

    /** Randomly nicks every online player. */
    public void nickAll(NickCause cause) {
        Objects.requireNonNull(cause, "cause");
        for (Player p : Bukkit.getOnlinePlayers()) {
            nickRandom(p, cause);
        }
    }

    /** Returns true if the name is a valid Minecraft username. */
    public boolean isValidName(String name) {
        return name != null && VALID_NAME.matcher(name).matches();
    }

    /** Injects the optional GameProfileModifier. Call this after construction if enabled. */
    public void setGameProfileModifier(GameProfileModifier modifier) {
        this.gameProfileModifier = modifier;
    }

    /** Returns the plugin configuration. */
    public NickedConfig getNickedConfig() {
        return config;
    }

    /**
     * Returns an unmodifiable <em>live</em> view of the active nick map.
     * Because it is backed by the internal {@link java.util.concurrent.ConcurrentHashMap},
     * changes (nicks applied or removed after this call) are immediately visible
     * through the returned map. Callers that need a stable snapshot should copy
     * the entries themselves.
     */
    public Map<UUID, NickData> getActiveNicks() {
        return Collections.unmodifiableMap(activeNicks);
    }

    /**
     * Saves all in-memory nicks synchronously and clears state.
     * Called on plugin disable.
     */
    public void saveAndClear() {
        if (config.isPersistAcrossRestarts()) {
            storage.saveAllSync(activeNicks);
        }
        activeNicks.clear();
        pendingRestores.clear();
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Applies a fully-resolved nick (with known skin) and triggers the visual refresh.
     * Used by the restore-on-join path where skin data is already available.
     */
    private void applyNickWithSkin(
            Player player,
            String nickedName,
            SkinData nickedSkin,
            SkinData originalSkin,
            NickCause cause
    ) {
        NickData data = new NickData(
                player.getUniqueId(), player.getName(), nickedName,
                originalSkin, nickedSkin, cause, System.currentTimeMillis());

        activeNicks.put(player.getUniqueId(), data);
        setBukkitDisplayName(player, nickedName);

        if (config.isPersistAcrossRestarts() || config.isPersistAcrossLogout()) {
            storage.save(data);
        }
        if (gameProfileModifier != null) {
            gameProfileModifier.applyNick(player, nickedName, nickedSkin);
        }

        refresher.refresh(player, nickedSkin, nickedName);

        NickApplyEvent applyEvent = new NickApplyEvent(
                player,
                new NickInfo(data.realUUID(), data.realName(), data.nickedName(),
                        data.nickedSkin(), data.cause(), data.timestamp()),
                cause);
        Bukkit.getPluginManager().callEvent(applyEvent);
    }

    private String pickRandomName(Player player) {
        // Always merge configured pool + currently online player names (deduped).
        Set<String> poolSet = new LinkedHashSet<>(config.getRandomNickPool());
        Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(n -> !n.equalsIgnoreCase(player.getName()))
                .forEach(poolSet::add);

        if (poolSet.isEmpty()) {
            plugin.getLogger().warning("No names in random nick pool and no other players online.");
            return null;
        }

        List<String> pool = new ArrayList<>(poolSet);
        return pool.get(RANDOM.nextInt(pool.size()));
    }

    private String resolveRealName(UUID uuid) {
        NickData data = activeNicks.get(uuid);
        if (data != null) {
            return data.realName();
        }
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) {
            return online.getName();
        }
        String offlineName = Bukkit.getOfflinePlayer(uuid).getName();
        return offlineName != null ? offlineName : uuid.toString();
    }

    /**
     * Captures the player's current skin from the PacketEvents UserProfile.
     * Returns null if the skin cannot be determined.
     */
    private SkinData captureCurrentSkin(Player player) {
        try {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
            if (user == null || user.getProfile() == null) {
                return null;
            }
            List<TextureProperty> props = user.getProfile().getTextureProperties();
            if (props == null || props.isEmpty()) {
                return null;
            }
            for (TextureProperty prop : props) {
                if ("textures".equals(prop.getName()) && prop.getValue() != null) {
                    String sig = prop.getSignature();
                    return new SkinData(prop.getValue(), sig != null ? sig : "");
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.FINE, "Could not capture original skin for " + player.getName(), e);
        }
        return null;
    }

    /** Sets the Bukkit display name and player list name to the given nick. */
    private void setBukkitDisplayName(Player player, String nick) {
        player.setDisplayName(nick);
        player.setPlayerListName(nick);
    }

    /** Resets the Bukkit display name to the player's real name. */
    private void restoreBukkitDisplayName(Player player) {
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());
    }
}
