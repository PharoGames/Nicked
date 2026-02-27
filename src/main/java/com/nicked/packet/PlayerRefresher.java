package com.nicked.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.chat.RemoteChatSession;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChangeGameState;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerAbilities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRespawn;
import com.github.retrooper.packetevents.util.Vector3d;
import com.nicked.NickedPlugin;
import com.nicked.nick.SkinData;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Refreshes a player's visible identity for all viewers and optionally for themselves
 * after a nick or skin change.
 *
 * <p>For <em>other players</em>: calls {@code hidePlayer}/{@code showPlayer}, forcing
 * the client to re-receive a fresh {@code PLAYER_INFO_UPDATE} that our
 * {@link NickedPacketListener} rewrites with the nicked name and skin.</p>
 *
 * <p>For <em>self</em> (when {@code skin_change_for_self} is enabled): sends
 * {@code PLAYER_INFO_REMOVE} + {@code PLAYER_INFO_UPDATE} (with
 * {@code ADD_PLAYER + INITIALIZE_CHAT + UPDATE_LISTED} etc.) to update the
 * tab-list entry, then a same-dimension {@code RESPAWN} packet built from
 * the cached Login/Respawn spawn info. Because the dimension name matches
 * exactly, the client treats it as a same-world respawn: it keeps all loaded
 * chunks but recreates the local player entity with the new skin from the
 * updated tab list. A 1-tick delayed teleport restores the client's position.</p>
 */
public final class PlayerRefresher {

    private final NickedPlugin plugin;
    private final Map<UUID, RemoteChatSession> chatSessions;
    private final Map<UUID, SpawnInfo> spawnInfoCache;

    public PlayerRefresher(NickedPlugin plugin,
                           Map<UUID, RemoteChatSession> chatSessions,
                           Map<UUID, SpawnInfo> spawnInfoCache) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.chatSessions = Objects.requireNonNull(chatSessions, "chatSessions");
        this.spawnInfoCache = Objects.requireNonNull(spawnInfoCache, "spawnInfoCache");
    }

    public void refresh(Player player, SkinData displaySkin, String displayName) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(displayName, "displayName");

        if (plugin.getNickedConfig().isSkinChangeForSelf()) {
            refreshSelf(player, displaySkin, displayName);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> refreshForOthers(player), 2L);
    }

    private void refreshSelf(Player player, SkinData skin, String displayName) {
        SpawnInfo info = spawnInfoCache.get(player.getUniqueId());
        if (info == null) {
            plugin.getLogger().warning(
                    "No cached spawn info for " + player.getName()
                    + "; self-skin refresh skipped (will update on next death/respawn).");
            return;
        }

        // --- Snapshot state BEFORE the respawn ---
        Location loc = player.getLocation().clone();
        org.bukkit.util.Vector vel = player.getVelocity().clone();
        boolean wasFlying = player.isFlying();
        boolean allowFlight = player.getAllowFlight();
        float flySpeed = player.getFlySpeed();
        float walkSpeed = player.getWalkSpeed();
        int expLevel = player.getLevel();
        float expProgress = player.getExp();
        int heldSlot = player.getInventory().getHeldItemSlot();
        boolean isOp = player.isOp();
        boolean isGodMode = player.isInvulnerable();

        UserProfile fakeProfile = buildProfile(player, skin, displayName);
        GameMode packetGameMode = toPacketGameMode(player.getGameMode());
        boolean isCreative = player.getGameMode() == org.bukkit.GameMode.CREATIVE;
        RemoteChatSession cachedSession = chatSessions.get(player.getUniqueId());

        // 1. Update tab list: remove old entry, re-add with new profile + chat session
        WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry =
                new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                        fakeProfile, true, player.getPing(), packetGameMode,
                        Component.text(displayName), cachedSession);

        EnumSet<WrapperPlayServerPlayerInfoUpdate.Action> actions = EnumSet.of(
                WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
                WrapperPlayServerPlayerInfoUpdate.Action.INITIALIZE_CHAT,
                WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_GAME_MODE,
                WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED,
                WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LATENCY,
                WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME);

        WrapperPlayServerPlayerInfoRemove removePacket =
                new WrapperPlayServerPlayerInfoRemove(Collections.singletonList(player.getUniqueId()));

        WrapperPlayServerPlayerInfoUpdate addPacket =
                new WrapperPlayServerPlayerInfoUpdate(actions, entry);

        // 2. Same-dimension Respawn using the exact spawn info from the Login packet.
        WrapperPlayServerRespawn respawnPacket = new WrapperPlayServerRespawn(
                info.dimensionTypeRef(),
                info.worldName(),
                null,
                info.hashedSeed(),
                packetGameMode,
                packetGameMode,
                info.isDebug(),
                info.isFlat(),
                WrapperPlayServerRespawn.KEEP_ALL_DATA,
                null,
                0,
                info.seaLevel());

        // 3. Position packet to keep the client at their current location.
        WrapperPlayServerPlayerPositionAndLook posPacket =
                new WrapperPlayServerPlayerPositionAndLook(
                        loc.getX(), loc.getY(), loc.getZ(),
                        loc.getYaw(), loc.getPitch(),
                        (byte) 0, 0, false);

        // 4. Abilities packet to preserve flying state, fly speed, etc.
        WrapperPlayServerPlayerAbilities abilitiesPacket =
                new WrapperPlayServerPlayerAbilities(
                        isGodMode, wasFlying, allowFlight, isCreative,
                        flySpeed, walkSpeed);

        // 5. Velocity packet to preserve momentum.
        WrapperPlayServerEntityVelocity velocityPacket =
                new WrapperPlayServerEntityVelocity(
                        player.getEntityId(),
                        new Vector3d(vel.getX(), vel.getY(), vel.getZ()));

        // 6. Game event: tell the client to stop showing "Loading terrain".
        WrapperPlayServerChangeGameState chunkStartPacket =
                new WrapperPlayServerChangeGameState(
                        WrapperPlayServerChangeGameState.Reason.START_LOADING_CHUNKS, 0f);

        var manager = PacketEvents.getAPI().getPlayerManager();
        manager.sendPacket(player, removePacket);
        manager.sendPacket(player, addPacket);
        manager.sendPacket(player, respawnPacket);
        manager.sendPacket(player, posPacket);
        manager.sendPacket(player, abilitiesPacket);
        manager.sendPacket(player, velocityPacket);
        manager.sendPacket(player, chunkStartPacket);

        // 7. Sync server-side state on next tick.
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            player.teleport(loc);
            player.setAllowFlight(allowFlight);
            player.setFlying(wasFlying);
            player.setFlySpeed(flySpeed);
            player.setWalkSpeed(walkSpeed);
            player.setLevel(expLevel);
            player.setExp(expProgress);
            player.getInventory().setHeldItemSlot(heldSlot);
            player.setVelocity(vel);
            player.updateInventory();
        }, 1L);
    }

    private void refreshForOthers(Player player) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.getUniqueId().equals(player.getUniqueId())) continue;
            if (!viewer.canSee(player)) continue;
            viewer.hidePlayer(plugin, player);
            viewer.showPlayer(plugin, player);
        }
    }

    private GameMode toPacketGameMode(org.bukkit.GameMode bukkit) {
        return switch (bukkit) {
            case CREATIVE -> GameMode.CREATIVE;
            case ADVENTURE -> GameMode.ADVENTURE;
            case SPECTATOR -> GameMode.SPECTATOR;
            default -> GameMode.SURVIVAL;
        };
    }

    private UserProfile buildProfile(Player player, SkinData skin, String displayName) {
        List<TextureProperty> textures = new ArrayList<>();
        if (skin != null) {
            textures.add(new TextureProperty("textures", skin.value(), skin.signature()));
        }
        return new UserProfile(player.getUniqueId(), displayName, textures);
    }
}
