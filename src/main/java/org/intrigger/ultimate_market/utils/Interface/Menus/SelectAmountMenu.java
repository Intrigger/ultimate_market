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
import java.util.stream.IntStream;

import static org.bukkit.Bukkit.getServer;
import static org.intrigger.ultimate_market.Ultimate_market.*;

public class SelectAmountMenu extends Menu {
    public SelectAmountMenu(String _name, String _title, Map<String, Button> _buttons) {
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

        ItemStack main_menu = PutData.put(new ItemStack(Material.CHEST), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "select_amount_menu"),
                new Pair<>("button", "main_menu")
        ));
        ItemMeta main_menu_meta = main_menu.getItemMeta();
        main_menu_meta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("main_menu").title));
        main_menu_meta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("main_menu").lore));
        main_menu.setItemMeta(main_menu_meta);
        inventory.setItem(0, main_menu);

        List<Integer> gray_slots = IntStream.range(1, 18).boxed().collect(Collectors.toList());
        gray_slots.addAll(IntStream.range(27, 45).boxed().collect(Collectors.toList()));

        List<Integer> green_slots = Arrays.asList(23, 24, 25, 26);
        List<Integer> red_slots = Arrays.asList(18, 19, 20, 21);

        int index = 0;
        
        List<Integer> green_amounts = Arrays.asList(1, 4, 8, 16);
        List<Integer> red_amounts = Arrays.asList(-16, -8, -4, -1);
        
        for (Integer green_int: green_slots){
            ItemStack green = PutData.put(new ItemStack(Material.LIME_STAINED_GLASS_PANE), Arrays.asList(
                    new Pair<>("plugin", "ultimate_market"),
                    new Pair<>("menu", "select_amount_menu"),
                    new Pair<>("button", "green"),
                    new Pair<>("amount", String.valueOf(green_amounts.get(index)))
            ));
            ItemMeta green_meta = green.getItemMeta();
            green_meta.displayName(StringDeserializer.deserialize("+" + green_amounts.get(index)));
            green_meta.lore(null);
            green.setItemMeta(green_meta);
            inventory.setItem(green_int, green);
            index++;
        }

        index = 0;

        for (Integer red_int: red_slots){
            ItemStack red = PutData.put(new ItemStack(Material.RED_STAINED_GLASS_PANE), Arrays.asList(
                    new Pair<>("plugin", "ultimate_market"),
                    new Pair<>("menu", "select_amount_menu"),
                    new Pair<>("button", "red"),
                    new Pair<>("amount", String.valueOf(red_amounts.get(index)))
            ));

            ItemMeta red_meta = red.getItemMeta();
            red_meta.displayName(StringDeserializer.deserialize(String.valueOf(red_amounts.get(index))));
            red_meta.lore(null);
            red.setItemMeta(red_meta);
            inventory.setItem(red_int, red);
            index++;
        }

        index = 0;

        for (Integer gray_int: gray_slots){
            ItemStack gray = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);

            ItemMeta gray_meta = gray.getItemMeta();
            gray_meta.displayName(StringDeserializer.deserialize(""));
            gray_meta.lore(null);
            gray.setItemMeta(gray_meta);
            inventory.setItem(gray_int, gray);
            index++;
        }

        ItemStackNotation itemStackNotation = itemStorage.getItem(info.current_buying_item.get(playerName));
        ItemStack item = PutData.put(ItemStack.deserializeBytes(itemStackNotation.bytes), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "select_amount_menu")
        ));

        item.setAmount(info.current_buying_item_amount.get(playerName));
        inventory.setItem(22, item);

        ItemStack confirmButton = PutData.put(new ItemStack(Material.SLIME_BALL), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "select_amount_menu"),
                new Pair<>("button", "confirm")
        ));

        ItemMeta confirmButtonMeta = confirmButton.getItemMeta();
        confirmButtonMeta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("confirm").title));
        confirmButtonMeta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("confirm").lore));
        confirmButton.setItemMeta(confirmButtonMeta);
        inventory.setItem(40, confirmButton);

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
                case "green":
                case "red": {
                    String amount_key = pdc.get(new NamespacedKey(plugin, "amount"), PersistentDataType.STRING);
                    info.current_buying_item_amount.put(playerName,
                        Math.max(1,
                            Math.min(info.current_buying_item_amount.get(playerName) + Integer.parseInt(amount_key),
                                    itemStorage.getItem(info.current_buying_item.get(playerName)).amount)
                        )
                    );
                    player.openInventory(gui.menus.get("select_amount_menu").generateInventory(playerName));
                    break;
                }
                case "main_menu":{
                    info.current_menu.put(playerName, "main_menu");
                    player.openInventory(gui.menus.get("main_menu").generateInventory(playerName));
                    break;
                }
                case "confirm":{
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
                    int item_amount = info.current_buying_item_amount.get(playerName);

                    ItemStack newItem = ItemStack.deserializeBytes(notation.bytes);
                    newItem.setAmount(item_amount);
                    ItemMeta meta = newItem.getItemMeta();
                    pdc = meta.getPersistentDataContainer();
                    NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
                    pdc.remove(namespacedKey);
                    newItem.setItemMeta(meta);

                    double price = notation.price * info.current_buying_item_amount.get(playerName);

                    if (notation.amount < item_amount){
                        gui.send_message(player, "item_already_sold");
                        info.current_menu.put(playerName, "main_menu");
                        player.openInventory(gui.menus.get("main_menu").generateInventory(playerName));
                        return;
                    }

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
                            slot = -1;
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
                    parse_args.put("{AMOUNT}", String.valueOf(info.current_buying_item_amount.get(playerName)));
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

                    itemStorage.changeAmountBy(unique_key, -info.current_buying_item_amount.get(playerName));

                    if (notation.amount - info.current_buying_item_amount.get(playerName) == 0){
                        itemStorage.removeItem(unique_key);
                    }

                    info.current_menu.put(playerName, "main_menu");
                    player.openInventory(gui.menus.get("main_menu").generateInventory(playerName));
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
