package com.nicked.command;

import com.nicked.api.NickInfo;
import com.nicked.config.MessagesConfig;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * /realname {@code <player>} — reveals the real name of a nicked player.
 * The argument is matched against both the nick name and the real name.
 */
public final class RealNameCommand implements CommandExecutor, TabCompleter {

    private final NickManager nickManager;
    private final MessagesConfig messages;

    public RealNameCommand(NickManager nickManager, MessagesConfig messages) {
        this.nickManager = Objects.requireNonNull(nickManager, "nickManager");
        this.messages = Objects.requireNonNull(messages, "messages");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("nicked.command.realname")) {
            messages.send(sender, "no_permission");
            return true;
        }

        if (args.length != 1) {
            messages.send(sender, "usage_realname");
            return true;
        }

        String query = args[0];
        Player onlineMatch = findByNickOrName(query);

        if (onlineMatch == null) {
            messages.send(sender, "player_not_found", "name", query);
            return true;
        }

        Optional<NickInfo> nickInfo = nickManager.getNickInfo(onlineMatch.getUniqueId());
        if (nickInfo.isEmpty()) {
            messages.send(sender, "realname_not_nicked", "player", query);
            return true;
        }

        messages.send(sender, "realname_result",
                "nick", nickInfo.get().nickedName(),
                "real_name", nickInfo.get().realName());

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(p -> nickManager.getDisplayName(p.getUniqueId()))
                    .filter(n -> n.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Player findByNickOrName(String query) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            String displayName = nickManager.getDisplayName(p.getUniqueId());
            if (displayName.equalsIgnoreCase(query) || p.getName().equalsIgnoreCase(query)) {
                return p;
            }
        }
        return null;
    }
}
