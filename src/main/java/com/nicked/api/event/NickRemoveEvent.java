package com.nicked.api.event;

import com.nicked.nick.NickCause;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Objects;

/**
 * Fired before a player's nick is removed (unnicked).
 *
 * <p>This event is <strong>cancellable</strong>. Cancelling it prevents the unnick
 * from being applied.</p>
 */
public final class NickRemoveEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String nickedName;
    private final NickCause cause;
    private boolean cancelled;

    /**
     * @param player     the player being unnicked
     * @param nickedName the nick name being removed
     * @param cause      the cause of the removal
     */
    public NickRemoveEvent(Player player, String nickedName, NickCause cause) {
        this.player = Objects.requireNonNull(player, "player");
        this.nickedName = Objects.requireNonNull(nickedName, "nickedName");
        this.cause = Objects.requireNonNull(cause, "cause");
    }

    /** @return the player being unnicked */
    public Player getPlayer() {
        return player;
    }

    /** @return the nick name being removed */
    public String getNickedName() {
        return nickedName;
    }

    /** @return the cause of this removal */
    public NickCause getCause() {
        return cause;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
