package com.nicked.api.event;

import com.nicked.nick.NickCause;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Objects;

/**
 * Fired when a random nick name has been selected for a player, before it is applied.
 *
 * <p>This event is <strong>cancellable</strong>. Cancelling it aborts the random
 * nick assignment for that player. The selected name can also be replaced by
 * calling {@link #setSelectedName(String)}.</p>
 */
public final class NickRandomSelectEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private String selectedName;
    private final NickCause cause;
    private boolean cancelled;

    /**
     * @param player       the player receiving a random nick
     * @param selectedName the randomly chosen nick name
     * @param cause        the cause (typically {@code RANDOM})
     */
    public NickRandomSelectEvent(Player player, String selectedName, NickCause cause) {
        this.player = Objects.requireNonNull(player, "player");
        this.selectedName = Objects.requireNonNull(selectedName, "selectedName");
        this.cause = Objects.requireNonNull(cause, "cause");
    }

    /** @return the player receiving a random nick */
    public Player getPlayer() {
        return player;
    }

    /** @return the currently selected random nick name */
    public String getSelectedName() {
        return selectedName;
    }

    /**
     * Replaces the randomly selected nick name with a custom one.
     *
     * @param selectedName the name to use instead
     */
    public void setSelectedName(String selectedName) {
        this.selectedName = Objects.requireNonNull(selectedName, "selectedName");
    }

    /** @return the cause of the random nick selection */
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
