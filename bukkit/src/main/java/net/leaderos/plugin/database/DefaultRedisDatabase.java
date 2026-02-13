package net.leaderos.plugin.database;

import com.chickennw.utils.database.redis.RedisDatabase;
import com.chickennw.utils.models.config.redis.RedisConfiguration;
import net.leaderos.plugin.Bukkit;
import net.leaderos.plugin.configuration.Config;

public class DefaultRedisDatabase extends RedisDatabase {

    private static DefaultRedisDatabase instance;

    public static DefaultRedisDatabase getInstance() {
        if (instance == null) {
            Config config = Bukkit.getInstance().getConfigFile();
            instance = new DefaultRedisDatabase(config.getRedisConfiguration());
        }

        return instance;
    }

    private DefaultRedisDatabase(RedisConfiguration redisConfiguration) {
        super(redisConfiguration);
        subscribe(redisConfiguration.getChannel());
    }

    @Override
    public void onMessage(String channel, String message) {

    }
}
