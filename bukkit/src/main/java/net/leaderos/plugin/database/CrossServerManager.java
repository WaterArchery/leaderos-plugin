package net.leaderos.plugin.database;

import com.chickennw.utils.logger.LoggerFactory;
import lombok.Getter;
import me.waterarchery.cross.api.ChickenCrossApi;
import me.waterarchery.cross.api.redis.DefaultRedisDatabase;
import net.leaderos.plugin.Bukkit;
import net.leaderos.plugin.api.managers.ModuleManager;
import net.leaderos.plugin.helpers.ChatUtil;
import net.leaderos.plugin.modules.connect.ConnectModule;
import org.bukkit.entity.Player;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.UUID;

@Getter
public class CrossServerManager {

    private static CrossServerManager instance;
    private final DefaultRedisDatabase redisDatabase;
    private final Logger logger;

    public static CrossServerManager getInstance() {
        if (instance == null) {
            instance = new CrossServerManager();
        }

        return instance;
    }

    private CrossServerManager() {
        ChickenCrossApi chickenCrossApi = ChickenCrossApi.getInstance();
        redisDatabase = chickenCrossApi.getRedisDatabase();

        logger = LoggerFactory.getLogger();

        Bukkit plugin = Bukkit.getInstance();
        redisDatabase.registerRunnable(plugin.getName(), this::onMessage);
    }

    public void onMessage(String message) {
        JSONObject json = new JSONObject(message);
        String method = json.getString("method");
        if (method.equalsIgnoreCase("confirm")) {
            String uuid = json.getString("uuid");

            logger.info("Redis receive confirmation channel: {}", uuid);
            ConnectModule connectModule = (ConnectModule) ModuleManager.getModule("Connect");
            connectModule.getUuidCommandMap().remove(UUID.fromString(uuid));
        } else if (method.equalsIgnoreCase("execute")) {
            String uuid = json.getString("uuid");
            String playerName = json.getString("player");
            String command = json.getString("command");

            Player player = org.bukkit.Bukkit.getPlayerExact(playerName);
            logger.info("Redis reward channel: {}", uuid);
            if (player != null) {
                logger.info("Player is online, executing command immediately.");
                Bukkit.getFoliaLib().getScheduler().runNextTick((task) -> {
                    org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), command);
                    String msg = ChatUtil.replacePlaceholders(Bukkit.getInstance().getLangFile().getMessages().getConnect().getConnectExecutedCommand());
                    ChatUtil.sendMessage(org.bukkit.Bukkit.getConsoleSender(), msg);

                    JSONObject confirm = new JSONObject();
                    confirm.put("method", "confirm");
                    confirm.put("uuid", uuid);
                    confirm.put("plugin", Bukkit.getInstance().getName());

                    redisDatabase.publish(confirm.toString());
                });
            }
        }
    }
}
