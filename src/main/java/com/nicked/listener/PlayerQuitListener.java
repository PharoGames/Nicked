package com.nicked.listener;

import com.github.retrooper.packetevents.protocol.chat.RemoteChatSession;
import com.nicked.nick.NickManager;
import com.nicked.packet.SpawnInfo;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Handles player quits to clean up or retain nick state per configuration.
 */
public final class PlayerQuitListener implements Listener {

    private final NickManager nickManager;
    private final Map<UUID, RemoteChatSession> chatSessions;
    private final Map<UUID, SpawnInfo> spawnInfoCache;

    public PlayerQuitListener(NickManager nickManager,
                              Map<UUID, RemoteChatSession> chatSessions,
                              Map<UUID, SpawnInfo> spawnInfoCache) {
        this.nickManager = Objects.requireNonNull(nickManager, "nickManager");
        this.chatSessions = Objects.requireNonNull(chatSessions, "chatSessions");
        this.spawnInfoCache = Objects.requireNonNull(spawnInfoCache, "spawnInfoCache");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        nickManager.onPlayerQuit(event.getPlayer());
        chatSessions.remove(uuid);
        spawnInfoCache.remove(uuid);
    }
}
