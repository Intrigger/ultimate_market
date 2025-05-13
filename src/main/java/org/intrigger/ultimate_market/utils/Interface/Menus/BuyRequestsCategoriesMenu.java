package org.intrigger.ultimate_market.utils.Interface.Menus;

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
import org.intrigger.ultimate_market.Ultimate_market;
import org.intrigger.ultimate_market.utils.Interface.Button;
import org.intrigger.ultimate_market.utils.Interface.Menu;
import org.intrigger.ultimate_market.utils.ItemUtils.PutData;
import org.intrigger.ultimate_market.utils.Pair;

import java.util.Arrays;
import java.util.Map;

import static org.intrigger.ultimate_market.Ultimate_market.*;
import static org.intrigger.ultimate_market.Ultimate_market.gui;
import static org.intrigger.ultimate_market.Ultimate_market.info;
import static org.intrigger.ultimate_market.Ultimate_market.itemCategoriesProcessor;

public class BuyRequestsCategoriesMenu extends Menu {
    public BuyRequestsCategoriesMenu(String _name, String _title, Map<String, Button> _buttons) {
        super(_name, _title, _buttons);
    }

    @Override
    public Inventory generateInventory(String playerName){
        int inventorySize = 54;
        Inventory inventory = Bukkit.createInventory(null,
                inventorySize, gui.cm.parseColoredText(
                        gui.menus.get(info.current_menu.get(playerName)).title
                )

        );

        ItemStack main_menu = PutData.put(new ItemStack(Material.CHEST), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "buy_requests_categories"),
                new Pair<>("button", "buy_requests")
        ));
        ItemMeta main_menu_meta = main_menu.getItemMeta();
        main_menu_meta.displayName(gui.cm.parseColoredText(gui.menus.get(info.current_menu.get(playerName)).buttons.get("buy_requests").title));
        main_menu_meta.lore(gui.cm.parseColoredText(gui.menus.get(info.current_menu.get(playerName)).buttons.get("buy_requests").lore));
        main_menu.setItemMeta(main_menu_meta);
        inventory.setItem(0, main_menu);

        for (String title: Ultimate_market.itemCategoriesProcessor.titles){
            ItemStack sortingItem = PutData.put(new ItemStack(itemCategoriesProcessor.filterNotations.get(title).material), Arrays.asList(
                    new Pair<>("plugin", "ultimate_market"),
                    new Pair<>("menu", "buy_requests_categories"),
                    new Pair<>("button", title)
            ));
            ItemMeta sortingItemMeta = sortingItem.getItemMeta();
            sortingItemMeta.displayName(gui.cm.parseColoredText(itemCategoriesProcessor.filterNotations.get(title).title));
            sortingItemMeta.lore(null);
            sortingItemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            sortingItemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            sortingItem.setItemMeta(sortingItemMeta);
            inventory.setItem(itemCategoriesProcessor.filterNotations.get(title).slot, sortingItem);
        }

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

            info.current_buy_requests_page.put(playerName, 0);

            switch (menu_item_key) {
                case "buy_requests": {
                    info.current_buy_requests_item_filter.put(playerName, null);
                    info.current_menu.put(playerName, "buy_requests");
                    player.openInventory(gui.menus.get("buy_requests").generateInventory(playerName));
                    return;
                }
            }

            if (!itemCategoriesProcessor.titles.contains(menu_item_key)){ return;}
            info.current_buy_requests_item_filter.put(playerName, menu_item_key);
            info.current_menu.put(playerName, "buy_requests");
            player.openInventory(gui.menus.get("buy_requests").generateInventory(playerName));
        }
    }
}
