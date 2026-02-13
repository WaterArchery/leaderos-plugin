package net.leaderos.plugin.modules.bazaar.commands;

import com.chickennw.utils.libs.cmd.bukkit.annotation.Permission;
import com.chickennw.utils.libs.cmd.core.annotations.Command;
import com.chickennw.utils.models.commands.BaseCommand;
import lombok.RequiredArgsConstructor;
import net.leaderos.plugin.Bukkit;
import net.leaderos.plugin.api.managers.ModuleManager;
import net.leaderos.plugin.helpers.ChatUtil;
import net.leaderos.plugin.modules.cache.model.User;
import net.leaderos.plugin.modules.bazaar.gui.BazaarGui;
import org.bukkit.entity.Player;

/**
 * bazaar commands
 * @author poyrazinan
 * @since 1.0
 */
@RequiredArgsConstructor
@Command(value = "bazaar", alias = {"webbazaar", "pazar"})
public class BazaarCommand extends BaseCommand {

    /**
     * Default command of bazaar
     * @param player executor
     */
    @Command
    @Permission("leaderos.bazaar.open")
    public void defaultCommand(Player player) {
        if (ModuleManager.getModule("Bazaar").isEnabled())
            if (User.isPlayerAuthed(player))
                BazaarGui.showGui(player);
            else
                ChatUtil.sendMessage(player, Bukkit.getInstance().getLangFile().getMessages().getRegistrationRequired());
    }
}
