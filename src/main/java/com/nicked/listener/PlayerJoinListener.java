package com.nicked.listener;

import com.nicked.nick.NickManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;

/**
 * Handles player joins to restore persisted nicks and refresh the joining
 * player's appearance for viewers.
 */
public final class PlayerJoinListener implements Listener {

    private final NickManager nickManager;

    public PlayerJoinListener(NickManager nickManager) {
        this.nickManager = Objects.requireNonNull(nickManager, "nickManager");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        nickManager.onPlayerJoin(event.getPlayer());
    }
}
