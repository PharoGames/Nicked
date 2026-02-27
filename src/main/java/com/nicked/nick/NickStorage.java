package com.nicked.nick;

import com.nicked.NickedPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Handles reading and writing nick data to {@code plugins/Nicked/nicks.yml}.
 *
 * <p>All disk writes are dispatched asynchronously. Reads (on startup) happen
 * synchronously since the server is still loading at that point.</p>
 */
public final class NickStorage {

    private final NickedPlugin plugin;
    private final File storageFile;

    public NickStorage(NickedPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.storageFile = new File(plugin.getDataFolder(), "nicks.yml");
    }

    /**
     * Saves a nick entry to disk asynchronously.
     *
     * @param data the nick data to persist
     */
    public void save(NickData data) {
        Objects.requireNonNull(data, "data");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            YamlConfiguration cfg = loadFile();
            writeSingleEntry(cfg, data);
            writeFile(cfg);
        });
    }

    /**
     * Removes a persisted nick entry from disk asynchronously.
     *
     * @param uuid the real UUID of the player to clear
     */
    public void remove(UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            YamlConfiguration cfg = loadFile();
            cfg.set(uuid.toString(), null);
            writeFile(cfg);
        });
    }

    /**
     * Saves all given nick entries synchronously. Used on plugin disable to ensure
     * data is written before the JVM shuts down scheduler tasks.
     *
     * @param nicks the map of UUID to NickData to persist
     */
    public void saveAllSync(Map<UUID, NickData> nicks) {
        Objects.requireNonNull(nicks, "nicks");
        if (nicks.isEmpty()) {
            return;
        }
        YamlConfiguration cfg = loadFile();
        for (Map.Entry<UUID, NickData> entry : nicks.entrySet()) {
            writeSingleEntry(cfg, entry.getValue());
        }
        writeFile(cfg);
    }

    private void writeSingleEntry(YamlConfiguration cfg, NickData data) {
        String key = data.realUUID().toString();
        cfg.set(key + ".real_name", data.realName());
        cfg.set(key + ".nicked_name", data.nickedName());
        cfg.set(key + ".cause", data.cause().name());
        cfg.set(key + ".timestamp", data.timestamp());
        if (data.nickedSkin() != null) {
            cfg.set(key + ".skin_value", data.nickedSkin().value());
            cfg.set(key + ".skin_signature", data.nickedSkin().signature());
        }
        if (data.originalSkin() != null) {
            cfg.set(key + ".original_skin_value", data.originalSkin().value());
            cfg.set(key + ".original_skin_signature", data.originalSkin().signature());
        }
    }

    /**
     * Loads all persisted nick entries. Called synchronously on startup.
     *
     * @return an unmodifiable map of real UUID to nick data
     */
    public Map<UUID, NickData> loadAll() {
        if (!storageFile.exists()) {
            return Collections.emptyMap();
        }
        YamlConfiguration cfg = loadFile();
        Map<UUID, NickData> result = new HashMap<>();

        for (String uuidStr : cfg.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                String realName = cfg.getString(uuidStr + ".real_name");
                String nickedName = cfg.getString(uuidStr + ".nicked_name");
                String causeName = cfg.getString(uuidStr + ".cause", NickCause.RESTART_RESTORE.name());
                long timestamp = cfg.getLong(uuidStr + ".timestamp", System.currentTimeMillis());

                if (realName == null || nickedName == null) {
                    plugin.getLogger().log(Level.WARNING,
                            "Skipping malformed nick entry for UUID {0}: missing real_name or nicked_name", uuidStr);
                    continue;
                }

                SkinData nickedSkin = readSkinData(cfg, uuidStr, "skin_value", "skin_signature");
                SkinData originalSkin = readSkinData(cfg, uuidStr, "original_skin_value", "original_skin_signature");

                NickCause cause;
                try {
                    cause = NickCause.valueOf(causeName);
                } catch (IllegalArgumentException e) {
                    cause = NickCause.RESTART_RESTORE;
                }

                result.put(uuid, new NickData(uuid, realName, nickedName, originalSkin, nickedSkin, cause, timestamp));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.WARNING,
                        "Skipping nick entry with invalid UUID key: " + uuidStr, e);
            }
        }

        return Collections.unmodifiableMap(result);
    }

    private SkinData readSkinData(YamlConfiguration cfg, String uuidStr, String valueKey, String sigKey) {
        String value = cfg.getString(uuidStr + "." + valueKey);
        String signature = cfg.getString(uuidStr + "." + sigKey);
        if (value != null && signature != null) {
            return new SkinData(value, signature);
        }
        return null;
    }

    private YamlConfiguration loadFile() {
        if (!storageFile.exists()) {
            return new YamlConfiguration();
        }
        return YamlConfiguration.loadConfiguration(storageFile);
    }

    private void writeFile(YamlConfiguration cfg) {
        try {
            plugin.getDataFolder().mkdirs();
            cfg.save(storageFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to write nicks.yml", e);
        }
    }
}
