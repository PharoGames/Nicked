package com.nicked.config;

import com.nicked.NickedPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Loads and caches all messages from messages.yml.
 * Messages use MiniMessage format with {placeholder} tokens.
 * Output is serialized to legacy section-sign format for Spigot compatibility.
 */
public final class MessagesConfig {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private final NickedPlugin plugin;
    private FileConfiguration messages;

    public MessagesConfig(NickedPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        load();
    }

    private void load() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);

        try (InputStream defaultStream = plugin.getResource("messages.yml")) {
            if (defaultStream != null) {
                YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
                messages.setDefaults(defaults);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not load default messages.yml resource", e);
        }
    }

    /**
     * Sends a formatted message to the given sender.
     * Placeholders are applied as alternating key/value pairs.
     *
     * @param sender       the recipient
     * @param key          the message key in messages.yml
     * @param replacements alternating key/value pairs, e.g. "nick", "Steve"
     */
    public void send(CommandSender sender, String key, String... replacements) {
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(key, "key");
        sender.sendMessage(getLegacy(key, replacements));
    }

    /**
     * Returns a legacy-formatted string for the given message key.
     *
     * @param key          the message key
     * @param replacements alternating key/value pairs
     * @return legacy section-sign color-coded string
     */
    public String getLegacy(String key, String... replacements) {
        Objects.requireNonNull(key, "key");
        String raw = messages.getString(key, "<red>Missing message: " + key);
        String prefix = messages.getString("prefix", "");
        raw = applyReplacements(prefix + raw, replacements);
        Component component = MINI.deserialize(raw);
        return LEGACY.serialize(component);
    }

    /**
     * Returns the parsed {@link Component} for advanced usage (e.g., sending to Players via Adventure).
     *
     * @param key          the message key
     * @param replacements alternating key/value pairs
     * @return parsed Component
     */
    public Component getComponent(String key, String... replacements) {
        Objects.requireNonNull(key, "key");
        String raw = messages.getString(key, "<red>Missing message: " + key);
        String prefix = messages.getString("prefix", "");
        raw = applyReplacements(prefix + raw, replacements);
        return MINI.deserialize(raw);
    }

    private String applyReplacements(String template, String... pairs) {
        if (pairs.length == 0) {
            return template;
        }
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("replacements must be key/value pairs, got odd count: " + pairs.length);
        }
        String result = template;
        for (int i = 0; i < pairs.length; i += 2) {
            result = result.replace("{" + pairs[i] + "}", pairs[i + 1]);
        }
        return result;
    }
}
