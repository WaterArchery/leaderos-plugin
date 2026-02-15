package net.leaderos.plugin.modules.webstore.commands;

import com.chickennw.utils.libs.cmd.bukkit.annotation.Permission;
import com.chickennw.utils.libs.cmd.core.annotations.Command;
import com.chickennw.utils.models.commands.BaseCommand;
import lombok.RequiredArgsConstructor;
import net.leaderos.plugin.Bukkit;
import net.leaderos.plugin.api.LeaderOSAPI;
import net.leaderos.plugin.api.managers.ModuleManager;
import net.leaderos.plugin.helpers.ChatUtil;
import net.leaderos.plugin.modules.webstore.gui.WebStoreGui;
import net.leaderos.plugin.modules.webstore.helpers.WebStoreHelper;
import net.leaderos.plugin.modules.webstore.model.Category;
import net.leaderos.shared.helpers.RequestUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * webstore commands
 * @author poyrazinan
 * @since 1.0
 */
@RequiredArgsConstructor
@Command(value = "webstore", alias = {"webshop", "sitemarket", "webmarket"})
public class WebStoreCommand extends BaseCommand {

    /**
     * Default command of webstore
     * @param player executor
     */
    @Command
    @Permission("leaderos.webstore.open")
    public void defaultCommand(Player player) {
        LeaderOSAPI.getModuleManager();
        if (!ModuleManager.getModule("WebStore").isEnabled()) return;

        String categoryId = Bukkit.getInstance().getModulesFile().getWebStore().getGui().getDefaultCategory().getCategoryId();
        if (Bukkit.getInstance().getModulesFile().getWebStore().getGui().getDefaultCategory().isEnable() && !categoryId.equals("0")) {
            categoryCommand(player, categoryId);
        } else {
            WebStoreGui.showGui(player, null);
        }
    }

    /**
     * Open category command of webstore
     * @param sender commandsender
     * @param categoryId category id to open
     */
    @Permission("leaderos.webstore.open")
    @Command("category")
    public void categoryCommand(CommandSender sender, String categoryId) {
        if (!(sender instanceof Player player)) return;
        if (!ModuleManager.getModule("WebStore").isEnabled()) return;

        if (!RequestUtil.canRequest(player.getUniqueId())) {
            ChatUtil.sendMessage(player, Bukkit.getInstance().getLangFile().getMessages().getHaveRequestOngoing());
            return;
        }

        RequestUtil.addRequest(player.getUniqueId());

        Bukkit.getFoliaLib().getScheduler().runAsync((task) -> {
            Category category = WebStoreHelper.findCategoryById(player.getName(), categoryId);
            RequestUtil.invalidate(player.getUniqueId());

            if (category == null) {
                player.sendMessage(ChatUtil.color(Bukkit.getInstance().getLangFile().getGui().getWebStoreGui().getWebStoreCategoryNotFound()));
                return;
            }

            WebStoreGui.showGui(player, category);
        });
    }

    /**
     * Buy command of webstore
     * @param sender commandsender
     * @param productId product id to buy
     */
    @Permission("leaderos.webstore.buy")
    @Command("buy")
    public void buyCommand(CommandSender sender, String productId) {
        if (!(sender instanceof Player player)) return;
        if (!ModuleManager.getModule("WebStore").isEnabled()) return;

        WebStoreHelper.buyItem(player, productId);
    }
}
