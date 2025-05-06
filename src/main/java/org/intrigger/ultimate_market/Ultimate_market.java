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
import java.util.Objects;
import java.util.logging.Logger;

public final class Ultimate_market extends JavaPlugin {


    /*
    * 2. TODO Добавить автосдачу на поставки. При нажатии кнопки игрок сдает весь инвентарь на самые выгодные для
    *     каждого из предметов поставки, если такие есть
    * 3. TODO Исправить localized_strings для "Вы успешно купили...", добавить игрока, у которого ты покупаешь товар
    * 4. TODO При поставке, если ты хочешь сдать товар, которого у тебя нет, ничего не происходит (нужно вывести ошибку)
    * 5. TODO Добавить сдачу при поставке: 64 шт, сдать весь имеющийся товар
    * 6. TODO Добавить игроку, получающему товары при поставке (если он онлайн), уведомление о получении товара
    * 7. TODO Добавить поиск поставок на нужный товар
    */

    public static Logger LOGGER;

    public static Plugin plugin;
    public static MarketExecutor marketExecutor;

    @Override
    public void onEnable() {
        LOGGER = getLogger();

        LOGGER.info(ChatColor.DARK_PURPLE  + "Ultimate Market " + ChatColor.RESET + "plugin has been " + ChatColor.GREEN + "enabled!");

        plugin = this;

        marketExecutor = new MarketExecutor(this);

        Objects.requireNonNull(getCommand("ah")).setExecutor(marketExecutor);
        Objects.requireNonNull(getCommand("ah")).setTabCompleter(new MarketTabComplete());

        new ClickHandler(this, marketExecutor);

        Metrics metrics = new Metrics(this, 18923);
        setLog4JFilter();
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
        marketExecutor.closeDatabase();
        LOGGER.info(ChatColor.DARK_PURPLE  + "Ultimate Market " + ChatColor.RESET + "plugin has been " + ChatColor.RED + "disabled!");
    }
}
