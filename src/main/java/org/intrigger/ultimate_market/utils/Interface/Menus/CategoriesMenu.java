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
import org.intrigger.ultimate_market.utils.StringDeserializer;

import java.util.Arrays;
import java.util.Map;

import static org.intrigger.ultimate_market.Ultimate_market.*;

public class CategoriesMenu extends Menu {
    public CategoriesMenu(String _name, String _title, Map<String, Button> _buttons) {
        super(_name, _title, _buttons);
    }

    @Override
    public Inventory generateInventory(String playerName){
        int inventorySize = 54;
        Inventory inventory = Bukkit.createInventory(null,
                inventorySize, gui.cm.parseColoredText(
                                gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).title
                        )
                
        );

        ItemStack main_menu = PutData.put(new ItemStack(Material.CHEST), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "categories"),
                new Pair<>("button", "main_menu")
        ));
        ItemMeta main_menu_meta = main_menu.getItemMeta();
        main_menu_meta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("main_menu").title));
        main_menu_meta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("main_menu").lore));
        main_menu.setItemMeta(main_menu_meta);
        inventory.setItem(0, main_menu);

        for (String title: Ultimate_market.itemCategoriesProcessor.titles){
            ItemStack sortingItem = PutData.put(new ItemStack(Ultimate_market.itemCategoriesProcessor.filterNotations.get(title).material), Arrays.asList(
                    new Pair<>("plugin", "ultimate_market"),
                    new Pair<>("menu", "categories"),
                    new Pair<>("button", title)
            ));
            ItemMeta sortingItemMeta = sortingItem.getItemMeta();
            sortingItemMeta.displayName(gui.cm.parseColoredText(Ultimate_market.itemCategoriesProcessor.filterNotations.get(title).title));
            sortingItemMeta.lore(null);
            sortingItemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            sortingItemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            sortingItem.setItemMeta(sortingItemMeta);
            inventory.setItem(Ultimate_market.itemCategoriesProcessor.filterNotations.get(title).slot, sortingItem);
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

            switch (menu_item_key) {
                case "main_menu": {
                    info.current_menu.put(playerName, "main_menu");
                    player.openInventory(gui.menus.get("main_menu").generateInventory(playerName));
                    break;
                }
            }

            if (!itemCategoriesProcessor.titles.contains(menu_item_key)){ return;}
            info.current_item_filter.put(playerName, menu_item_key);
            info.current_menu.put(playerName, "main_menu");
            player.openInventory(gui.menus.get("main_menu").generateInventory(playerName));
        }
    }
}
