package net.leaderos.plugin.commands;

import com.chickennw.utils.libs.cmd.bukkit.annotation.Permission;
import com.chickennw.utils.libs.cmd.core.annotations.Command;
import com.chickennw.utils.models.commands.BaseCommand;
import lombok.RequiredArgsConstructor;
import net.leaderos.plugin.Bukkit;
import net.leaderos.plugin.api.LeaderOSAPI;
import net.leaderos.plugin.helpers.ChatUtil;
import net.leaderos.shared.Shared;
import net.leaderos.shared.helpers.UrlUtil;
import org.bukkit.command.CommandSender;

/**
 * @author poyrazinan, hyperion
 * @since 1.0
 */
@Command("leaderos")
@RequiredArgsConstructor
public class LeaderOSCommand extends BaseCommand {

    /**
     * default command of leaderos
     * @param sender command sender
     */
    @Command
    @Permission("leaderos.help")
    public void defaultCommand(CommandSender sender) {
        for (String message : Bukkit.getInstance().getLangFile().getMessages().getHelp()) {
            ChatUtil.sendMessage(sender, message);
        }
    }

    /**
     * reload command of plugin
     * @param sender commandsender
     */
    @Permission("leaderos.reload")
    @Command("reload")
    public void reloadCommand(CommandSender sender) {
        Bukkit.getInstance().getConfigFile().load(true);
        Bukkit.getInstance().getLangFile().load(true);
        Bukkit.getInstance().getModulesFile().load(true);

        Shared.setLink(UrlUtil.format(Bukkit.getInstance().getConfigFile().getSettings().getUrl()));
        Shared.setApiKey(Bukkit.getInstance().getConfigFile().getSettings().getApiKey());

        LeaderOSAPI.getModuleManager().reloadModules();
        ChatUtil.sendMessage(sender, Bukkit.getInstance().getLangFile().getMessages().getReload());
    }

}