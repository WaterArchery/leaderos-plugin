package net.leaderos.plugin.modules.connect;

import com.chickennw.utils.models.redis.RedisMessage;
import com.pusher.client.connection.ConnectionState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.leaderos.plugin.Bukkit;
import net.leaderos.plugin.database.DefaultRedisDatabase;
import net.leaderos.plugin.helpers.ChatUtil;
import net.leaderos.plugin.modules.connect.listeners.LoginListener;
import net.leaderos.plugin.modules.connect.timer.FallbackTimer;
import net.leaderos.plugin.modules.connect.timer.ReconnectionTimer;
import net.leaderos.shared.helpers.Placeholder;
import net.leaderos.shared.modules.LeaderOSModule;
import net.leaderos.shared.modules.connect.data.CommandsQueue;
import net.leaderos.shared.modules.connect.socket.SocketClient;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Connect module main class
 *
 * @author poyrazinan
 * @since 1.0
 */
@Slf4j
@Getter
public class ConnectModule extends LeaderOSModule {

    /**
     * Socket client for connect to leaderos
     */
    private SocketClient socket;

    /**
     * LoginListener for load cache
     */
    private static LoginListener loginListener;

    /**
     * Commands Queue file
     */
    @Getter
    private static CommandsQueue commandsQueue;

    private final HashMap<UUID, String> uuidCommandMap = new HashMap<>();

    /**
     * onEnable method of module
     */
    public void onEnable() {
        // Load queue data
        commandsQueue = new CommandsQueue("plugins/" + Bukkit.getInstance().getDescription().getName());

        // Register listeners
        if (Bukkit.getInstance().getModulesFile().getConnect().isOnlyOnline()) {
            loginListener = new LoginListener();
            org.bukkit.Bukkit.getPluginManager().registerEvents(loginListener, Bukkit.getInstance());
        }

        // Socket connection
        this.socket = new SocketClient(Bukkit.getInstance().getConfigFile().getSettings().getApiKey(),
            Bukkit.getInstance().getModulesFile().getConnect().getServerToken()) {
            /**
             * Executes console command
             * @param commands command list to execute
             * @param username username of player
             */
            @Override
            public void executeCommands(List<String> commands, String username) {
                List<String> validatedCommands = new ArrayList<>();

                // Get command blacklist from config
                List<String> commandBlacklist = Bukkit.getInstance().getModulesFile().getConnect().getCommandBlacklist();

                // Check if commands are in blacklist
                for (String command : commands) {
                    // If command is not empty and starts with a slash, remove the slash
                    if (command.startsWith("/")) {
                        command = command.substring(1);
                    }

                    // Get the root command (the first word before space)
                    String commandRoot = command.split(" ")[0];

                    // Clean bukkit: prefix
                    if (commandRoot.startsWith("bukkit:")) {
                        commandRoot = commandRoot.substring(7);
                    }

                    // Clean minecraft: prefix
                    if (commandRoot.startsWith("minecraft:")) {
                        commandRoot = commandRoot.substring(10);
                    }

                    // Check if the command is blacklisted
                    if (commandBlacklist.contains(commandRoot)) {
                        String msg = ChatUtil.replacePlaceholders(Bukkit.getInstance().getLangFile().getMessages().getConnect().getCommandBlacklisted(),
                            new Placeholder("%command%", command));
                        ChatUtil.sendMessage(org.bukkit.Bukkit.getConsoleSender(), msg);
                    } else {
                        // If command is valid, add to validatedCommands
                        validatedCommands.add(command);
                    }
                }

                // If player is offline and onlyOnline is true
                Player player = org.bukkit.Bukkit.getPlayer(username);
                if (Bukkit.getInstance().getModulesFile().getConnect().isOnlyOnline() && player == null) {
                    commandsQueue.addCommands(username, validatedCommands);

                    validatedCommands.forEach(command -> {
                        String msg =
                            ChatUtil.replacePlaceholders(Bukkit.getInstance().getLangFile().getMessages().getConnect().getConnectWillExecuteCommand(),
                                new Placeholder("%command%", command));
                        ChatUtil.sendMessage(org.bukkit.Bukkit.getConsoleSender(), msg);
                    });
                } else if (player != null) {
                    // Execute validated commands
                    Bukkit.getFoliaLib().getScheduler().runNextTick(task -> {
                        validatedCommands.forEach(command -> {
                            org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), command);

                            String raw = Bukkit.getInstance().getLangFile().getMessages().getConnect().getConnectExecutedCommand();
                            String msg = ChatUtil.replacePlaceholders(raw, new Placeholder("%command%", command));
                            ChatUtil.sendMessage(org.bukkit.Bukkit.getConsoleSender(), msg);
                        });
                    });
                } else if (Bukkit.getInstance().getModulesFile().getConnect().isUseRedis() &&
                    Bukkit.getInstance().getModulesFile().getConnect().isRedisSender()) {
                    String channel = Bukkit.getInstance().getModulesFile().getConnect().getRedisRewardChannel();
                    DefaultRedisDatabase redis = DefaultRedisDatabase.getInstance();

                    validatedCommands.forEach(command -> {
                        UUID uuid = UUID.randomUUID();
                        JSONObject json = new JSONObject();
                        json.put("player", username);
                        json.put("command", command);
                        json.put("uuid", uuid.toString());

                        RedisMessage redisMessage = new RedisMessage(channel, json);
                        redis.publish(redisMessage);

                        uuidCommandMap.put(uuid, command);
                        log.info("Sending command '{}' for player '{}' via Redis on channel '{}'", command, username, channel);

                        Bukkit.getFoliaLib().getScheduler().runLater((task) -> {
                            if (uuidCommandMap.containsKey(uuid)) {
                                log.info("No confirmation received for command '{}' and player '{}', adding to command queue.", command, username);
                                commandsQueue.addCommands(username, List.of(command));

                                String raw = Bukkit.getInstance().getLangFile().getMessages().getConnect().getConnectWillExecuteCommand();
                                String msg = ChatUtil.replacePlaceholders(raw, new Placeholder("%command%", command));
                                ChatUtil.sendMessage(org.bukkit.Bukkit.getConsoleSender(), msg);
                            }
                        }, 100);
                    });
                }
            }

            @Override
            public void subscribed() {
                ChatUtil.sendMessage(org.bukkit.Bukkit.getConsoleSender(),
                    Bukkit.getInstance().getLangFile().getMessages().getConnect().getSubscribedChannel());
            }
        };

        try {
            ReconnectionTimer.run();
            FallbackTimer.run();
        } catch (Exception ignored) {}
    }

    /**
     * onDisable method of module
     */
    public void onDisable() {
        // Unregister listeners
        try {
            HandlerList.unregisterAll(loginListener);
            getCommandsQueue().getExecutor().shutdown();
            if (ReconnectionTimer.task != null) {
                ReconnectionTimer.task.cancel();
            }
            if (FallbackTimer.task != null) {
                FallbackTimer.task.cancel();
            }
        } catch (Exception ignored) {}
    }

    /**
     * onReload method of module
     */
    public void onReload() {
        this.socket.getPusher().disconnect();
    }

    /**
     * Check connection and reconnect
     */
    public void reconnect() {
        if (this.socket.getPusher().getConnection().getState() == ConnectionState.DISCONNECTED) {
            try {
                this.socket.getPusher().connect();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Constructor of connect
     */
    public ConnectModule() {
    }
}