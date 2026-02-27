package com.nicked.config;

import com.nicked.NickedPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Loads and validates config.yml with fail-fast behaviour.
 * If any critical value is invalid the plugin is disabled during construction.
 */
public final class NickedConfig {

    private final NickedPlugin plugin;

    private final boolean internalNameChangeEnabled;
    private final boolean persistAcrossRestarts;
    private final boolean persistAcrossLogout;
    private final boolean skinChangeForSelf;
    private final boolean strictNameValidation;
    private final int skinCacheTtlMinutes;
    private final List<String> randomNickPool;

    public NickedConfig(NickedPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");

        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration cfg = plugin.getConfig();

        this.internalNameChangeEnabled = cfg.getBoolean("internal_name_change.enabled", false);
        this.persistAcrossRestarts = cfg.getBoolean("persist_across_restarts", false);
        this.persistAcrossLogout = cfg.getBoolean("persist_across_logout", false);
        this.skinChangeForSelf = cfg.getBoolean("skin_change_for_self", true);
        this.strictNameValidation = cfg.getBoolean("strict_name_validation", true);

        int ttl = cfg.getInt("skin_cache_ttl_minutes", 30);
        if (ttl <= 0) {
            fail("skin_cache_ttl_minutes must be greater than 0, got: " + ttl);
            ttl = 30;
        }
        this.skinCacheTtlMinutes = ttl;

        if (!cfg.isList("random_nick_pool") && cfg.get("random_nick_pool") != null) {
            fail("random_nick_pool must be a list in config.yml");
            this.randomNickPool = Collections.emptyList();
        } else {
            this.randomNickPool = Collections.unmodifiableList(cfg.getStringList("random_nick_pool"));
        }
    }

    private void fail(String message) {
        plugin.getLogger().log(Level.SEVERE, "Configuration error: " + message);
        plugin.getServer().getPluginManager().disablePlugin(plugin);
    }

    /**
     * Whether the dangerous server-side {@code GameProfile} name replacement is active.
     * Controlled by {@code internal_name_change.enabled} in config.yml. Default: {@code false}.
     */
    public boolean isInternalNameChangeEnabled() {
        return internalNameChangeEnabled;
    }

    /**
     * Whether nicks are written to {@code nicks.yml} and restored after a server restart.
     * Controlled by {@code persist_across_restarts} in config.yml. Default: {@code false}.
     */
    public boolean isPersistAcrossRestarts() {
        return persistAcrossRestarts;
    }

    /**
     * Whether a nick is retained in memory when a player disconnects and re-applied on
     * their next login, without requiring a server restart.
     * Controlled by {@code persist_across_logout} in config.yml. Default: {@code false}.
     */
    public boolean isPersistAcrossLogout() {
        return persistAcrossLogout;
    }

    /**
     * Whether to send a {@code PLAYER_INFO_UPDATE} self-refresh packet so the nicked
     * player sees the skin change reflected in their own tab list and third-person view.
     * Controlled by {@code skin_change_for_self} in config.yml. Default: {@code true}.
     */
    public boolean isSkinChangeForSelf() {
        return skinChangeForSelf;
    }

    /**
     * Whether nick names are validated against the Minecraft username format
     * (3–16 characters, alphanumeric and underscores only).
     * Controlled by {@code strict_name_validation} in config.yml. Default: {@code true}.
     */
    public boolean isStrictNameValidation() {
        return strictNameValidation;
    }

    /**
     * How long a fetched skin entry stays in the in-memory cache before being
     * re-requested from the Mojang API. Must be &gt; 0.
     * Controlled by {@code skin_cache_ttl_minutes} in config.yml. Default: {@code 30}.
     */
    public int getSkinCacheTtlMinutes() {
        return skinCacheTtlMinutes;
    }

    /**
     * The configured list of names used when randomly selecting a nick.
     * The runtime pool combines this list with the names of currently online players.
     * Returns an unmodifiable view.
     */
    public List<String> getRandomNickPool() {
        return randomNickPool;
    }
}
