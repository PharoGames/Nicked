package com.nicked.nick;


import java.util.Objects;
import java.util.UUID;

/**
 * Immutable snapshot of a player's active nick state.
 * {@code originalSkin} and {@code nickedSkin} may be null when the skin
 * could not be fetched from the Mojang API.
 */
public record NickData(
        UUID realUUID,
        String realName,
        String nickedName,
        SkinData originalSkin,
        SkinData nickedSkin,
        NickCause cause,
        long timestamp
) {

    public NickData {
        Objects.requireNonNull(realUUID, "realUUID");
        Objects.requireNonNull(realName, "realName");
        Objects.requireNonNull(nickedName, "nickedName");
        Objects.requireNonNull(cause, "cause");
    }
}
