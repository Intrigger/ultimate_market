package org.intrigger.ultimate_market.utils.Interface;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.intrigger.ultimate_market.utils.ColorManager.ColorManager;
import org.intrigger.ultimate_market.utils.Interface.Menus.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;

import static org.intrigger.ultimate_market.Ultimate_market.*;

public class GUI {

    public String price;
    public ColorManager cm;

    public Map<String, Menu> menus;
    Map<String, Function<Object[], Menu>> menus_constructor;
    public List<String> menu_titles;
    public Map<String, List<String>> messages;

    public GUI() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException {

        menus = new HashMap<>();
        menus_constructor = new HashMap<>();
        cm = new ColorManager();

        messages = new HashMap<>();
        menu_titles = new ArrayList<>();

        if (!plugin.getDataFolder().exists()){
            new File(plugin.getDataFolder().getPath()).mkdirs();
        }

        if (!new File(plugin.getDataFolder() + "/interfaces").exists()) {
            new File(plugin.getDataFolder().getPath() + "/interfaces").mkdirs();

            if (!new File(plugin.getDataFolder() + "/interfaces/interface_ru.yml").exists()){
                plugin.saveResource("interfaces/interface_ru.yml", true);
            }
        }

        if (!new File(plugin.getDataFolder() + "/item_categories").exists()){
            new File(plugin.getDataFolder().getPath() + "/item_categories").mkdirs();
            if (!new File(plugin.getDataFolder() + "/item_categories/item_categories_ru.yml").exists()){
                plugin.saveResource("item_categories/item_categories_ru.yml", true);
            }
        }

        if (!new File(plugin.getDataFolder() + "/interface.yml").exists()){
            plugin.saveResource("interface.yml", true);
        }
        if (!new File(plugin.getDataFolder() + "/groups_config.yml").exists()){
            plugin.saveResource("groups_config.yml", false);
        }
        if (!new File(plugin.getDataFolder() + "/item_categories.yml").exists()) {
            plugin.saveResource("item_categories.yml", true);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + "/interface.yml"));

        ConfigurationSection menus_section = config.getConfigurationSection("menus");

        if (menus_section == null){
            throw new RuntimeException("interface.yml menus section was null");
        }

        menus_constructor.put("main_menu", args -> new MainMenu((String) args[0], (String) args[1], (Map<String, Button>) args[2]));
        menus_constructor.put("my_shop", args -> new MyShop((String) args[0], (String) args[1], (Map<String, Button>) args[2]));
        menus_constructor.put("tips_menu", args -> new TipsMenu((String) args[0], (String) args[1], (Map<String, Button>) args[2]));
        menus_constructor.put("buy_requests", args -> new BuyRequestsMenu((String) args[0], (String) args[1], (Map<String, Button>) args[2]));
        menus_constructor.put("categories", args -> new CategoriesMenu((String) args[0], (String) args[1], (Map<String, Button>) args[2]));
        menus_constructor.put("confirm_full_menu", args -> new ConfirmFullMenu((String) args[0], (String) args[1], (Map<String, Button>) args[2]));
        menus_constructor.put("select_amount_menu", args -> new SelectAmountMenu((String) args[0], (String) args[1], (Map<String, Button>) args[2]));
        menus_constructor.put("my_buy_requests", args -> new MyBuyRequestsMenu((String) args[0], (String) args[1], (Map<String, Button>) args[2]));
        menus_constructor.put("buy_requests_categories", args -> new BuyRequestsCategoriesMenu((String) args[0], (String) args[1], (Map<String, Button>) args[2]));

        for (String menu_name: menus_section.getKeys(false)){
            String menu_title = menus_section.getConfigurationSection(menu_name).getString("title");
            menu_titles.add(menu_title);
            Map<String, Button> buttons = new HashMap<>();
            ConfigurationSection buttons_section = menus_section.getConfigurationSection(menu_name + ".buttons");
            for (String button: buttons_section.getKeys(false)){
                String button_title = buttons_section.getConfigurationSection(button).getString("title");
                List<String> button_lore = buttons_section.getConfigurationSection(button).getStringList("lore");
                buttons.put(button, new Button(button, button_title, button_lore));
            }
            Object[] objects = {menu_name, menu_title, buttons};
            menus.put(menu_name, menus_constructor.get(menu_name).apply(objects));
        }

        ConfigurationSection messages_section = config.getConfigurationSection("messages");

        if (messages_section == null){
            throw new RuntimeException("interface.yml messages section was null");
        }

        for (String message_name: messages_section.getKeys(false)){
            messages.put(message_name, messages_section.getStringList(message_name));
        }
    }

    public void send_message(Player player, String key){
        for (String temp: gui.messages.get(key)){
            player.sendMessage(gui.cm.format(temp));
        }
    }

    public void send_message(Player player, List<String> strings){
        for (String temp: strings){
            player.sendMessage(gui.cm.format(temp));
        }
    }

    public void send_message(CommandSender commandSender, String key){
        for (String temp: gui.messages.get(key)){
            commandSender.sendMessage(gui.cm.format(temp));
        }
    }

    public void send_message(CommandSender commandSender, List<String> strings){
        for (String temp: strings){
            commandSender.sendMessage(gui.cm.format(temp));
        }
    }
}
