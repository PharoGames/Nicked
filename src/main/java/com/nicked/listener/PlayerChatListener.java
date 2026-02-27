package com.nicked.listener;

import com.nicked.nick.NickManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Objects;

/**
 * Ensures a nicked player's nick name appears in their outgoing chat messages.
 *
 * <p>Spigot's default chat format uses {@code player.getDisplayName()} as its
 * {@code %1$s} placeholder. When a player is nicked, {@code NickManager} calls
 * {@code player.setDisplayName(nick)}, so in most cases the default format
 * already shows the nick automatically. This listener acts as a safety net for
 * chat plugins that build their own format using the event's format string rather
 * than {@code getDisplayName()} directly.</p>
 *
 * <p>Running at {@code HIGHEST} priority means this fires <em>after</em>
 * {@code NORMAL} and {@code HIGH} listeners. This is intentional: other plugins
 * set their custom format first, and we then ensure the nicked name is substituted
 * into whatever format they produced before the message is delivered.</p>
 */
@SuppressWarnings("deprecation")
public final class PlayerChatListener implements Listener {

    private final NickManager nickManager;

    public PlayerChatListener(NickManager nickManager) {
        this.nickManager = Objects.requireNonNull(nickManager, "nickManager");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (!nickManager.isNicked(event.getPlayer().getUniqueId())) {
            return;
        }

        String nick = nickManager.getNickInfo(event.getPlayer().getUniqueId())
                .map(i -> i.nickedName())
                .orElse(event.getPlayer().getName());

        String format = event.getFormat();
        String realDisplayName = event.getPlayer().getDisplayName();

        if (!realDisplayName.equals(nick)) {
            event.setFormat(format.replace(realDisplayName, nick));
        }
    }
}
