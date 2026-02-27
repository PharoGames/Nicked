package com.nicked.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Objects;
import java.util.UUID;

/**
 * Fired when any system resolves a player's display name through the Nicked API
 * (i.e. {@code NickedAPI#getDisplayName(UUID)}).
 *
 * <p>Listening plugins may override the resolved name by calling
 * {@link #setResolvedName(String)}. This allows integration with chat
 * formatting, scoreboards, or other display systems.</p>
 *
 * <p>This event is not cancellable.</p>
 */
public final class NickResolveEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID uuid;
    private final String realName;
    private String resolvedName;

    /**
     * @param uuid         the UUID of the player being resolved
     * @param realName     the player's real name
     * @param resolvedName the initial resolved name (nicked name if nicked, else real name)
     */
    public NickResolveEvent(UUID uuid, String realName, String resolvedName) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
        this.realName = Objects.requireNonNull(realName, "realName");
        this.resolvedName = Objects.requireNonNull(resolvedName, "resolvedName");
    }

    /** @return the UUID of the player being resolved */
    public UUID getPlayerUUID() {
        return uuid;
    }

    /** @return the player's real name, regardless of nick state */
    public String getRealName() {
        return realName;
    }

    /** @return the currently resolved display name */
    public String getResolvedName() {
        return resolvedName;
    }

    /**
     * Overrides the resolved display name returned by the API.
     *
     * @param resolvedName the new display name to use
     */
    public void setResolvedName(String resolvedName) {
        this.resolvedName = Objects.requireNonNull(resolvedName, "resolvedName");
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
