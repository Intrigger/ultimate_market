package org.intrigger.ultimate_market.utils.Interface.Menus;

import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.intrigger.ultimate_market.Ultimate_market;
import org.intrigger.ultimate_market.utils.BuyRequestNotation;
import org.intrigger.ultimate_market.utils.Interface.Button;
import org.intrigger.ultimate_market.utils.Interface.Menu;
import org.intrigger.ultimate_market.utils.ItemUtils.PutData;
import org.intrigger.ultimate_market.utils.NumberUtil;
import org.intrigger.ultimate_market.utils.Pair;

import java.util.*;

import static org.bukkit.Bukkit.getServer;
import static org.intrigger.ultimate_market.Ultimate_market.*;

public class MyBuyRequestsMenu extends Menu {
    public MyBuyRequestsMenu(String _name, String _title, Map<String, Button> _buttons) {
        super(_name, _title, _buttons);
    }

    @Override
    public Inventory generateInventory(String playerName) {
        int inventorySize = 54;
        Inventory inventory = Bukkit.createInventory(null,
                inventorySize,
                gui.cm.parseColoredText(
                        gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).title
                )

        );

        ItemStack main_menu = PutData.put(new ItemStack(Material.CHEST), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "my_buy_requests"),
                new Pair<>("button", "main_menu")
        ));
        ItemMeta main_menu_meta = main_menu.getItemMeta();
        main_menu_meta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("main_menu").title));
        main_menu_meta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("main_menu").lore));
        main_menu.setItemMeta(main_menu_meta);
        inventory.setItem(0, main_menu);

        
        ItemStack buy_requests = PutData.put(new ItemStack(Material.CHEST_MINECART), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "my_buy_requests"),
                new Pair<>("button", "buy_requests")
        ));
        ItemMeta buy_requests_meta = buy_requests.getItemMeta();
        buy_requests_meta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("buy_requests").title));
        buy_requests_meta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("buy_requests").lore));
        buy_requests.setItemMeta(buy_requests_meta);
        inventory.setItem(2, buy_requests);


        ItemStack page_left = PutData.put(new ItemStack(Material.PAPER), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "my_buy_requests"),
                new Pair<>("button", "page_left")
        ));
        ItemMeta page_left_meta = page_left.getItemMeta();
        page_left_meta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("page_left").title));
        page_left_meta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("page_left").lore));
        page_left.setItemMeta(page_left_meta);
        inventory.setItem(3, page_left);


        ItemStack update_page = PutData.put(new ItemStack(Material.SLIME_BALL), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "my_buy_requests"),
                new Pair<>("button", "update_page")
        ));
        ItemMeta update_page_meta = update_page.getItemMeta();
        update_page_meta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("update_page").title));
        update_page_meta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("update_page").lore));
        update_page.setItemMeta(update_page_meta);
        inventory.setItem(4, update_page);


        ItemStack page_right = PutData.put(new ItemStack(Material.PAPER), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "my_buy_requests"),
                new Pair<>("button", "page_right")
        ));
        ItemMeta page_right_meta = page_right.getItemMeta();
        page_right_meta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("page_right").title));
        page_right_meta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("page_right").lore));
        page_right.setItemMeta(page_right_meta);
        inventory.setItem(5, page_right);


        ArrayList<BuyRequestNotation> buyRequests = buyRequestStorage.getAllBuyRequests(playerName, info.current_my_buy_requests_page.get(playerName));


        int currentSlot = 9;
        int br_size = buyRequests.size();

        for (int i = 0; i < br_size; i++) {
            if (currentSlot > 53) break;
            BuyRequestNotation requestNotation = buyRequests.get(i);
            Map<String, String> parse_args = new HashMap<>();

            parse_args.put("{CURRENCY}", gui.messages.get("currency_symbol").get(0));
            parse_args.put("{AMOUNT_NOW}", String.valueOf(requestNotation.amount_now));
            parse_args.put("{AMOUNT_TOTAL}", String.valueOf(requestNotation.amount_total));
            parse_args.put("{AVAILABLE}", String.valueOf(requestNotation.amount_now - requestNotation.amount_taken));
            parse_args.put("{PRICE}", NumberUtil.formatClean(requestNotation.price, 2));
            inventory.setItem(currentSlot, prepare_goods_item(requestNotation, parse_args));
            currentSlot++;
        }

        return inventory;
    }

    private ItemStack prepare_goods_item(BuyRequestNotation requestNotation, Map<String, String> parse_args) {
        ItemStack currentItemStack = PutData.put(ItemStack.deserializeBytes(requestNotation.bytes), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "my_buy_requests"),
                new Pair<>("key", "request"),
                new Pair<>("unique_key", requestNotation.key)
        ));
        //
        List<Component> current_lore = currentItemStack.getItemMeta().lore();
        List<String> original_lore = new ArrayList<>(gui.menus.get("my_buy_requests").buttons.get("request").lore);
        List<String> new_lore = new ArrayList<>();

        for (String s : original_lore) {
            new_lore.add(parse(s, parse_args));
        }
        if (current_lore == null) current_lore = new ArrayList<>();
        current_lore.addAll(gui.cm.parseColoredText(new_lore));
        currentItemStack.lore(current_lore);
        ItemMeta meta = currentItemStack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        currentItemStack.setItemMeta(meta);
        return currentItemStack;
    }

    private String parse(String s, Map<String, String> parse_args) {
        String res = s;
        for (String i : parse_args.keySet()) {
            res = res.replace(i, parse_args.get(i));
        }
        return res;
    }

    @Override
    public void onItemClick(InventoryClickEvent event) {
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
                case "main_menu": {
                    info.current_menu.put(playerName, "main_menu");
                    player.openInventory(gui.menus.get("main_menu").generateInventory(playerName));
                    break;
                }
                case "buy_requests":{
                    info.current_menu.put(playerName, "buy_requests");
                    player.openInventory(gui.menus.get("buy_requests").generateInventory(playerName));
                    break;
                }
                case "update_page":{
                    info.current_menu.put(playerName, "my_buy_requests");
                    player.openInventory(gui.menus.get("my_buy_requests").generateInventory(playerName));
                    break;
                }
                case "page_left":
                    info.current_my_buy_requests_page.put(playerName, Math.max(0, Math.min(info.current_my_buy_requests_page.get(playerName) - 1, buyRequestStorage.getPagesNum(playerName))));
                    player.openInventory(gui.menus.get("my_buy_requests").generateInventory(playerName));
                    break;
                case "page_right":
                    info.current_my_buy_requests_page.put(playerName, Math.max(0, Math.min(info.current_my_buy_requests_page.get(playerName) + 1, buyRequestStorage.getPagesNum(playerName))));
                    player.openInventory(gui.menus.get("my_buy_requests").generateInventory(playerName));
                    break;
            }
        } else if (pdc.has(new NamespacedKey(plugin, "key"), PersistentDataType.STRING)) {
            String key = pdc.get(new NamespacedKey(plugin, "key"), PersistentDataType.STRING);
            assert key != null;
            if (key.equalsIgnoreCase("request")) {
                if (pdc.get(new NamespacedKey(plugin, "unique_key"), PersistentDataType.STRING) != null) {
                    String unique_key = pdc.get(new NamespacedKey(plugin, "unique_key"), PersistentDataType.STRING);
                    if (unique_key == null) return;

                    BuyRequestNotation buyRequestNotation = buyRequestStorage.getBuyRequest(unique_key);

                    if (buyRequestNotation == null) return;
                    if (event.isLeftClick() && (!event.isShiftClick())) {// Если это чисто ЛКМ
                        boolean hasEmptySlot = false;
                        if (buyRequestNotation.amount_taken + 1 > buyRequestNotation.amount_now){
                            gui.send_message(player, "no_items_to_withdraw");
                            return;
                        }
                        ItemStack newItem = ItemStack.deserializeBytes(buyRequestNotation.bytes);
                        ItemMeta meta = newItem.getItemMeta();

                        pdc = meta.getPersistentDataContainer();
                        NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
                        pdc.remove(namespacedKey);
                        newItem.setItemMeta(meta);

                        for (int slot = 35; slot >= 0; slot--) {
                            if (player.getInventory().getItem(slot) == null) {
                                buyRequestStorage.updateAmountTaken(unique_key, 1);

                                player.getInventory().setItem(slot, newItem);
                                gui.send_message(player, "you_took_one_item_from_buy_requests");
                                player.openInventory(gui.menus.get("my_buy_requests").generateInventory(playerName));
                                slot = -1;
                                hasEmptySlot = true;
                            }
                        }

                        if (!hasEmptySlot) {
                            gui.send_message(player, "free_up_your_inventory_space");
                        }
                    }
                    else if (event.isLeftClick()) { //shift + ЛКМ = снять все что есть
                        int empty_slots = 0;
                        for (int slot = 35; slot >= 0; slot--) {
                            if (player.getInventory().getItem(slot) == null) {
                                empty_slots++;
                            }
                        }
                        if (empty_slots == 0){
                            gui.send_message(player, "free_up_your_inventory_space");
                            return;
                        }

                        ItemStack newItem = ItemStack.deserializeBytes(buyRequestNotation.bytes);
                        ItemMeta meta = newItem.getItemMeta();
                        pdc = meta.getPersistentDataContainer();
                        NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
                        pdc.remove(namespacedKey);
                        newItem.setItemMeta(meta);

                        int max_stack_size = newItem.getMaxStackSize();
                        int max_can_be_taken = max_stack_size * empty_slots;
                        int available = buyRequestNotation.amount_now - buyRequestNotation.amount_taken;

                        if (available == 0){
                            gui.send_message(player, "no_items_to_withdraw");
                            return;
                        }

                        int to_be_taken = Math.min(available, max_can_be_taken);

                        for (int slot = 35; slot >= 0; slot--) {
                            if (player.getInventory().getItem(slot) == null) {
                                newItem.setAmount(Math.min(max_stack_size, to_be_taken));

                                buyRequestStorage.updateAmountTaken(unique_key, Math.min(max_stack_size, to_be_taken));

                                player.getInventory().setItem(slot, newItem);
                                to_be_taken -= Math.min(max_stack_size, to_be_taken);

                                if (to_be_taken == 0) slot = -1;
                            }
                        }
                        player.openInventory(gui.menus.get("my_buy_requests").generateInventory(playerName));
                    }
                    else if (event.isRightClick() && event.isShiftClick()){
                        int available = buyRequestNotation.amount_now - buyRequestNotation.amount_taken;

                        ItemStack newItem = ItemStack.deserializeBytes(buyRequestNotation.bytes);
                        ItemMeta meta = newItem.getItemMeta();
                        pdc = meta.getPersistentDataContainer();
                        NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
                        pdc.remove(namespacedKey);
                        newItem.setItemMeta(meta);

                        int max_stack_size = newItem.getMaxStackSize();



                        if (getServer().getPluginManager().getPlugin("Vault") == null) {
                            return;
                        }
                        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
                        if (rsp == null) {
                            return;
                        }

                        rsp = getServer().getServicesManager().getRegistration(Economy.class);

                        if (rsp == null) {
                            return;
                        }

                        rsp.getProvider().depositPlayer(playerName, (buyRequestNotation.amount_total - available) * buyRequestNotation.price);


                        Map<String, String> parse_args = new HashMap<>();
                        parse_args.put("{MONEY}", NumberUtil.formatClean((buyRequestNotation.amount_total - available) * buyRequestNotation.price, 2));
                        parse_args.put("{CURRENCY}", gui.messages.get("currency_symbol").get(0));

                        List<String> msg = new ArrayList<>();
                        for (String temp: gui.messages.get("returned_money")){
                            msg.add(parse(temp, parse_args));
                        }
                        gui.send_message(player, msg);

                        while (available != 0){
                            ItemStack itemCopy = newItem.clone();
                            itemCopy.setAmount(Math.min(available, max_stack_size));

                            available -= Math.min(available, max_stack_size);
                            player.getWorld().dropItem(player.getLocation(), itemCopy);
                        }

                        buyRequestStorage.finishBuyRequest(unique_key);
                        player.openInventory(gui.menus.get("my_buy_requests").generateInventory(playerName));
                    }
                }
            }
        }
    }
}
