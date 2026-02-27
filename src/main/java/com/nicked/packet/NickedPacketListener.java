package com.nicked.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.chat.RemoteChatSession;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerJoinGame;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRespawn;
import com.nicked.nick.NickData;
import com.nicked.nick.NickManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Intercepts outgoing packets to:
 * <ul>
 *   <li>Rewrite {@code PLAYER_INFO_UPDATE} with nicked profile data</li>
 *   <li>Cache {@link RemoteChatSession} from {@code INITIALIZE_CHAT} actions</li>
 *   <li>Cache {@link SpawnInfo} from {@code JOIN_GAME} / {@code RESPAWN} packets
 *       so the self-refresh Respawn packet matches the client's current world exactly</li>
 * </ul>
 */
public final class NickedPacketListener extends PacketListenerAbstract {

    private final NickManager nickManager;
    private final Map<UUID, RemoteChatSession> chatSessions;
    private final Map<UUID, SpawnInfo> spawnInfoCache;

    public NickedPacketListener(NickManager nickManager,
                                Map<UUID, RemoteChatSession> chatSessions,
                                Map<UUID, SpawnInfo> spawnInfoCache) {
        super(PacketListenerPriority.HIGH);
        this.nickManager = Objects.requireNonNull(nickManager, "nickManager");
        this.chatSessions = Objects.requireNonNull(chatSessions, "chatSessions");
        this.spawnInfoCache = Objects.requireNonNull(spawnInfoCache, "spawnInfoCache");
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.PLAYER_INFO_UPDATE) {
            handlePlayerInfoUpdate(event);
        } else if (event.getPacketType() == PacketType.Play.Server.JOIN_GAME) {
            cacheFromJoinGame(event);
        } else if (event.getPacketType() == PacketType.Play.Server.RESPAWN) {
            cacheFromRespawn(event);
        }
    }

    private void cacheFromJoinGame(PacketSendEvent event) {
        if (!(event.getPlayer() instanceof Player p)) return;
        WrapperPlayServerJoinGame packet = new WrapperPlayServerJoinGame(event);
        SpawnInfo info = new SpawnInfo(
                packet.getDimensionTypeRef(),
                packet.getWorldName(),
                packet.getHashedSeed(),
                packet.isDebug(),
                packet.isFlat(),
                packet.getSeaLevel());
        spawnInfoCache.put(p.getUniqueId(), info);
    }

    private void cacheFromRespawn(PacketSendEvent event) {
        if (!(event.getPlayer() instanceof Player p)) return;
        WrapperPlayServerRespawn packet = new WrapperPlayServerRespawn(event);
        spawnInfoCache.put(p.getUniqueId(), new SpawnInfo(
                packet.getDimensionTypeRef(),
                packet.getWorldName().orElse(""),
                packet.getHashedSeed(),
                packet.isWorldDebug(),
                packet.isWorldFlat(),
                packet.getSeaLevel()
        ));
    }

    private void handlePlayerInfoUpdate(PacketSendEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        WrapperPlayServerPlayerInfoUpdate packet = new WrapperPlayServerPlayerInfoUpdate(event);
        boolean hasInitChat = packet.getActions()
                .contains(WrapperPlayServerPlayerInfoUpdate.Action.INITIALIZE_CHAT);

        Map<UUID, NickData> activeNicks = nickManager.getActiveNicks();
        boolean modified = false;

        for (WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry : packet.getEntries()) {
            UUID profileUuid = entry.getGameProfile().getUUID();

            if (hasInitChat && entry.getChatSession() != null) {
                chatSessions.put(profileUuid, entry.getChatSession());
            }

            NickData data = activeNicks.get(profileUuid);
            if (data == null) continue;

            UserProfile profile = entry.getGameProfile();
            profile.setName(data.nickedName());

            if (data.nickedSkin() != null) {
                List<TextureProperty> textures = new ArrayList<>();
                textures.add(new TextureProperty(
                        "textures",
                        data.nickedSkin().value(),
                        data.nickedSkin().signature()));
                profile.setTextureProperties(textures);
            }

            entry.setGameProfile(profile);
            entry.setDisplayName(Component.text(data.nickedName()));
            modified = true;
        }

        if (modified) {
            event.markForReEncode(true);
        }
    }
}
