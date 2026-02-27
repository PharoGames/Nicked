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
 * /nick [name]
 *
 * <ul>
 *   <li>No args + not nicked: apply a random nick.</li>
 *   <li>No args + already nicked: unnick (toggle behaviour).</li>
 *   <li>One arg: nick the sender with that name.</li>
 * </ul>
 */
public final class NickCommand implements CommandExecutor, TabCompleter {

    private final NickManager nickManager;
    private final MessagesConfig messages;

    public NickCommand(NickManager nickManager, MessagesConfig messages) {
        this.nickManager = Objects.requireNonNull(nickManager, "nickManager");
        this.messages = Objects.requireNonNull(messages, "messages");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("nicked.command.nick")) {
            messages.send(sender, "no_permission");
            return true;
        }

        if (!(sender instanceof Player player)) {
            messages.send(sender, "player_only");
            return true;
        }

        if (args.length == 0) {
            handleToggle(player);
            return true;
        }

        if (args.length == 1) {
            handleNickWithName(player, args[0]);
            return true;
        }

        messages.send(sender, "usage_nick");
        return true;
    }

    private void handleToggle(Player player) {
        if (nickManager.isNicked(player.getUniqueId())) {
            nickManager.unnickPlayer(player.getUniqueId(), NickCause.COMMAND);
            messages.send(player, "nick_removed", "name", player.getName());
        } else {
            nickManager.nickRandom(player, NickCause.RANDOM);
            if (nickManager.isNicked(player.getUniqueId())) {
                String nick = nickManager.getNickInfo(player.getUniqueId())
                        .map(i -> i.nickedName())
                        .orElse("?");
                messages.send(player, "nick_applied", "nick", nick);
            } else {
                messages.send(player, "no_random_pool");
            }
        }
    }

    private void handleNickWithName(Player player, String name) {
        if (nickManager.getNickedConfig().isStrictNameValidation() && !nickManager.isValidName(name)) {
            messages.send(player, "invalid_name", "name", name);
            return;
        }

        nickManager.nickPlayer(player.getUniqueId(), name, NickCause.COMMAND);

        if (nickManager.isNicked(player.getUniqueId())) {
            messages.send(player, "nick_applied", "nick", name);
        } else {
            messages.send(player, "nick_cancelled");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}
