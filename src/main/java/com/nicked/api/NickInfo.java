package com.nicked.api;

import com.nicked.nick.NickCause;
import com.nicked.nick.SkinData;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable public snapshot of a player's current nick state.
 * Returned by {@link NickedAPI#getNickInfo(UUID)}.
 *
 * <p>{@code nickedSkin} may be {@code null} if the skin fetch from the Mojang API
 * failed or has not yet completed at the time this snapshot was taken.</p>
 */
public record NickInfo(
        UUID realUUID,
        String realName,
        String nickedName,
        SkinData nickedSkin,
        NickCause cause,
        long timestamp
) {

    public NickInfo {
        Objects.requireNonNull(realUUID, "realUUID");
        Objects.requireNonNull(realName, "realName");
        Objects.requireNonNull(nickedName, "nickedName");
        Objects.requireNonNull(cause, "cause");
    }
}
