package org.intrigger.ultimate_market;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.intrigger.ultimate_market.commands.MarketExecutor;
import org.intrigger.ultimate_market.commands.MarketTabComplete;
import org.intrigger.ultimate_market.listeners.ClickHandler;

import java.util.Objects;
import java.util.logging.Logger;

public final class Ultimate_market extends JavaPlugin {

    public static Logger LOGGER;

    public static Plugin plugin;
    MarketExecutor marketExecutor;

    @Override
    public void onEnable() {
        LOGGER = getLogger();
        LOGGER.info(ChatColor.DARK_PURPLE  + "Ultimate Market " + ChatColor.RESET + "plugin has been " + ChatColor.GREEN + "enabled!");

        plugin = this;

        marketExecutor = new MarketExecutor(this);

        Objects.requireNonNull(getCommand("ah")).setExecutor(marketExecutor);
        Objects.requireNonNull(getCommand("ah")).setTabCompleter(new MarketTabComplete());

        new ClickHandler(this, LOGGER, marketExecutor);

    }

    @Override
    public void onDisable() {
        marketExecutor.closeDatabase();
        LOGGER.info(ChatColor.DARK_PURPLE  + "Ultimate Market " + ChatColor.RESET + "plugin has been " + ChatColor.RED + "disabled!");
    }
}
