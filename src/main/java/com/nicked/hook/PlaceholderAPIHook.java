package com.nicked.hook;

import com.nicked.api.NickedAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.util.Objects;

/**
 * PlaceholderAPI expansion for the Nicked plugin.
 *
 * <p>Available placeholders:</p>
 * <ul>
 *   <li>{@code %nicked_displayname%} — nicked name if nicked, otherwise real name</li>
 *   <li>{@code %nicked_is_nicked%} — "true" or "false"</li>
 *   <li>{@code %nicked_real_name%} — always the real name</li>
 * </ul>
 */
public final class PlaceholderAPIHook extends PlaceholderExpansion {

    private final NickedAPI api;

    public PlaceholderAPIHook(NickedAPI api) {
        this.api = Objects.requireNonNull(api, "api");
    }

    @Override    public String getIdentifier() {
        return "nicked";
    }

    @Override    public String getAuthor() {
        return "Nicked";
    }

    @Override    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) {
            return null;
        }
        return switch (params.toLowerCase()) {
            case "displayname" -> api.getDisplayName(player.getUniqueId());
            case "is_nicked" -> String.valueOf(api.isNicked(player.getUniqueId()));
            case "real_name" -> api.getRealName(player.getUniqueId());
            default -> null;
        };
    }
}
