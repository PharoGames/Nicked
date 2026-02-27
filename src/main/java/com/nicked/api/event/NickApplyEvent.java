package com.nicked.api.event;

import com.nicked.api.NickInfo;
import com.nicked.nick.NickCause;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Objects;

/**
 * Fired after a nick has been fully applied — the skin has been fetched (or failed),
 * packets have been sent, and the {@link NickInfo} snapshot reflects the final state.
 *
 * <p>This event is <strong>not cancellable</strong>. Use {@link NickChangeEvent}
 * to prevent a nick from being applied.</p>
 */
public final class NickApplyEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final NickInfo nickInfo;
    private final NickCause cause;

    /**
     * @param player   the player who was nicked
     * @param nickInfo the immutable snapshot of the applied nick state
     * @param cause    the cause of the nick change
     */
    public NickApplyEvent(Player player, NickInfo nickInfo, NickCause cause) {
        this.player = Objects.requireNonNull(player, "player");
        this.nickInfo = Objects.requireNonNull(nickInfo, "nickInfo");
        this.cause = Objects.requireNonNull(cause, "cause");
    }

    /** @return the player who was nicked */
    public Player getPlayer() {
        return player;
    }

    /** @return the immutable snapshot of the nick state after application */
    public NickInfo getNickInfo() {
        return nickInfo;
    }

    /** @return the cause of this nick change */
    public NickCause getCause() {
        return cause;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
