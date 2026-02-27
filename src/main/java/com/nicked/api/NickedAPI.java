package com.nicked.api;

import com.nicked.nick.NickCause;

import java.util.Optional;
import java.util.UUID;

/**
 * Public API for the Nicked plugin.
 *
 * <p>Access the implementation via {@link NickedAPIProvider#getAPI()}.
 * All methods throw {@link NullPointerException} if any non-nullable
 * parameter is null.</p>
 *
 * <p>Methods that trigger nick changes fire the corresponding Bukkit events
 * (e.g. {@code NickChangeEvent}), allowing third-party plugins to cancel
 * or listen to changes.</p>
 */
public interface NickedAPI {

    /**
     * Returns the player's display name: their nicked name if currently nicked,
     * otherwise their real name. Fires {@code NickResolveEvent} to allow
     * downstream plugins to override the resolved value.
     *
     * @param uuid the player's UUID; may belong to an offline player
     * @return the display name, never null
     */
    String getDisplayName(UUID uuid);

    /**
     * Returns {@code true} if the player is currently nicked.
     *
     * @param uuid the player's UUID
     * @return whether the player is nicked
     */
    boolean isNicked(UUID uuid);

    /**
     * Returns an immutable snapshot of the player's active nick state.
     *
     * @param uuid the player's UUID
     * @return an Optional containing nick info, or empty if not nicked
     */
    Optional<NickInfo> getNickInfo(UUID uuid);

    /**
     * Applies a nick to the player. Fires {@link com.nicked.api.event.NickChangeEvent}
     * (cancellable) synchronously before any change is made. If the event is cancelled
     * the call is a no-op.
     *
     * <p>The nick entry is stored synchronously so {@link #isNicked(UUID)} returns
     * {@code true} immediately. The skin fetch is asynchronous: once the Mojang API
     * responds, the skin is applied and {@link com.nicked.api.event.NickApplyEvent}
     * is fired on the main thread. If no skin can be fetched the nick remains active
     * but without a skin change, and the player receives a {@code skin_fetch_failed}
     * message.</p>
     *
     * <p>The player must be online; if they are not, a warning is logged and the
     * call returns immediately without storing any data.</p>
     *
     * @param uuid    the UUID of the player to nick; must be an online player
     * @param newName the nick name to apply
     * @param cause   the reason for the nick change
     */
    void nickPlayer(UUID uuid, String newName, NickCause cause);

    /**
     * Removes the player's active nick. Fires {@link com.nicked.api.event.NickRemoveEvent}
     * (cancellable) before removing. Does nothing if the player is not currently nicked.
     * If the event is cancelled the nick is left in place.
     *
     * @param uuid  the UUID of the player to unnick
     * @param cause the reason for the removal
     */
    void unnickPlayer(UUID uuid, NickCause cause);

    /**
     * Returns the player's real (underlying) name regardless of nick state.
     * For online players this is {@code Player#getName()}. For offline players
     * this resolves from persistent storage or falls back to the UUID-based name
     * if unknown.
     *
     * @param uuid the player's UUID
     * @return the real name, never null
     */
    String getRealName(UUID uuid);
}
