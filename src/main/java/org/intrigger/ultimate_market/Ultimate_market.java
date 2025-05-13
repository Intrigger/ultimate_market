package org.intrigger.ultimate_market;

import org.apache.logging.log4j.LogManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.intrigger.ultimate_market.commands.MarketExecutor;
import org.intrigger.ultimate_market.commands.MarketTabComplete;
import org.intrigger.ultimate_market.listeners.ClickHandler;
import org.intrigger.ultimate_market.output.Log4JFilter;
import org.intrigger.ultimate_market.utils.*;
import org.intrigger.ultimate_market.utils.Interface.GUI;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.logging.Logger;

public final class Ultimate_market extends JavaPlugin {

    /*
    * [X] Добавить категорию избранное для желаемых предметов
    * [X] Добавить в сортировку "Дешевые за 1 шт"
    * [X] Добавить авто-сдачу на поставки. При нажатии кнопки игрок сдает весь инвентарь на самые выгодные для
    *     каждого из предметов поставки, если такие есть
    * [X] Добавить поиск поставок на нужный товар
    * [X] Добавить покупку по средней цене для /ah buy
    */

    public static Logger LOGGER;

    public static Plugin plugin;
    public static MarketExecutor marketExecutor;
    public static PlayerInfo info;
    public static GUI gui;
    public static ItemCategoriesProcessor itemCategoriesProcessor;
    public static ItemStorage itemStorage;
    public static BuyRequestStorage buyRequestStorage;
    public static GroupsPermissions groupsPermissions;

    @Override
    public void onEnable() {
        LOGGER = getLogger();

        LOGGER.info(ChatColor.DARK_PURPLE  + "Ultimate Market " + ChatColor.RESET + "plugin has been " + ChatColor.GREEN + "enabled!");

        plugin = this;
        create_data_folder();

        itemStorage = new ItemStorage(plugin.getDataFolder() + "/database.db");
        buyRequestStorage = new BuyRequestStorage();
        marketExecutor = new MarketExecutor(this);

        info = new PlayerInfo();
        try {
            gui = new GUI();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 IOException e) {
            throw new RuntimeException(e);
        }
        groupsPermissions = new GroupsPermissions();
        itemCategoriesProcessor = new ItemCategoriesProcessor( plugin.getDataFolder() + "/item_categories.yml");

        Objects.requireNonNull(getCommand("ah")).setExecutor(marketExecutor);
        Objects.requireNonNull(getCommand("ah")).setTabCompleter(new MarketTabComplete());

        new ClickHandler(this, marketExecutor);

        Metrics metrics = new Metrics(this, 18923);
        setLog4JFilter();
    }

    private void create_data_folder(){
        if (!plugin.getDataFolder().exists()){
            new File(plugin.getDataFolder().getPath()).mkdirs();
        }
    }

    private void setLog4JFilter(){
        try{
            Class.forName("org.apache.logging.log4j.core.filter.AbstractFilter");
            org.apache.logging.log4j.core.Logger logger;
            logger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
            logger.addFilter(new Log4JFilter());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        itemStorage.closeConnection();
        LOGGER.info(ChatColor.DARK_PURPLE  + "Ultimate Market " + ChatColor.RESET + "plugin has been " + ChatColor.RED + "disabled!");
    }
}
