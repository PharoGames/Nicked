package com.nicked;

import com.github.retrooper.packetevents.PacketEvents;
import com.nicked.api.NickedAPIProvider;
import com.nicked.command.NickAllCommand;
import com.nicked.command.NickCommand;
import com.nicked.command.NickOtherCommand;
import com.nicked.command.RealNameCommand;
import com.nicked.command.UnNickCommand;
import com.nicked.config.MessagesConfig;
import com.nicked.config.NickedConfig;
import com.nicked.hook.PlaceholderAPIHook;
import com.nicked.listener.PlayerChatListener;
import com.nicked.listener.PlayerJoinListener;
import com.nicked.listener.PlayerQuitListener;
import com.nicked.nick.GameProfileModifier;
import com.nicked.nick.NickManager;
import com.nicked.nick.NickStorage;
import com.nicked.nick.SkinFetcher;
import com.nicked.packet.NickedPacketListener;
import com.nicked.packet.PlayerRefresher;
import com.nicked.packet.SpawnInfo;
import com.github.retrooper.packetevents.protocol.chat.RemoteChatSession;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Main plugin class. Kept minimal — all setup is delegated to managers.
 */
public final class NickedPlugin extends JavaPlugin {

    private NickedConfig nickedConfig;
    private MessagesConfig messagesConfig;
    private NickManager nickManager;
    private GameProfileModifier gameProfileModifier;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();

        nickedConfig = new NickedConfig(this);
        if (!isEnabled()) {
            return;
        }

        messagesConfig = new MessagesConfig(this);

        if (nickedConfig.isInternalNameChangeEnabled()) {
            warnInternalNameChange();
            gameProfileModifier = new GameProfileModifier(this);
        }

        Map<UUID, RemoteChatSession> chatSessions = new ConcurrentHashMap<>();
        Map<UUID, SpawnInfo> spawnInfoCache = new ConcurrentHashMap<>();

        SkinFetcher skinFetcher = new SkinFetcher(this, nickedConfig.getSkinCacheTtlMinutes());
        NickStorage storage = new NickStorage(this);
        PlayerRefresher refresher = new PlayerRefresher(this, chatSessions, spawnInfoCache);

        nickManager = new NickManager(this, nickedConfig, skinFetcher, storage, refresher);
        if (gameProfileModifier != null) {
            nickManager.setGameProfileModifier(gameProfileModifier);
        }
        nickManager.loadPersisted();

        NickedAPIProvider.setInstance(nickManager);

        PacketEvents.getAPI().getEventManager().registerListener(
                new NickedPacketListener(nickManager, chatSessions, spawnInfoCache));

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(nickManager), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(nickManager, chatSessions, spawnInfoCache), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(nickManager), this);

        registerCommands();
        hookPlaceholderAPI();

        getLogger().info("Nicked enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (gameProfileModifier != null) {
            gameProfileModifier.restoreAll();
        }

        if (nickManager != null) {
            nickManager.saveAndClear();
        }

        NickedAPIProvider.setInstance(null);
        PacketEvents.getAPI().terminate();

        getLogger().info("Nicked disabled.");
    }

    /** Exposed so managers can access config without being passed a reference. */
    public NickedConfig getNickedConfig() {
        return nickedConfig;
    }

    /** Exposed so managers can send configurable messages. */
    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }

    private void registerCommands() {
        registerCommand("nick", new NickCommand(nickManager, messagesConfig));
        registerCommand("nickall", new NickAllCommand(nickManager, messagesConfig));
        registerCommand("unnick", new UnNickCommand(nickManager, messagesConfig));
        registerCommand("nickother", new NickOtherCommand(nickManager, messagesConfig));
        registerCommand("realname", new RealNameCommand(nickManager, messagesConfig));
    }

    private void registerCommand(String name, Object executor) {
        var cmd = getCommand(name);
        if (cmd == null) {
            getLogger().log(Level.SEVERE, "Command '/" + name + "' not found in plugin.yml.");
            return;
        }
        if (executor instanceof org.bukkit.command.CommandExecutor ce) {
            cmd.setExecutor(ce);
        }
        if (executor instanceof org.bukkit.command.TabCompleter tc) {
            cmd.setTabCompleter(tc);
        }
    }

    private void hookPlaceholderAPI() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(nickManager).register();
            getLogger().info("PlaceholderAPI hook registered.");
        }
    }

    private void warnInternalNameChange() {
        getLogger().severe("=========================================================");
        getLogger().severe("  WARNING: internal_name_change is ENABLED in config.yml");
        getLogger().severe("  This modifies the server-side GameProfile via reflection.");
        getLogger().severe("  Other plugins will see the fake name as the real name.");
        getLogger().severe("  Commands targeting by name will use the fake name.");
        getLogger().severe("  DATA CORRUPTION IS POSSIBLE. USE AT YOUR OWN RISK.");
        getLogger().severe("=========================================================");
    }
}
