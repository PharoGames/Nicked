package com.nicked.command;

import com.nicked.config.MessagesConfig;
import com.nicked.nick.NickCause;
import com.nicked.nick.NickManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * /nickother {@code <player>} {@code <nick>} — nicks another player to the given name.
 */
public final class NickOtherCommand implements CommandExecutor, TabCompleter {

    private final NickManager nickManager;
    private final MessagesConfig messages;

    public NickOtherCommand(NickManager nickManager, MessagesConfig messages) {
        this.nickManager = Objects.requireNonNull(nickManager, "nickManager");
        this.messages = Objects.requireNonNull(messages, "messages");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("nicked.command.nickother")) {
            messages.send(sender, "no_permission");
            return true;
        }

        if (args.length != 2) {
            messages.send(sender, "usage_nickother");
            return true;
        }

        String targetName = args[0];
        String nick = args[1];

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            messages.send(sender, "player_not_found", "name", targetName);
            return true;
        }

        NickCause cause = sender instanceof Player ? NickCause.COMMAND : NickCause.CONSOLE;

        if (nickManager.getNickedConfig().isStrictNameValidation() && !nickManager.isValidName(nick)) {
            messages.send(sender, "invalid_name", "name", nick);
            return true;
        }

        if (nickManager.isNicked(target.getUniqueId())) {
            nickManager.unnickPlayer(target.getUniqueId(), cause);
        }

        nickManager.nickPlayer(target.getUniqueId(), nick, cause);

        if (nickManager.isNicked(target.getUniqueId())) {
            messages.send(sender, "nick_other_applied", "target", targetName, "nick", nick);
            messages.send(target, "nick_applied", "nick", nick);
        } else {
            messages.send(sender, "nick_cancelled");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
