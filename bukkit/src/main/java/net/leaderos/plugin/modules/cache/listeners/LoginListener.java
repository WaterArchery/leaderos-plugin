package net.leaderos.plugin.modules.cache.listeners;

import me.waterarchery.chickencommons.handlers.ExemptHandler;
import net.leaderos.plugin.Bukkit;
import net.leaderos.plugin.modules.cache.model.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 * Login listener class
 * @author poyrazinan
 * @since 1.0
 */
public class LoginListener implements Listener {

    /**
     * Login event of player
     * @param event LoginEvent
     */
    @EventHandler
    public void playerLoginEvent(PlayerLoginEvent event) {
        try {
            if (ExemptHandler.getInstance().isExempt(event.getPlayer().getUniqueId())) return;
        } catch (Exception ignored) { }

        Bukkit.getFoliaLib().getScheduler().runAsync((task) -> User.loadPlayerCache(event.getPlayer()));
    }
}
