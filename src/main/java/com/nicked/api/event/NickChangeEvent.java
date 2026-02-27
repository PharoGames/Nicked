package com.nicked.api.event;

import com.nicked.nick.NickCause;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Objects;

/**
 * Fired before a nick is applied to a player.
 *
 * <p>This event is <strong>cancellable</strong>. Cancelling it prevents the nick
 * from being applied.</p>
 */
public final class NickChangeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String oldNick;
    private final String newNick;
    private final NickCause cause;
    private boolean cancelled;

    /**
     * @param player  the player being nicked
     * @param oldNick the previous nick, or {@code null} if not previously nicked
     * @param newNick the nick being applied
     * @param cause   the cause of the change
     */
    public NickChangeEvent(Player player, String oldNick, String newNick, NickCause cause) {
        this.player = Objects.requireNonNull(player, "player");
        this.oldNick = oldNick;
        this.newNick = Objects.requireNonNull(newNick, "newNick");
        this.cause = Objects.requireNonNull(cause, "cause");
    }

    /** @return the player being nicked */
    public Player getPlayer() {
        return player;
    }

    /** @return the player's previous nick, or {@code null} if they were not nicked before */
    public String getOldNick() {
        return oldNick;
    }

    /** @return the nick name being applied */
    public String getNewNick() {
        return newNick;
    }

    /** @return the cause of this nick change */
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
