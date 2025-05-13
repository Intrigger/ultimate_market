package org.intrigger.ultimate_market.utils.Interface.Menus;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.intrigger.ultimate_market.Ultimate_market;
import org.intrigger.ultimate_market.utils.Interface.Button;
import org.intrigger.ultimate_market.utils.Interface.Menu;
import org.intrigger.ultimate_market.utils.ItemStackNotation;
import org.intrigger.ultimate_market.utils.ItemUtils.PutData;
import org.intrigger.ultimate_market.utils.NumberUtil;
import org.intrigger.ultimate_market.utils.Pair;
import org.intrigger.ultimate_market.utils.StringDeserializer;

import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getServer;
import static org.intrigger.ultimate_market.Ultimate_market.*;

public class ConfirmFullMenu extends Menu {
    public ConfirmFullMenu(String _name, String _title, Map<String, Button> _buttons) {
        super(_name, _title, _buttons);
    }

    @Override
    public Inventory generateInventory(String playerName) {
        int inventorySize = 45;
        Inventory inventory = Bukkit.createInventory(null,
                inventorySize, gui.cm.parseColoredText(
                                gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).title
                        )

        );

        List<Integer> gray_slots = Arrays.asList(
                0, 1, 2, 3, 4, 5, 6, 7, 8,
                36, 37, 38, 39, 40, 41, 42, 43, 44,
                13, 31
                );

        List<Integer> green_slots = Arrays.asList(
                9, 10, 11, 12,
                18, 19, 20, 21,
                27, 28, 29, 30
        );

        List<Integer> red_slots = Arrays.asList(
                14, 15, 16, 17,
                23, 24, 25, 26,
                32, 33, 34, 35
        );

        ItemStack gray = PutData.put(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "confirm_full_menu"),
                new Pair<>("button", "gray")
        ));
        ItemMeta gray_meta = gray.getItemMeta();
        gray_meta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("gray").title));
        gray_meta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("gray").lore));
        gray.setItemMeta(gray_meta);

        ItemStack green = PutData.put(new ItemStack(Material.LIME_STAINED_GLASS_PANE), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "confirm_full_menu"),
                new Pair<>("button", "green")
        ));
        ItemMeta green_meta = green.getItemMeta();
        green_meta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("green").title));
        green_meta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("green").lore));
        green.setItemMeta(green_meta);

        ItemStack red = PutData.put(new ItemStack(Material.RED_STAINED_GLASS_PANE), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "confirm_full_menu"),
                new Pair<>("button", "red")
        ));
        ItemMeta red_meta = red.getItemMeta();
        red_meta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("red").title));
        red_meta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("red").lore));
        red.setItemMeta(red_meta);
        
        for (Integer i : gray_slots){
            inventory.setItem(i, gray);
        }
        for (Integer i : green_slots){
            inventory.setItem(i, green);
        }
        for (Integer i : red_slots){
            inventory.setItem(i, red);
        }

        ItemStackNotation the_item_notation = itemStorage.getItem(info.current_buying_item.get(playerName));
        ItemStack the_item = ItemStack.deserializeBytes(the_item_notation.bytes);
        the_item.setAmount(the_item_notation.amount);


        inventory.setItem(22, the_item);

        return inventory;
    }

    @Override
    public void onItemClick(InventoryClickEvent event){
        String playerName = event.getWhoClicked().getName();
        Player player = Bukkit.getPlayer(playerName);

        if (player == null) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;
        PersistentDataContainer pdc = clickedItem.getItemMeta().getPersistentDataContainer();
        if (pdc.has(new NamespacedKey(plugin, "button"), PersistentDataType.STRING)) {
            String menu_item_key = pdc.get(new NamespacedKey(plugin, "button"), PersistentDataType.STRING);
            assert menu_item_key != null;

            switch (menu_item_key) {
                case "green": {
                    if (getServer().getPluginManager().getPlugin("Vault") == null) {
                        throw new RuntimeException("Bukkit.getServer().getPluginManager().getPlugin(\"Vault\") was null");
                    }
                    RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
                    if (rsp == null) {
                        throw new RuntimeException("Bukkit.getServer().getServicesManager().getRegistration(Economy.class) was null");
                    }

                    long balance = (long) rsp.getProvider().getBalance(playerName);

                    ItemStackNotation notation = itemStorage.getItem(info.current_buying_item.get(playerName));
                    if (notation == null){
                        gui.send_message(player, "item_already_sold");
                        info.current_menu.put(playerName, "main_menu");
                        player.openInventory(gui.menus.get("main_menu").generateInventory(playerName));
                        return;
                    }
                    String unique_key = notation.key;
                    ItemStack newItem = ItemStack.deserializeBytes(notation.bytes);
                    ItemMeta meta = newItem.getItemMeta();
                    pdc = meta.getPersistentDataContainer();
                    NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
                    pdc.remove(namespacedKey);
                    newItem.setItemMeta(meta);

                    double price = (notation.full == 1) ? notation.price : notation.price * notation.amount;

                    if (notation.amount < info.current_buying_item_amount.get(playerName)){
                        gui.send_message(player, "item_already_sold");
                        info.current_menu.put(playerName, "main_menu");
                        player.openInventory(gui.menus.get("main_menu").generateInventory(playerName));
                        return;
                    }

                    newItem.setAmount(info.current_buying_item_amount.get(playerName));

                    if (balance < price){
                        gui.send_message(player, "not_enough_money_to_buy");
                        info.current_menu.put(playerName, "main_menu");
                        player.openInventory(gui.menus.get("main_menu").generateInventory(playerName));
                        return;
                    }

                    boolean hasEmptySlot = false;

                    for (int slot = 0; slot <= 35; slot++) {
                        if (player.getInventory().getItem(slot) == null) {
                            player.getInventory().setItem(slot, newItem);
                            slot = 36;
                            hasEmptySlot = true;
                        }
                    }

                    if (!hasEmptySlot) {
                        gui.send_message(player, "free_up_your_inventory_space");
                        info.current_menu.put(playerName, "main_menu");
                        player.openInventory(gui.menus.get("main_menu").generateInventory(playerName));
                        return;
                    }
                    rsp = getServer().getServicesManager().getRegistration(Economy.class);
                    String itemOwner = notation.owner;

                    rsp.getProvider().withdrawPlayer(playerName, price                                                                                                                                                                                                      );
                    rsp.getProvider().depositPlayer(itemOwner, price);

                    String item_name = newItem.getItemMeta().getDisplayName().isEmpty() ? newItem.getI18NDisplayName() : newItem.getItemMeta().getDisplayName();

                    Map<String, String> parse_args = new HashMap<>();
                    parse_args.put("{PLAYER}", playerName);
                    parse_args.put("{ITEM}", item_name);
                    parse_args.put("{AMOUNT}", String.valueOf(notation.amount));
                    parse_args.put("{PRICE}", NumberUtil.formatClean(price, 2));
                    parse_args.put("{CURRENCY}", gui.messages.get("currency_symbol").get(0));

                    if (getServer().getOnlinePlayers().contains(getServer().getPlayer(itemOwner))){
                        Player item_owner = getServer().getPlayerExact(itemOwner);
                        if (item_owner != null){
                            List<String> msg = new ArrayList<>();
                            for (String temp: gui.messages.get("item_was_bought_notification_seller")){
                                msg.add(parse(temp, parse_args));
                            }
                            gui.send_message(item_owner, msg);
                        }
                    }

                    parse_args.put("{PLAYER}", itemOwner);

                    List<String> msg = new ArrayList<>();
                    for (String temp: gui.messages.get("item_was_bought_notification_buyer")){
                        msg.add(parse(temp, parse_args));
                    }
                    gui.send_message(player, msg);

                    itemStorage.removeItem(unique_key);
                    info.current_menu.put(playerName, "main_menu");
                    player.openInventory(gui.menus.get("main_menu").generateInventory(playerName));
                    break;
                }
                case "red": {
                    info.current_menu.put(playerName, "main_menu");
                    player.openInventory(gui.menus.get("main_menu").generateInventory(playerName));
                    break;
                }
            }
        }
    }

    private String parse(String s, Map<String, String> parse_args){
        String res = s;
        for (String i: parse_args.keySet()){
            res = res.replace(i, parse_args.get(i));
        }
        return res;
    }
}
