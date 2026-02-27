package com.nicked.packet;

import com.github.retrooper.packetevents.protocol.world.dimension.DimensionTypeRef;

/**
 * Cached snapshot of the spawn/dimension info sent to a player in the
 * {@code JOIN_GAME} or {@code RESPAWN} packet. Used to construct a matching
 * Respawn packet for self-skin refresh so the client treats it as a
 * same-dimension respawn and keeps its loaded chunks.
 */
public record SpawnInfo(
        DimensionTypeRef dimensionTypeRef,
        String worldName,
        long hashedSeed,
        boolean isDebug,
        boolean isFlat,
        int seaLevel
) {}
