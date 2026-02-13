package net.leaderos.plugin.modules.donations.commands;

import com.chickennw.utils.libs.cmd.bukkit.annotation.Permission;
import com.chickennw.utils.libs.cmd.core.annotations.Command;
import com.chickennw.utils.models.commands.BaseCommand;
import lombok.RequiredArgsConstructor;
import net.leaderos.plugin.Bukkit;
import net.leaderos.plugin.api.managers.ModuleManager;
import net.leaderos.plugin.helpers.ChatUtil;
import net.leaderos.plugin.modules.donations.gui.DonationGui;
import net.leaderos.plugin.modules.donations.timer.Timer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Recent donators module commands
 * @author poyrazinan
 * @since 1.0
 */
@RequiredArgsConstructor
@Command(value = "donations", alias = {"recentdonations", "krediyukleyenler"})
public class DonationsCommand extends BaseCommand {

    /**
     * Default command of recent donations
     * @param player executor
     */
    @Command
    @Permission("leaderos.donations.open")
    public void defaultCommand(Player player) {
        if (ModuleManager.getModule("Donations").isEnabled())
            DonationGui.showGui(player);
    }

    /**
     * updateCache command
     * @param sender executor
     */
    @Command(value = "update", alias = {"güncelle"})
    @Permission("leaderos.donations.update")
    public void updateCacheCommand(CommandSender sender) {
        ChatUtil.sendMessage(sender, Bukkit.getInstance().getLangFile().getGui().getDonationsGui().getUpdatedDonationDataMessage());
        Timer.run();
    }
}
