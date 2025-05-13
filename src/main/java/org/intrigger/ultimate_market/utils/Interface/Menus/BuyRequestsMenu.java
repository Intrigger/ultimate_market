package org.intrigger.ultimate_market.utils.Interface.Menus;

import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.checkerframework.checker.units.qual.A;
import org.intrigger.ultimate_market.Ultimate_market;
import org.intrigger.ultimate_market.utils.*;
import org.intrigger.ultimate_market.utils.Interface.Button;
import org.intrigger.ultimate_market.utils.Interface.Menu;
import org.intrigger.ultimate_market.utils.ItemUtils.PutData;

import java.util.*;

import static org.bukkit.Bukkit.getServer;
import static org.intrigger.ultimate_market.Ultimate_market.*;
import static org.intrigger.ultimate_market.Ultimate_market.info;
import static org.intrigger.ultimate_market.Ultimate_market.plugin;

public class BuyRequestsMenu extends Menu {
    public BuyRequestsMenu(String _name, String _title, Map<String, Button> _buttons) {
        super(_name, _title, _buttons);
    }

    @Override
    public Inventory generateInventory(String playerName) {
        int inventorySize = 54;
        Inventory inventory = Bukkit.createInventory(null,
                inventorySize,
                gui.cm.parseColoredText(
                        gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).title +
                                ((info.current_buy_requests_item_filter.get(playerName) == null) ? "" :
                                        ": " + itemCategoriesProcessor.display_names.get(Ultimate_market.info.current_buy_requests_item_filter.get(playerName))
                                )
                )

        );

        ItemStack main_menu = PutData.put(new ItemStack(Material.CHEST), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "buy_requests"),
                new Pair<>("button", "main_menu")
        ));
        ItemMeta main_menu_meta = main_menu.getItemMeta();
        main_menu_meta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("main_menu").title));
        main_menu_meta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("main_menu").lore));
        main_menu.setItemMeta(main_menu_meta);
        inventory.setItem(0, main_menu);


        ItemStack my_buy_requests = PutData.put(new ItemStack(Material.CHEST_MINECART), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "buy_requests"),
                new Pair<>("button", "my_buy_requests")
        ));
        ItemMeta my_buy_requests_meta = my_buy_requests.getItemMeta();
        my_buy_requests_meta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("my_buy_requests").title));
        my_buy_requests_meta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("my_buy_requests").lore));
        my_buy_requests.setItemMeta(my_buy_requests_meta);
        inventory.setItem(2, my_buy_requests);


        ItemStack page_left = PutData.put(new ItemStack(Material.PAPER), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "buy_requests"),
                new Pair<>("button", "page_left")
        ));
        ItemMeta page_left_meta = page_left.getItemMeta();
        page_left_meta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("page_left").title));
        page_left_meta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("page_left").lore));
        page_left.setItemMeta(page_left_meta);
        inventory.setItem(3, page_left);


        ItemStack update_page = PutData.put(new ItemStack(Material.SLIME_BALL), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "buy_requests"),
                new Pair<>("button", "update_page")
        ));
        ItemMeta update_page_meta = update_page.getItemMeta();
        update_page_meta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("update_page").title));
        update_page_meta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("update_page").lore));
        update_page.setItemMeta(update_page_meta);
        inventory.setItem(4, update_page);


        ItemStack page_right = PutData.put(new ItemStack(Material.PAPER), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "buy_requests"),
                new Pair<>("button", "page_right")
        ));
        ItemMeta page_right_meta = page_right.getItemMeta();
        page_right_meta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("page_right").title));
        page_right_meta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("page_right").lore));
        page_right.setItemMeta(page_right_meta);
        inventory.setItem(5, page_right);


        //
        // SORTING BUTTON
        //
        ItemStack sortingButton = PutData.put(new ItemStack(Material.HOPPER),Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "buy_requests"),
                new Pair<>("button", "sorting")
        ));
        ItemMeta sortingButtonMeta = sortingButton.getItemMeta();
        sortingButtonMeta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("sorting").title));
        ArrayList<String> lore = new ArrayList<>((gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("sorting").lore));

        switch (info.current_buy_requests_sorting_type.get(playerName)){
            case "NEW_FIRST":
                lore.set(0, gui.messages.get("dash").get(0) + lore.get(0));
                break;
            case "CHEAP_FIRST":
                lore.set(1, gui.messages.get("dash").get(0) + lore.get(1));
                break;
            case "OLD_FIRST":
                lore.set(2, gui.messages.get("dash").get(0) + lore.get(2));
                break;
            case "EXPENSIVE_FIRST":
                lore.set(3, gui.messages.get("dash").get(0) + lore.get(3));
                break;
        }

        sortingButtonMeta.lore(gui.cm.parseColoredText(lore));
        sortingButton.setItemMeta(sortingButtonMeta);
        inventory.setItem(7, sortingButton);

        //
        // CATEGORIES PAGE
        //
        ItemStack categoriesPage = PutData.put(new ItemStack(Material.FEATHER),Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "buy_requests"),
                new Pair<>("button", "categories")
        ));
        ItemMeta categoriesPageMeta = categoriesPage.getItemMeta();
        categoriesPageMeta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("categories").title));
        categoriesPageMeta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("categories").lore));
        categoriesPage.setItemMeta(categoriesPageMeta);
        inventory.setItem(8, categoriesPage);


        ArrayList<BuyRequestNotation> buyRequests;
        if (info.current_buy_requests_item_filter.get(playerName) == null) buyRequests = buyRequestStorage.getAllBuyRequestsSorted(info.current_buy_requests_sorting_type.get(playerName), info.current_buy_requests_page.get(playerName));
        else buyRequests = buyRequestStorage.getAllBuyRequestsFiltered(itemCategoriesProcessor.filterNotations.get(info.current_buy_requests_item_filter.get(playerName)).filters,
                info.current_buy_requests_page.get(playerName), info.current_buy_requests_sorting_type.get(playerName));

        int currentSlot = 9;
        int br_size = buyRequests.size();

        for (int i = 0; i < br_size; i++) {
            if (currentSlot > 53) break;
            BuyRequestNotation requestNotation = buyRequests.get(i);
            Map<String, String> parse_args = new HashMap<>();

            parse_args.put("{AMOUNT}",NumberUtil.formatClean(requestNotation.amount_total - requestNotation.amount_now, 2));
            parse_args.put("{PRICE}", NumberUtil.formatClean(requestNotation.price, 2));
            parse_args.put("{CURRENCY}", gui.messages.get("currency_symbol").get(0));
            parse_args.put("{BUYER}", requestNotation.owner);
            inventory.setItem(currentSlot, prepare_goods_item(requestNotation, parse_args));
            currentSlot++;
        }

        return inventory;
    }

    private ItemStack prepare_goods_item(BuyRequestNotation requestNotation, Map<String, String> parse_args) {
        ItemStack currentItemStack = PutData.put(ItemStack.deserializeBytes(requestNotation.bytes), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "buy_requests"),
                new Pair<>("key", "request"),
                new Pair<>("unique_key", requestNotation.key)
        ));
        //
        List<Component> current_lore = currentItemStack.getItemMeta().lore();
        List<String> original_lore = new ArrayList<>(gui.menus.get("buy_requests").buttons.get("request").lore);
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
                case "my_buy_requests":{
                    info.current_my_buy_requests_page.put(playerName, 0);
                    info.current_menu.put(playerName, "my_buy_requests");
                    player.openInventory(gui.menus.get("my_buy_requests").generateInventory(playerName));
                    break;
                }
                case "update_page":{
                    info.current_menu.put(playerName, "buy_requests");
                    player.openInventory(gui.menus.get("buy_requests").generateInventory(playerName));
                    break;
                }
                case "page_left":
                    info.current_buy_requests_page.put(playerName, Math.max(0, Math.min(info.current_buy_requests_page.get(playerName) - 1, buyRequestStorage.getPagesNumFiltered(info.current_buy_requests_item_filter.get(playerName)))));
                    player.openInventory(gui.menus.get("buy_requests").generateInventory(playerName));
                    break;
                case "page_right":
                    info.current_buy_requests_page.put(playerName, Math.max(0, Math.min(info.current_buy_requests_page.get(playerName) + 1, buyRequestStorage.getPagesNumFiltered(info.current_buy_requests_item_filter.get(playerName)))));
                    player.openInventory(gui.menus.get("buy_requests").generateInventory(playerName));
                    break;
                case "sorting": {
                    String newSortingType = "";
                    if (event.isLeftClick()){
                        switch (info.current_buy_requests_sorting_type.get(playerName)){
                            case "NEW_FIRST":
                                newSortingType = "CHEAP_FIRST";
                                break;
                            case "CHEAP_FIRST":
                                newSortingType = "OLD_FIRST";
                                break;
                            case "OLD_FIRST":
                                newSortingType = "EXPENSIVE_FIRST";
                                break;
                            case "EXPENSIVE_FIRST":
                                newSortingType = "NEW_FIRST";
                                break;
                        }
                    }
                    else if (event.isRightClick()){
                        switch (info.current_buy_requests_sorting_type.get(playerName)){
                            case "NEW_FIRST":
                                newSortingType = "EXPENSIVE_FIRST";
                                break;
                            case "EXPENSIVE_FIRST":
                                newSortingType = "OLD_FIRST";
                                break;
                            case "OLD_FIRST":
                                newSortingType = "CHEAP_FIRST";
                                break;
                            case "CHEAP_FIRST":
                                newSortingType = "NEW_FIRST";
                                break;
                        }
                    }

                    info.current_buy_requests_sorting_type.put(playerName, newSortingType);
                    player.openInventory(gui.menus.get("buy_requests").generateInventory(playerName));
                    break;
                }
                case "categories": {
                    info.current_menu.put(playerName, "buy_requests_categories");
                    player.openInventory(gui.menus.get("buy_requests_categories").generateInventory(playerName));
                    break;
                }
            }
        } else if (pdc.has(new NamespacedKey(plugin, "key"), PersistentDataType.STRING)) {
            String key = pdc.get(new NamespacedKey(plugin, "key"), PersistentDataType.STRING);
            if (key.equalsIgnoreCase("request")) {
                if (pdc.get(new NamespacedKey(plugin, "unique_key"), PersistentDataType.STRING) != null) {
                    String unique_key = pdc.get(new NamespacedKey(plugin, "unique_key"), PersistentDataType.STRING);
                    if (unique_key == null) return;

                    BuyRequestNotation buyRequestNotation = buyRequestStorage.getBuyRequest(unique_key);

                    if (buyRequestNotation == null) {
                        gui.send_message(player, "buy_request_is_closed");
                        player.openInventory(gui.menus.get("buy_requests").generateInventory(playerName));
                        return;
                    }

                    if (buyRequestNotation.owner.equals(playerName)) {
                        gui.send_message(player, "cannot_sell_to_yourself");
                        player.openInventory(gui.menus.get("buy_requests").generateInventory(playerName));
                        return;
                    }

                    if (event.isLeftClick() && !event.isShiftClick()) process_left_click(player, buyRequestNotation);
                    else if (event.isRightClick() && !event.isShiftClick()) process_right_click(player, buyRequestNotation);
                    else if (event.isRightClick() && event.isShiftClick()) process_shift_right_click(player, buyRequestNotation);

//                    else if (event.isRightClick() && (!event.isShiftClick())){
//                        int stack_size = new ItemStack(Objects.requireNonNull(Material.getMaterial(buyRequestNotation.material))).getMaxStackSize();
//                        if (buyRequestNotation.amount_now + stack_size <= buyRequestNotation.amount_total){
//                            Inventory playerInv = player.getInventory();
//                            int valid_items_counter = 0;
//
//                            for (ItemStack playerItem : playerInv.getContents()) {
//                                if (playerItem != null) {
//
//                                    ItemStack playerItemCopy = playerItem.clone();
//                                    ItemMeta playerItemCopyMeta = playerItemCopy.getItemMeta();
//                                    playerItemCopyMeta.displayName(LEGACY.deserialize(""));
//                                    playerItemCopy.setItemMeta(playerItemCopyMeta);
//                                    playerItemCopy.setAmount(1);
//
//                                    ItemStack shopItemCopy = ItemStack.deserializeBytes(buyRequestNotation.bytes);
//                                    ItemMeta shopItemCopyMeta = shopItemCopy.getItemMeta();
//                                    shopItemCopyMeta.displayName(LEGACY.deserialize(""));
//                                    shopItemCopy.setItemMeta(shopItemCopyMeta);
//                                    shopItemCopy.setAmount(1);
//
//                                    if (playerItemCopy.equals(shopItemCopy)) {
//                                        valid_items_counter += playerItem.getAmount();
//                                    }
//                                }
//                            }
//
//                            if (valid_items_counter < stack_size){
//                                player.sendMessage(gui.not_enough_items);
//                                return;
//                            }
//
//                            valid_items_counter = stack_size;
//
//                            for (ItemStack playerItem : playerInv.getContents()) {
//                                if (playerItem != null) {
//
//                                    ItemStack playerItemCopy = playerItem.clone();
//                                    ItemMeta playerItemCopyMeta = playerItemCopy.getItemMeta();
//                                    playerItemCopyMeta.displayName(LEGACY.deserialize(""));
//                                    playerItemCopy.setItemMeta(playerItemCopyMeta);
//                                    playerItemCopy.setAmount(1);
//
//                                    ItemStack shopItemCopy = ItemStack.deserializeBytes(buyRequestNotation.bytes);
//                                    ItemMeta shopItemCopyMeta = shopItemCopy.getItemMeta();
//                                    shopItemCopyMeta.displayName(LEGACY.deserialize(""));
//                                    shopItemCopy.setItemMeta(shopItemCopyMeta);
//                                    shopItemCopy.setAmount(1);
//
//                                    if (playerItemCopy.equals(shopItemCopy)) {
//                                        int d = Math.min(valid_items_counter, playerItem.getAmount());
//                                        buyRequestStorage.updateBuyRequest(unique_key, d);
//                                        playerItem.setAmount(playerItem.getAmount() - d);
//                                        valid_items_counter -= d;
//                                        if (valid_items_counter == 0) break;
//                                    }
//                                }
//                            }
//
//                            String msg = gui.you_sold_item_notification;
//                            msg = msg.replace("{ITEM}", buyRequestNotation.material);
//                            msg = msg.replace("{PLAYER}", buyRequestNotation.owner);
//                            msg = msg.replace("{AMOUNT}", String.valueOf(stack_size));
//                            msg = msg.replace("{PRICE}", String.valueOf(buyRequestNotation.price * stack_size));
//                            msg = msg.replace("{CURRENCY}", gui.currency);
//                            player.sendMessage(msg);
//
//                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
//
//                            if (getServer().getPluginManager().getPlugin("Vault") == null) {
//                                return;
//                            }
//                            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
//                            if (rsp == null) {
//                                return;
//                            }
//
//                            rsp = getServer().getServicesManager().getRegistration(Economy.class);
//
//                            rsp.getProvider().depositPlayer(playerName, buyRequestNotation.price * stack_size);
//
//                            if (Bukkit.getPlayer(buyRequestNotation.owner).isOnline()){
//                                msg = gui.buy_request_received;
//                                msg = msg.replace("{ITEM}", buyRequestNotation.material);
//                                msg = msg.replace("{PLAYER}", playerName);
//                                msg = msg.replace("{AMOUNT}", String.valueOf(stack_size));
//                                Objects.requireNonNull(Bukkit.getPlayer(buyRequestNotation.owner)).sendMessage(msg);
//                            }
//                        }
//                        else{
//                            player.sendMessage(gui.cannot_sell_one_stack);
//                        }
//                        player.updateInventory();
//                        player.openInventory(generateBuyRequestsMainMenu(playerName));
//                    }
//                    else if (event.isRightClick()){ //Shift + ПКМ
//                        if (buyRequestNotation.amount_now < buyRequestNotation.amount_total){
//                            int player_has_items = 0;
//                            Inventory playerInv = player.getInventory();
//                            for (ItemStack playerItem : playerInv.getContents()) {
//                                if (playerItem != null) {
//
//                                    ItemStack playerItemCopy = playerItem.clone();
//                                    ItemMeta playerItemCopyMeta = playerItemCopy.getItemMeta();
//                                    playerItemCopyMeta.displayName(LEGACY.deserialize(""));
//                                    playerItemCopy.setItemMeta(playerItemCopyMeta);
//                                    playerItemCopy.setAmount(1);
//
//                                    ItemStack shopItemCopy = ItemStack.deserializeBytes(buyRequestNotation.bytes);
//                                    ItemMeta shopItemCopyMeta = shopItemCopy.getItemMeta();
//                                    shopItemCopyMeta.displayName(LEGACY.deserialize(""));
//                                    shopItemCopy.setItemMeta(shopItemCopyMeta);
//                                    shopItemCopy.setAmount(1);
//
//                                    if (playerItemCopy.equals(shopItemCopy)) {
//                                        player_has_items += playerItem.getAmount();
//                                    }
//                                }
//                            }
//
//                            if (player_has_items == 0){
//                                player.sendMessage(gui.you_dont_have_this_item);
//                                return;
//                            }
//
//                            int can_be_sold = Math.min(buyRequestNotation.amount_total - buyRequestNotation.amount_now, player_has_items);
//
//                            String msg = gui.you_sold_item_notification;
//                            msg = msg.replace("{ITEM}", buyRequestNotation.material);
//                            msg = msg.replace("{PLAYER}", buyRequestNotation.owner);
//                            msg = msg.replace("{AMOUNT}", String.valueOf(can_be_sold));
//                            msg = msg.replace("{PRICE}", String.valueOf(buyRequestNotation.price * can_be_sold));
//                            msg = msg.replace("{CURRENCY}", gui.currency);
//                            player.sendMessage(msg);
//
//                            if (Bukkit.getPlayer(buyRequestNotation.owner).isOnline()){
//                                msg = gui.buy_request_received;
//                                msg = msg.replace("{ITEM}", buyRequestNotation.material);
//                                msg = msg.replace("{PLAYER}", playerName);
//                                msg = msg.replace("{AMOUNT}", String.valueOf(can_be_sold));
//                                Objects.requireNonNull(Bukkit.getPlayer(buyRequestNotation.owner)).sendMessage(msg);
//                            }
//
//                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
//
//                            if (getServer().getPluginManager().getPlugin("Vault") == null) {
//                                return;
//                            }
//                            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
//                            if (rsp == null) {
//                                return;
//                            }
//
//                            rsp = getServer().getServicesManager().getRegistration(Economy.class);
//
//                            rsp.getProvider().depositPlayer(playerName, buyRequestNotation.price * can_be_sold);
//
//                            for (ItemStack playerItem : playerInv.getContents()) {
//                                if (playerItem != null) {
//
//                                    ItemStack playerItemCopy = playerItem.clone();
//                                    ItemMeta playerItemCopyMeta = playerItemCopy.getItemMeta();
//                                    playerItemCopyMeta.displayName(LEGACY.deserialize(""));
//                                    playerItemCopy.setItemMeta(playerItemCopyMeta);
//                                    playerItemCopy.setAmount(1);
//
//                                    ItemStack shopItemCopy = ItemStack.deserializeBytes(buyRequestNotation.bytes);
//                                    ItemMeta shopItemCopyMeta = shopItemCopy.getItemMeta();
//                                    shopItemCopyMeta.displayName(LEGACY.deserialize(""));
//                                    shopItemCopy.setItemMeta(shopItemCopyMeta);
//                                    shopItemCopy.setAmount(1);
//
//                                    if (playerItemCopy.equals(shopItemCopy)) {
//                                        int d = Math.min(playerItem.getAmount(), can_be_sold);
//                                        can_be_sold -= d;
//                                        buyRequestStorage.updateBuyRequest(unique_key, d);
//                                        playerItem.setAmount(playerItem.getAmount() - d);
//                                        if (can_be_sold == 0) break;
//                                    }
//                                }
//                            }
//                            player.updateInventory();
//                        }
//                        player.openInventory(generateBuyRequestsMainMenu(playerName));
//                    }
                }
            }
        }
    }

    void process_left_click(Player player, BuyRequestNotation buyRequestNotation) {
        String playerName = player.getName();
        if (buyRequestNotation.amount_now + 1 <= buyRequestNotation.amount_total) {
            Inventory playerInv = player.getInventory();

            int player_has_items = 0;
            for (ItemStack playerItem : playerInv.getContents()) {
                if (playerItem != null) {

                    ItemStack playerItemCopy = playerItem.clone();
                    ItemMeta playerItemCopyMeta = playerItemCopy.getItemMeta();
                    playerItemCopyMeta.displayName(null);
                    playerItemCopy.setItemMeta(playerItemCopyMeta);
                    playerItemCopy.setAmount(1);

                    ItemStack shopItemCopy = ItemStack.deserializeBytes(buyRequestNotation.bytes);
                    ItemMeta shopItemCopyMeta = shopItemCopy.getItemMeta();
                    shopItemCopyMeta.displayName(null);
                    shopItemCopy.setItemMeta(shopItemCopyMeta);
                    shopItemCopy.setAmount(1);

                    if (playerItemCopy.equals(shopItemCopy)) {
                        player_has_items += playerItem.getAmount();
                    }
                }
            }

            if (player_has_items == 0) {
                gui.send_message(player, "you_dont_have_this_item");
                player.openInventory(gui.menus.get("buy_requests").generateInventory(playerName));
                return;
            }

            for (ItemStack playerItem : playerInv.getContents()) {
                if (playerItem != null) {

                    ItemStack playerItemCopy = playerItem.clone();
                    ItemMeta playerItemCopyMeta = playerItemCopy.getItemMeta();
                    playerItemCopyMeta.displayName(null);
                    playerItemCopy.setItemMeta(playerItemCopyMeta);
                    playerItemCopy.setAmount(1);

                    ItemStack shopItemCopy = ItemStack.deserializeBytes(buyRequestNotation.bytes);
                    ItemMeta shopItemCopyMeta = shopItemCopy.getItemMeta();
                    shopItemCopyMeta.displayName(null);
                    shopItemCopy.setItemMeta(shopItemCopyMeta);
                    shopItemCopy.setAmount(1);

                    if (playerItemCopy.equals(shopItemCopy)) {
                        playerItem.setAmount(playerItem.getAmount() - 1);
                        Map<String, String> parse_args = new HashMap<>();
                        parse_args.put("{ITEM}", buyRequestNotation.material);
                        parse_args.put("{BUYER}", buyRequestNotation.owner);
                        parse_args.put("{AMOUNT}", "1");
                        parse_args.put("{MONEY}", NumberUtil.formatClean(buyRequestNotation.price, 2));
                        parse_args.put("{CURRENCY}", gui.messages.get("currency_symbol").get(0));

                        List<String> msg = new ArrayList<>();
                        for (String temp : gui.messages.get("buy_requests_item_was_sold_seller")) {
                            msg.add(parse(temp, parse_args));
                        }
                        gui.send_message(player, msg);

                        if (getServer().getOnlinePlayers().contains(getServer().getPlayer(buyRequestNotation.owner))) {
                            Player item_owner = getServer().getPlayerExact(buyRequestNotation.owner);
                            boolean is_online = (item_owner != null);
                            if (is_online) is_online = item_owner.isOnline();
                            if (is_online) {
                                parse_args = new HashMap<>();
                                parse_args.put("{ITEM}", buyRequestNotation.material);
                                parse_args.put("{SELLER}", playerName);
                                parse_args.put("{AMOUNT}", String.valueOf(1));
                                msg = new ArrayList<>();
                                for (String temp : gui.messages.get("buy_requests_item_was_sold_buyer")) {
                                    msg.add(parse(temp, parse_args));
                                }
                                gui.send_message(item_owner, msg);
                            }
                        }

                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                        if (getServer().getPluginManager().getPlugin("Vault") == null) {
                            throw new RuntimeException("Bukkit.getServer().getPluginManager().getPlugin(\"Vault\") was null");
                        }
                        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
                        if (rsp == null) {
                            throw new RuntimeException("Bukkit.getServer().getServicesManager().getRegistration(Economy.class) was null");
                        }

                        rsp.getProvider().depositPlayer(playerName, buyRequestNotation.price);

                        buyRequestStorage.updateBuyRequest(buyRequestNotation.key, 1);
                        break;
                    } else {
                        //
                    }
                }
            }
            player.updateInventory();
            player.openInventory(gui.menus.get("buy_requests").generateInventory(playerName));
        } else {
            player.openInventory(gui.menus.get("buy_requests").generateInventory(playerName));
            gui.send_message(player, "buy_request_is_closed");
        }
    }
    void process_right_click(Player player, BuyRequestNotation buyRequestNotation) {
        int stack_size = new ItemStack(Objects.requireNonNull(Material.getMaterial(buyRequestNotation.material))).getMaxStackSize();
        if (buyRequestNotation.amount_now + stack_size <= buyRequestNotation.amount_total) {
            Inventory playerInv = player.getInventory();
            int valid_items_counter = 0;

            for (ItemStack playerItem : playerInv.getContents()) {
                if (playerItem != null) {

                    ItemStack playerItemCopy = playerItem.clone();
                    ItemMeta playerItemCopyMeta = playerItemCopy.getItemMeta();
                    playerItemCopyMeta.displayName(null);
                    playerItemCopy.setItemMeta(playerItemCopyMeta);
                    playerItemCopy.setAmount(1);

                    ItemStack shopItemCopy = ItemStack.deserializeBytes(buyRequestNotation.bytes);
                    ItemMeta shopItemCopyMeta = shopItemCopy.getItemMeta();
                    shopItemCopyMeta.displayName(null);
                    shopItemCopy.setItemMeta(shopItemCopyMeta);
                    shopItemCopy.setAmount(1);

                    if (playerItemCopy.equals(shopItemCopy)) {
                        valid_items_counter += playerItem.getAmount();
                    }
                }
            }

            if (valid_items_counter < stack_size) {
                gui.send_message(player, "you_dont_have_stack_of_this_item");
                return;
            }

            valid_items_counter = stack_size;

            for (ItemStack playerItem : playerInv.getContents()) {
                if (playerItem != null) {

                    ItemStack playerItemCopy = playerItem.clone();
                    ItemMeta playerItemCopyMeta = playerItemCopy.getItemMeta();
                    playerItemCopyMeta.displayName(null);
                    playerItemCopy.setItemMeta(playerItemCopyMeta);
                    playerItemCopy.setAmount(1);

                    ItemStack shopItemCopy = ItemStack.deserializeBytes(buyRequestNotation.bytes);
                    ItemMeta shopItemCopyMeta = shopItemCopy.getItemMeta();
                    shopItemCopyMeta.displayName(null);
                    shopItemCopy.setItemMeta(shopItemCopyMeta);
                    shopItemCopy.setAmount(1);

                    if (playerItemCopy.equals(shopItemCopy)) {
                        int d = Math.min(valid_items_counter, playerItem.getAmount());
                        buyRequestStorage.updateBuyRequest(buyRequestNotation.key, d);
                        playerItem.setAmount(playerItem.getAmount() - d);
                        valid_items_counter -= d;
                        if (valid_items_counter == 0) break;
                    }
                }
            }

            Map<String, String> parse_args = new HashMap<>();
            parse_args.put("{ITEM}", buyRequestNotation.material);
            parse_args.put("{BUYER}", buyRequestNotation.owner);
            parse_args.put("{AMOUNT}", String.valueOf(stack_size));
            parse_args.put("{MONEY}", NumberUtil.formatClean(buyRequestNotation.price * stack_size, 2));
            parse_args.put("{CURRENCY}", gui.messages.get("currency_symbol").get(0));

            List<String> msg = new ArrayList<>();
            for (String temp : gui.messages.get("buy_requests_item_was_sold_seller")) {
                msg.add(parse(temp, parse_args));
            }
            gui.send_message(player, msg);

            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

            if (getServer().getPluginManager().getPlugin("Vault") == null) {
                return;
            }
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return;
            }

            rsp = getServer().getServicesManager().getRegistration(Economy.class);

            rsp.getProvider().depositPlayer(player.getName(), buyRequestNotation.price * stack_size);

            if (getServer().getOnlinePlayers().contains(getServer().getPlayer(buyRequestNotation.owner))) {
                Player item_owner = getServer().getPlayerExact(buyRequestNotation.owner);
                boolean is_online = (item_owner != null);
                if (is_online) is_online = item_owner.isOnline();
                if (is_online) {
                    parse_args = new HashMap<>();
                    parse_args.put("{ITEM}", buyRequestNotation.material);
                    parse_args.put("{SELLER}", player.getName());
                    parse_args.put("{AMOUNT}", String.valueOf(stack_size));
                    msg = new ArrayList<>();
                    for (String temp : gui.messages.get("buy_requests_item_was_sold_buyer")) {
                        msg.add(parse(temp, parse_args));
                    }
                    gui.send_message(item_owner, msg);
                }
            }
        } else {
            gui.send_message(player, "cannot_sell_one_stack");
        }
        player.updateInventory();
        player.openInventory(gui.menus.get("buy_requests").generateInventory(player.getName()));
    }
    void process_shift_right_click(Player player, BuyRequestNotation buyRequestNotation){
        if (buyRequestNotation.amount_now < buyRequestNotation.amount_total){
            int player_has_items = 0;
            Inventory playerInv = player.getInventory();
            for (ItemStack playerItem : playerInv.getContents()) {
                if (playerItem != null) {

                    ItemStack playerItemCopy = playerItem.clone();
                    ItemMeta playerItemCopyMeta = playerItemCopy.getItemMeta();
                    playerItemCopyMeta.displayName(null);
                    playerItemCopy.setItemMeta(playerItemCopyMeta);
                    playerItemCopy.setAmount(1);

                    ItemStack shopItemCopy = ItemStack.deserializeBytes(buyRequestNotation.bytes);
                    ItemMeta shopItemCopyMeta = shopItemCopy.getItemMeta();
                    shopItemCopyMeta.displayName(null);
                    shopItemCopy.setItemMeta(shopItemCopyMeta);
                    shopItemCopy.setAmount(1);

                    if (playerItemCopy.equals(shopItemCopy)) {
                        player_has_items += playerItem.getAmount();
                    }
                }
            }

            if (player_has_items == 0){
                gui.send_message(player, "you_dont_have_stack_of_this_item");
                return;
            }

            int can_be_sold = Math.min(buyRequestNotation.amount_total - buyRequestNotation.amount_now, player_has_items);

            Map<String, String> parse_args = new HashMap<>();
            parse_args.put("{ITEM}", buyRequestNotation.material);
            parse_args.put("{BUYER}", buyRequestNotation.owner);
            parse_args.put("{AMOUNT}", String.valueOf(can_be_sold));
            parse_args.put("{MONEY}", NumberUtil.formatClean(buyRequestNotation.price * can_be_sold, 2));
            parse_args.put("{CURRENCY}", gui.messages.get("currency_symbol").get(0));

            List<String> msg = new ArrayList<>();
            for (String temp : gui.messages.get("buy_requests_item_was_sold_seller")) {
                msg.add(parse(temp, parse_args));
            }
            gui.send_message(player, msg);

            if (getServer().getPluginManager().getPlugin("Vault") == null) {
                return;
            }
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return;
            }

            rsp = getServer().getServicesManager().getRegistration(Economy.class);

            rsp.getProvider().depositPlayer(player.getName(), buyRequestNotation.price * can_be_sold);

            if (getServer().getOnlinePlayers().contains(getServer().getPlayer(buyRequestNotation.owner))) {
                Player item_owner = getServer().getPlayerExact(buyRequestNotation.owner);
                boolean is_online = (item_owner != null);
                if (is_online) is_online = item_owner.isOnline();
                if (is_online) {
                    parse_args = new HashMap<>();
                    parse_args.put("{ITEM}", buyRequestNotation.material);
                    parse_args.put("{SELLER}", player.getName());
                    parse_args.put("{AMOUNT}", String.valueOf(can_be_sold));
                    msg = new ArrayList<>();
                    for (String temp : gui.messages.get("buy_requests_item_was_sold_buyer")) {
                        msg.add(parse(temp, parse_args));
                    }
                    gui.send_message(item_owner, msg);
                }
            }

            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

            ItemStack shopItemCopy = ItemStack.deserializeBytes(buyRequestNotation.bytes);
            ItemMeta shopItemCopyMeta = shopItemCopy.getItemMeta();
            shopItemCopyMeta.displayName(null);
            shopItemCopy.setItemMeta(shopItemCopyMeta);
            shopItemCopy.setAmount(1);

            for (ItemStack playerItem : playerInv.getContents()) {
                if (playerItem != null) {

                    ItemStack playerItemCopy = playerItem.clone();
                    ItemMeta playerItemCopyMeta = playerItemCopy.getItemMeta();
                    playerItemCopyMeta.displayName(null);
                    playerItemCopy.setItemMeta(playerItemCopyMeta);
                    playerItemCopy.setAmount(1);

                    if (playerItemCopy.equals(shopItemCopy)) {
                        int d = Math.min(playerItem.getAmount(), can_be_sold);
                        can_be_sold -= d;
                        buyRequestStorage.updateBuyRequest(buyRequestNotation.key, d);
                        playerItem.setAmount(playerItem.getAmount() - d);
                        if (can_be_sold == 0) break;
                    }
                }
            }
            player.updateInventory();
        }
        player.openInventory(gui.menus.get("buy_requests").generateInventory(player.getName()));
    }
}
