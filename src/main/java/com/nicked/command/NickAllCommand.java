package com.nicked.command;

import com.nicked.config.MessagesConfig;
import com.nicked.nick.NickCause;
import com.nicked.nick.NickManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * /nickall — randomly nicks every online player.
 */
public final class NickAllCommand implements CommandExecutor, TabCompleter {

    private final NickManager nickManager;
    private final MessagesConfig messages;

    public NickAllCommand(NickManager nickManager, MessagesConfig messages) {
        this.nickManager = Objects.requireNonNull(nickManager, "nickManager");
        this.messages = Objects.requireNonNull(messages, "messages");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("nicked.command.nickall")) {
            messages.send(sender, "no_permission");
            return true;
        }

        nickManager.nickAll(NickCause.RANDOM);
        messages.send(sender, "nick_all_applied");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}
