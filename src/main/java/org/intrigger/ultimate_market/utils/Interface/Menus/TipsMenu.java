package org.intrigger.ultimate_market.utils.Interface.Menus;

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
import org.intrigger.ultimate_market.Ultimate_market;
import org.intrigger.ultimate_market.utils.Interface.Button;
import org.intrigger.ultimate_market.utils.Interface.Menu;
import org.intrigger.ultimate_market.utils.ItemUtils.PutData;
import org.intrigger.ultimate_market.utils.Pair;
import org.intrigger.ultimate_market.utils.StringDeserializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.intrigger.ultimate_market.Ultimate_market.*;
import static org.intrigger.ultimate_market.Ultimate_market.info;
import static org.intrigger.ultimate_market.Ultimate_market.itemStorage;
import static org.intrigger.ultimate_market.Ultimate_market.plugin;

public class TipsMenu extends Menu {
    public TipsMenu(String _name, String _title, Map<String, Button> _buttons) {
        super(_name, _title, _buttons);
    }

    @Override
    public Inventory generateInventory(String playerName){
        int inventorySize = 54;
        Inventory inventory = Bukkit.createInventory(null,
                inventorySize, 
                        gui.cm.parseColoredText(
                                gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).title
                        )
                
        );

        ItemStack homeItem = PutData.put(new ItemStack(Material.CHEST), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "tips_menu"),
                new Pair<>("button", "main_menu")
        ));

        ItemMeta homeItemMeta = homeItem.getItemMeta();
        homeItemMeta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("main_menu").title));
        homeItemMeta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("main_menu").lore));
        homeItem.setItemMeta(homeItemMeta);
        inventory.setItem(0, homeItem);

        ItemStack button1 = PutData.put(new ItemStack(Material.PAPER), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "tips_menu"),
                new Pair<>("button", "button1")
        ));
        ItemMeta button1_meta = button1.getItemMeta();
        button1_meta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("button1").title));
        button1_meta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("button1").lore));
        button1.setItemMeta(button1_meta);
        inventory.setItem(18, button1);

        ItemStack button2 = PutData.put(new ItemStack(Material.MAP), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "tips_menu"),
                new Pair<>("button", "button2")
        ));
        ItemMeta button2_meta = button2.getItemMeta();
        button2_meta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("button2").title));
        button2_meta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("button2").lore));
        button2.setItemMeta(button2_meta);
        inventory.setItem(20, button2);

        ItemStack button3 = PutData.put(new ItemStack(Material.BOOK), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "tips_menu"),
                new Pair<>("button", "button3")
        ));
        ItemMeta button3_meta = button3.getItemMeta();
        button3_meta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("button3").title));
        button3_meta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("button3").lore));
        button3.setItemMeta(button3_meta);
        inventory.setItem(22, button3);

        ItemStack button4 = PutData.put(new ItemStack(Material.WRITABLE_BOOK), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "tips_menu"),
                new Pair<>("button", "button4")
        ));
        ItemMeta button4_meta = button4.getItemMeta();
        button4_meta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("button4").title));
        button4_meta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("button4").lore));
        button4.setItemMeta(button4_meta);
        inventory.setItem(24, button4);

        ItemStack button5 = PutData.put(new ItemStack(Material.LECTERN), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "tips_menu"),
                new Pair<>("button", "button5")
        ));
        ItemMeta button5_meta = button5.getItemMeta();
        button5_meta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("button5").title));
        button5_meta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("button5").lore));
        button5.setItemMeta(button5_meta);
        inventory.setItem(26, button5);
        
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
        }
    }
}
