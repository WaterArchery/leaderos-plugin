package net.leaderos.plugin.database;

import com.chickennw.utils.database.redis.RedisDatabase;
import com.chickennw.utils.logger.LoggerFactory;
import com.chickennw.utils.models.config.redis.RedisConfiguration;
import com.chickennw.utils.models.redis.RedisMessage;
import net.leaderos.plugin.Bukkit;
import net.leaderos.plugin.api.managers.ModuleManager;
import net.leaderos.plugin.configuration.Config;
import net.leaderos.plugin.helpers.ChatUtil;
import net.leaderos.plugin.modules.connect.ConnectModule;
import org.bukkit.entity.Player;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.UUID;

public class DefaultRedisDatabase extends RedisDatabase {

    private static DefaultRedisDatabase instance;
    private final Logger logger;

    public static DefaultRedisDatabase getInstance() {
        if (instance == null) {
            Config config = Bukkit.getInstance().getConfigFile();
            instance = new DefaultRedisDatabase(config.getRedisConfiguration());
        }

        return instance;
    }

    private DefaultRedisDatabase(RedisConfiguration redisConfiguration) {
        super(redisConfiguration);
        logger = LoggerFactory.getLogger();
        subscribe(redisConfiguration.getChannel());
    }

    @Override
    public void onMessage(String channel, String message) {
        logger.info("{}: {}", channel, message);
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

            Player player = org.bukkit.Bukkit.getPlayer(playerName);
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

                    RedisMessage publishMessage = new RedisMessage(channel, confirm);
                    publish(publishMessage);
                });
            }
        }
    }
}
