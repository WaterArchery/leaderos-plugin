package net.leaderos.plugin.modules.voucher;

import lombok.Getter;
import net.leaderos.plugin.Main;
import net.leaderos.plugin.modules.voucher.commands.Commands;
import net.leaderos.plugin.modules.voucher.handlers.VoucherListener;
import net.leaderos.plugin.modules.voucher.data.Data;
import net.leaderos.shared.module.LeaderOSModule;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

/**
 * Voucher module of leaderos plugin
 * @author poyrazinan
 * @since 1.0
 */
public class Voucher extends LeaderOSModule {

    /**
     * Voucher data folder
     */
    @Getter
    private static Data voucherData;

    /**
     * Listener of voucher
     */
    private static VoucherListener voucherListener;

    /**
     * onEnable method of module
     */
    public void onEnable() {
        (voucherData = new Data("voucher_data.yml")).create();
        voucherListener = new VoucherListener();
        Bukkit.getPluginManager().registerEvents(voucherListener, Main.getInstance());
        Main.getCommandManager().registerCommand(new Commands());
    }

    /**
     * onDisable method of module
     */
    public void onDisable() {
        HandlerList.unregisterAll(voucherListener);
        Main.getCommandManager().unregisterCommand(new Commands());
    }

    /**
     * Constructor of Voucher class
     */
    public Voucher() { addDependency("Credit"); }
}
