package org.intrigger.ultimate_market.utils.Interface.Menus;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.intrigger.ultimate_market.Ultimate_market;
import org.intrigger.ultimate_market.utils.Interface.Button;
import org.intrigger.ultimate_market.utils.Interface.Menu;
import org.intrigger.ultimate_market.utils.ItemStackNotation;
import org.intrigger.ultimate_market.utils.ItemUtils.PutData;
import org.intrigger.ultimate_market.utils.NumberUtil;
import org.intrigger.ultimate_market.utils.Pair;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.intrigger.ultimate_market.Ultimate_market.*;

public class MainMenu extends Menu {

    public MainMenu(String _name, String _title, Map<String, Button> _buttons) {
        super(_name, _title, _buttons);
    }

    @Override
    public Inventory generateInventory(String playerName){

        long nano_start = System.nanoTime();

        int inventorySize = 54;

        Inventory inventory = Bukkit.createInventory(null,
                inventorySize,
                        gui.cm.parseColoredText(
                                gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).title +
                                        ((Ultimate_market.info.current_item_filter.get(playerName) == null) ? "" :
                                                ": " + itemCategoriesProcessor.display_names.get(Ultimate_market.info.current_item_filter.get(playerName))
                                        )
                        )

        );

        /*
            My Auction Slots Page Button
         */

        ItemStack my_shop = PutData.put(new ItemStack(Material.CHEST),Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "main_menu"),
                new Pair<>("button", "my_shop")
                ));
        ItemMeta my_shop_meta = my_shop.getItemMeta();
        my_shop_meta.displayName(gui.cm.parseColoredText(gui.menus.get("main_menu").buttons.get("my_shop").title));
        my_shop_meta.lore(gui.cm.parseColoredText(gui.menus.get("main_menu").buttons.get("my_shop").lore));
        my_shop.setItemMeta(my_shop_meta);
        inventory.setItem(0, my_shop);

        /*
         *
         *   BuyRequests Button (кнопка поставок)
         *
         */

        ItemStack buyRequests = PutData.put(new ItemStack(Material.CHEST_MINECART),Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "main_menu"),
                new Pair<>("button", "buy_requests")
        ));
        ItemMeta buyRequestsMeta = buyRequests.getItemMeta();
        buyRequestsMeta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("buy_requests").title));
        buyRequestsMeta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("buy_requests").lore));
        buyRequests.setItemMeta(buyRequestsMeta);
        inventory.setItem(2, buyRequests);


        //
        // UPDATE PAGE
        //
        ItemStack updatePage = PutData.put(new ItemStack(Material.SLIME_BALL),Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "main_menu"),
                new Pair<>("button", "update_page")
        ));
        ItemMeta updatePageMeta = updatePage.getItemMeta();
        updatePageMeta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("update_page").title));
        updatePageMeta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("update_page").lore));
        updatePage.setItemMeta(updatePageMeta);
        inventory.setItem(4, updatePage);

        //
        // LEFT PAGE
        //
        ItemStack leftPage = PutData.put(new ItemStack(Material.PAPER),Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "main_menu"),
                new Pair<>("button", "page_left")
        ));
        ItemMeta leftPageMeta = leftPage.getItemMeta();
        leftPageMeta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("page_left").title));
        leftPageMeta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("page_left").lore));
        leftPage.setItemMeta(leftPageMeta);
        inventory.setItem(3, leftPage);

        //
        // RIGHT PAGE
        //
        ItemStack rightPage = PutData.put(new ItemStack(Material.PAPER),Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "main_menu"),
                new Pair<>("button", "page_right")
        ));
        ItemMeta rightPageMeta = rightPage.getItemMeta();
        rightPageMeta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("page_right").title));
        rightPageMeta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("page_right").lore));
        rightPage.setItemMeta(rightPageMeta);
        inventory.setItem(5, rightPage);

        //
        // CATEGORIES PAGE
        //
        ItemStack categoriesPage = PutData.put(new ItemStack(Material.FEATHER),Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "main_menu"),
                new Pair<>("button", "categories")
        ));
        ItemMeta categoriesPageMeta = categoriesPage.getItemMeta();
        categoriesPageMeta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("categories").title));
        categoriesPageMeta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("categories").lore));
        categoriesPage.setItemMeta(categoriesPageMeta);
        inventory.setItem(8, categoriesPage);

        //
        // Tip Button
        //

        ItemStack tipButton = PutData.put(new ItemStack(Material.WRITABLE_BOOK),Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "main_menu"),
                new Pair<>("button", "tips_menu")
        ));
        ItemMeta tipButtonMeta = tipButton.getItemMeta();
        tipButtonMeta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("tips_menu").title));
        tipButtonMeta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("tips_menu").lore));
        tipButton.setItemMeta(tipButtonMeta);
        inventory.setItem(1, tipButton);


        //
        // SORTING BUTTON
        //
        ItemStack sortingButton = PutData.put(new ItemStack(Material.HOPPER),Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "main_menu"),
                new Pair<>("button", "sorting")
        ));
        ItemMeta sortingButtonMeta = sortingButton.getItemMeta();
        sortingButtonMeta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("sorting").title));
        ArrayList<String> lore = new ArrayList<>((gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("sorting").lore));

        switch (Ultimate_market.info.current_sorting_type.get(playerName)){
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

        /*
            Sorting items from 'items sold file' by time
         */

        ArrayList<ItemStackNotation> queryResult;

        if (Ultimate_market.info.current_item_filter.get(playerName) == null) queryResult = itemStorage.getAllKeys(Ultimate_market.info.current_market_page.get(playerName), Ultimate_market.info.current_sorting_type.get(playerName));
        else queryResult = itemStorage.getAllItemsFiltered(itemCategoriesProcessor.filterNotations.get(Ultimate_market.info.current_item_filter.get(playerName)).filters, Ultimate_market.info.current_market_page.get(playerName), Ultimate_market.info.current_sorting_type.get(playerName));



        if (queryResult != null){
            int currentSlot = 9;

            int queryResultSize = queryResult.size();

            for (int i = 0; i < queryResultSize; i++) {
                Map<String, String> parse_args = new HashMap<>();
                if (currentSlot > 53) break;
                ItemStackNotation itemStackNotation = queryResult.get(i);

                String seller = itemStackNotation.owner;
                double price = itemStackNotation.price;

                parse_args.put("{SELLER}", seller);
                parse_args.put("{PRICE}", (itemStackNotation.full == 1) ? NumberUtil.formatClean(price, 2) : NumberUtil.formatClean(price * itemStackNotation.amount, 2));
                parse_args.put("{CURRENCY}", gui.messages.get("currency_symbol").get(0));

                if (!(itemStackNotation.full == 1 || itemStackNotation.amount == 1)){
                    parse_args.put("{PRICE_PER_ONE}", NumberUtil.formatClean(price, 2));
                }

                inventory.setItem(currentSlot, prepare_goods_item(itemStackNotation, parse_args));
                currentSlot++;
            }

        }

        long nano_stop = System.nanoTime();

        //System.out.println(ChatColor.GREEN + "Time Elapsed: " + ((nano_stop - nano_start) / 1_000.0f) + ChatColor.GOLD + " mcs");

        return inventory;
    }

    private ItemStack prepare_goods_item(ItemStackNotation goods_item, Map<String, String> parse_args){
        ItemStack currentItemStack = PutData.put(ItemStack.deserializeBytes(goods_item.bytes), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "main_menu"),
                new Pair<>("key", "goods")
        ));
        //
        List<Component> current_lore = currentItemStack.getItemMeta().lore();
        List<String> original_lore = new ArrayList<>(gui.menus.get("main_menu").buttons.get("goods").lore);
        List<String> new_lore = new ArrayList<>();

        if ((goods_item.full == 1 || goods_item.amount == 1)){
            original_lore.remove((original_lore.size() - 1) - 1);
        }

        for (String s: original_lore){
            if ((goods_item.full == 1 || goods_item.amount == 1) && (s.contains("{PRICE_PER_ONE}"))){
                continue;
            }
            else{
                new_lore.add(parse(s, parse_args));
            }
        }
        if (current_lore == null) current_lore = new ArrayList<>();
        current_lore.addAll(gui.cm.parseColoredText((new_lore)));
        currentItemStack.lore(current_lore);
        currentItemStack.setAmount(goods_item.amount);
        ItemMeta meta = currentItemStack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        currentItemStack.setItemMeta(meta);
        return currentItemStack;
    }

    private String parse(String s, Map<String, String> parse_args){
        String res = s;
        for (String i: parse_args.keySet()){
            res = res.replace(i, parse_args.get(i));
        }
        return res;
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

            String currentFilter = info.current_item_filter.get(playerName);
            ArrayList<String> filters;
            if (currentFilter == null) filters = null;
            else {
                filters = itemCategoriesProcessor.filterNotations.get(currentFilter).filters;
            }

            int pagesNum = itemStorage.getTotalPages(filters);

            int currentPage = info.current_market_page.get(playerName);

            switch (menu_item_key) {
                case "my_shop":
                    info.current_menu.put(playerName, "my_shop");
                    info.current_market_page.put(playerName, 0);
                    player.openInventory(gui.menus.get("my_shop").generateInventory(playerName));
                    break;
                case "tips_menu":
                    info.current_menu.put(playerName, "tips_menu");
                    player.openInventory(gui.menus.get("tips_menu").generateInventory(playerName));
                    break;
                case "update_page":
                    info.current_market_page.put(playerName, Math.max(0, Math.min(currentPage, pagesNum)));
                    player.openInventory(gui.menus.get("main_menu").generateInventory(playerName));
                    break;
                case "page_left":
                    info.current_market_page.put(playerName, Math.max(0, Math.min(currentPage - 1, pagesNum)));
                    player.openInventory(gui.menus.get("main_menu").generateInventory(playerName));
                    break;
                case "page_right":
                    info.current_market_page.put(playerName, Math.max(0, Math.min(currentPage + 1, pagesNum)));
                    player.openInventory(gui.menus.get("main_menu").generateInventory(playerName));
                    break;
                case "categories":
                    info.current_menu.put(playerName, "categories");
                    info.current_item_filter.put(playerName, null);
                    player.openInventory(gui.menus.get("categories").generateInventory(playerName));
                    break;
                case "sorting":
                    String newSortingType = "";
                    if (event.isLeftClick()){
                        switch (info.current_sorting_type.get(playerName)){
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
                        switch (info.current_sorting_type.get(playerName)){
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

                    info.current_sorting_type.put(playerName, newSortingType);
                    player.openInventory(gui.menus.get("main_menu").generateInventory(playerName));
                    break;
                case "buy_requests":
                    info.current_menu.put(playerName, "buy_requests");
                    player.openInventory(gui.menus.get("buy_requests").generateInventory(playerName));
                    break;
            }
        }
        else if (pdc.has(new NamespacedKey(plugin, "key"), PersistentDataType.STRING)){
            String key = pdc.get(new NamespacedKey(plugin, "key"), PersistentDataType.STRING);
            assert key != null;

            String unique_key = pdc.get(new NamespacedKey(plugin, "unique_key"), PersistentDataType.STRING);
            assert unique_key != null;

            ItemStackNotation itemStackNotation = itemStorage.getItem(unique_key);

            if (itemStackNotation.owner.equals(playerName)){
                gui.send_message(player, "cannot_buy_your_goods");
                player.openInventory(gui.menus.get("main_menu").generateInventory(playerName));
                return;
            }

            info.current_buying_item.put(playerName, unique_key);

            if (itemStackNotation.full == 1 || itemStackNotation.amount == 1 || (event.isRightClick() && !event.isShiftClick())){
                info.current_buying_item_amount.put(playerName, itemStackNotation.amount);
                info.current_menu.put(playerName, "confirm_full_menu");
                player.openInventory(gui.menus.get("confirm_full_menu").generateInventory(playerName));
            }
            else{
                if (event.isLeftClick() && (!event.isShiftClick())){
                    info.current_buying_item_amount.put(playerName, 1);
                    info.current_menu.put(playerName, "select_amount_menu");
                    player.openInventory(gui.menus.get("select_amount_menu").generateInventory(playerName));
                }
                else if (event.isRightClick() && (!event.isShiftClick())){
                    info.current_menu.put(playerName, "confirm_full_menu");
                    player.openInventory(gui.menus.get("confirm_full_menu").generateInventory(playerName));
                }
            }
        }
    }

}
