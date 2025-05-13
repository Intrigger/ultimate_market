package org.intrigger.ultimate_market.commands;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.intrigger.ultimate_market.Ultimate_market;
import org.intrigger.ultimate_market.utils.*;
import org.jetbrains.annotations.NotNull;
import net.milkbowl.vault.economy.Economy;

import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getServer;
import static org.intrigger.ultimate_market.Ultimate_market.*;

public class MarketExecutor implements CommandExecutor  {

    private static Plugin plugin;
    public MarketExecutor(Plugin _plugin){
        plugin = _plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player)){
            gui.send_message(commandSender, "command_can_be_only_used_by_player");
            return true;
        }

        Player player = (Player) commandSender;
        String playerName = player.getName();

        if (Bukkit.getPluginManager().isPluginEnabled("OmniLegacyEvolution")){
            if (player.hasPermission("OmniLegacyEvo.stage.1") && (!player.isOp())){
                gui.send_message(player, "omni_legacy_evo_low_level");
                return true;
            }
        }

        if (strings.length == 0){
            info.current_menu.put(playerName, "main_menu");
            info.current_market_page.put(playerName, 0);
            info.current_sorting_type.put(playerName, "NEW_FIRST");
            info.current_item_filter.put(playerName, null);
            info.current_buy_requests_page.put(playerName, 0);
            info.current_buy_requests_item_filter.put(playerName, null);
            info.current_buy_requests_sorting_type.put(playerName, "NEW_FIRST");
            info.current_my_shop_page.put(playerName, 0);
            player.openInventory(Ultimate_market.gui.menus.get("main_menu").generateInventory(playerName));
        }
        else if (strings.length == 1) {
            if (!MarketTabComplete.list.get(0).contains(strings[0])){
                for (String temp: gui.messages.get("wrong_command_usage")){
                    player.sendMessage(gui.cm.format(temp));
                }
            }
            else{
                if (strings[0].equalsIgnoreCase("sell")){
                    for (String temp: gui.messages.get("specify_the_price")){
                        player.sendMessage(gui.cm.format(temp));
                    }
                }
                else if (strings[0].equalsIgnoreCase("buy")){
                    for (String temp: gui.messages.get("wrong_item_amount_specified")){
                        player.sendMessage(gui.cm.format(temp));
                    }
                }
            }
        }
        else if (strings.length == 2){
            if (strings[0].equalsIgnoreCase("sell")){
                ItemStack itemToSell = player.getItemInHand();

                if (itemToSell.getTranslationKey().equals("block.minecraft.air")){
                    for (String temp: gui.messages.get("the_item_to_be_sold_must_be_held_in_your_hand")){
                        player.sendMessage(gui.cm.format(temp));
                    }
                    return true;
                }

                String priceStr = strings[1];

                boolean valid_input = NumberUtil.isValidIntegerUnderTrillion(priceStr);

                if (!valid_input){
                    gui.send_message(player, "incorrect_input");
                    return true;
                }

                double price = Double.parseDouble(priceStr);


                String playerGroup = "default";

                ArrayList<String> groups = new ArrayList<>(groupsPermissions.maxItemsToSell.keySet());

                for (int i = groups.size() - 1; i >= 0; i--){
                    String group = groups.get(i);
                    if (player.hasPermission("group." + group)){
                        playerGroup = group;
                        break;
                    }
                }


                if (itemStorage.playerItemsSoldNow(playerName) >= groupsPermissions.maxItemsToSell.get(playerGroup)){
                    for (String temp: gui.messages.get("items_sold_limit_reached")){
                        player.sendMessage(gui.cm.format(temp));
                    }
                    return true;
                }

                String unique_key = new Random().ints('a', 'z' + 1).limit(64).collect(StringBuilder::new,
                        StringBuilder::appendCodePoint, StringBuilder::append).toString();

                ItemMeta meta = itemToSell.getItemMeta();

                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
                pdc.set(namespacedKey, PersistentDataType.STRING, unique_key);

                itemToSell.setItemMeta(meta);

                int sell_full = 0;

                itemStorage.addItem(unique_key, player.getName(), (price / itemToSell.getAmount()), System.nanoTime(), itemToSell.getType().toString(), itemToSell, itemToSell.getAmount(), sell_full);

                player.getItemInHand().setAmount(0);
                for (String temp: gui.messages.get("successful_putting_up")){
                    player.sendMessage(gui.cm.format(temp));
                }

                if (price == 0){
                    for (String temp: gui.messages.get("zero_price_notice")){
                        player.sendMessage(gui.cm.format(temp));
                    }
                }
            }
            else if (strings[0].equalsIgnoreCase("buy")){
                String amountStr = strings[1];

                if (NumberUtil.isValidIntegerUnderTrillion(amountStr)){
                    gui.send_message(player, "incorrect_input");
                    return true;
                }

                for (String temp: gui.messages.get("specify_the_price")){
                    player.sendMessage(gui.cm.format(temp));
                    return true;
                }
            }
        }
        else if (strings.length == 3){
            if (strings[0].equalsIgnoreCase("sell")){
                String arg3 = strings[2];
                if (!MarketTabComplete.list.get(2).contains(arg3)){
                    for (String temp: gui.messages.get("wrong_command_usage")){
                        player.sendMessage(gui.cm.format(temp));
                    }
                    return true;
                }

                if (player.getItemInHand().getTranslationKey().equals("block.minecraft.air")){
                    for (String temp: gui.messages.get("the_item_to_be_bought_must_be_held_in_your_hand")){
                        player.sendMessage(gui.cm.format(temp));
                    }
                    return true;
                }

                String priceStr = strings[1];

                boolean valid_input = NumberUtil.isValidIntegerUnderTrillion(priceStr);

                if (!valid_input){
                    gui.send_message(player, "incorrect_input");
                    return true;
                }


                ItemStack itemToSell = player.getItemInHand();

                if (player.getItemInHand().getTranslationKey().equals("block.minecraft.air")){
                    for (String temp: gui.messages.get("the_item_to_be_sold_must_be_held_in_your_hand")){
                        player.sendMessage(gui.cm.format(temp));
                    }
                    return true;
                }

                String playerGroup = "default";

                ArrayList<String> groups = new ArrayList<>(groupsPermissions.maxItemsToSell.keySet());

                for (int i = groups.size() - 1; i >= 0; i--){
                    String group = groups.get(i);
                    if (player.hasPermission("group." + group)){
                        playerGroup = group;
                        break;
                    }
                }

                if (itemStorage.playerItemsSoldNow(playerName) >= groupsPermissions.maxItemsToSell.get(playerGroup)){
                    for (String temp: gui.messages.get("items_sold_limit_reached")){
                        player.sendMessage(gui.cm.format(temp));
                    }
                    return true;
                }

                double price = Math.ceil(Double.parseDouble(priceStr));

                String unique_key = new Random().ints('a', 'z' + 1).limit(64).collect(StringBuilder::new,
                        StringBuilder::appendCodePoint, StringBuilder::append).toString();

                ItemMeta meta = itemToSell.getItemMeta();

                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
                pdc.set(namespacedKey, PersistentDataType.STRING, unique_key);

                itemToSell.setItemMeta(meta);

                int sell_full = arg3.equalsIgnoreCase("full") ? 1 : 0;

                itemStorage.addItem(unique_key, player.getName(), sell_full == 1 ? price : price / itemToSell.getAmount(), System.nanoTime(), itemToSell.getType().toString(), itemToSell, itemToSell.getAmount(), sell_full);

                player.getItemInHand().setAmount(0);
                for (String temp: gui.messages.get("successful_putting_up")){
                    player.sendMessage(gui.cm.format(temp));
                }

                if (price == 0){
                    for (String temp: gui.messages.get("zero_price_notice")){
                        player.sendMessage(gui.cm.format(temp));
                    }
                }
            }
            else if (strings[0].equalsIgnoreCase("buy")){

                ItemStack itemToSell = player.getItemInHand().clone();

                if (itemToSell.getTranslationKey().equals("block.minecraft.air")){
                    for (String temp: gui.messages.get("the_item_to_be_bought_must_be_held_in_your_hand")){
                        player.sendMessage(gui.cm.format(temp));
                    }
                    return true;
                }
                String amount_str = strings[1];
                String price_str = strings[2];

                boolean valid_input = (NumberUtil.isValidIntegerUnderTrillion(amount_str)) && (NumberUtil.isValidIntegerUnderTrillion(price_str));

                if (!valid_input){
                    gui.send_message(player, "incorrect_input");
                    return true;
                }

                double priceDouble = Double.parseDouble(price_str);

                String playerGroup = "default";

                ArrayList<String> groups = new ArrayList<>(groupsPermissions.maxBuyRequests.keySet());

                for (int i = groups.size() - 1; i >= 0; i--){
                    String group = groups.get(i);
                    if (player.hasPermission("group." + group)){
                        playerGroup = group;
                        break;
                    }
                }

                if (Ultimate_market.buyRequestStorage.getAllBuyRequests(playerName) >= groupsPermissions.maxBuyRequests.get(playerGroup)){
                    for (String temp: gui.messages.get("buy_requests_limit_reached")){
                        player.sendMessage(gui.cm.format(temp));
                    }
                    return true;
                }

                if (Long.parseLong(amount_str) > groupsPermissions.maxBuyAmount.get(playerGroup)){
                    gui.send_message(player, "cannot_request_that_many_items");
                    return true;
                }

                //TODO Ограничить максимальное возможное значение для amount_str
                //System.out.println("Игрок " + playerName + " хочет купить " + itemToSell.getTranslationKey() + " в кол-во " + amount_str + " за " + priceDouble);

                String unique_key = new Random().ints('a', 'z' + 1).limit(64).collect(StringBuilder::new,
                        StringBuilder::appendCodePoint, StringBuilder::append).toString();

                itemToSell.setAmount(1);

                BuyRequestNotation buyRequestNotation = new BuyRequestNotation(unique_key, System.nanoTime(), playerName, priceDouble, itemToSell.getType().toString(), itemToSell.serializeAsBytes(), 0, 0, Integer.parseInt(amount_str));

                if (getServer().getPluginManager().getPlugin("Vault") == null) {
                    return true;
                }
                RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
                if (rsp == null) {
                    return true;
                }

                rsp = getServer().getServicesManager().getRegistration(Economy.class);

                if (rsp == null) {
                    return true;
                }

                long balance = (long) rsp.getProvider().getBalance(playerName);

                if (balance < buyRequestNotation.price * buyRequestNotation.amount_total){
                    for (String temp: gui.messages.get("not_enough_money_buy_requests")){
                        player.sendMessage(gui.cm.format(temp));
                    }
                    return true;
                }

                rsp.getProvider().withdrawPlayer(playerName, buyRequestNotation.price * buyRequestNotation.amount_total);

                Ultimate_market.buyRequestStorage.addRequest(buyRequestNotation);

                for (String temp: gui.messages.get("successful_creating_of_buy_request")){
                    player.sendMessage(gui.cm.format(temp));
                }
            }
            else {
                gui.send_message(player, "wrong_command_usage");
            }
        }
        return true;
    }
}
