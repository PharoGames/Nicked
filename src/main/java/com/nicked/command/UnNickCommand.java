package com.nicked.command;

import com.nicked.config.MessagesConfig;
import com.nicked.nick.NickCause;
import com.nicked.nick.NickManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * /unnick — removes the sender's own nick.
 */
public final class UnNickCommand implements CommandExecutor, TabCompleter {

    private final NickManager nickManager;
    private final MessagesConfig messages;

    public UnNickCommand(NickManager nickManager, MessagesConfig messages) {
        this.nickManager = Objects.requireNonNull(nickManager, "nickManager");
        this.messages = Objects.requireNonNull(messages, "messages");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("nicked.command.unnick")) {
            messages.send(sender, "no_permission");
            return true;
        }

        if (!(sender instanceof Player player)) {
            messages.send(sender, "player_only");
            return true;
        }

        if (!nickManager.isNicked(player.getUniqueId())) {
            messages.send(player, "unnick_not_nicked");
            return true;
        }

        nickManager.unnickPlayer(player.getUniqueId(), NickCause.COMMAND);
        messages.send(player, "nick_removed", "name", player.getName());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}
