package org.intrigger.ultimate_market.listeners;

import net.kyori.adventure.text.TextComponent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.intrigger.ultimate_market.Ultimate_market;
import org.intrigger.ultimate_market.commands.MarketExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ClickHandler implements Listener {

    private static MarketExecutor executor;

    public ClickHandler(Ultimate_market _plugin, MarketExecutor marketExecutor){
        Bukkit.getPluginManager().registerEvents(this, _plugin);
        executor = marketExecutor;
    }

    @EventHandler
    public void processClick(InventoryClickEvent event){
        boolean has_watermark = false;

        if (event.getClickedInventory() == null) return;

        List<ItemStack> all_items = new ArrayList<>();
        Collections.addAll(all_items, event.getView().getTopInventory().getContents());

        for (ItemStack item: all_items){
            if (item == null) continue;
            if (item.getItemMeta() == null) continue;
            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
            if (pdc.has(new NamespacedKey(Ultimate_market.plugin, "plugin"), PersistentDataType.STRING)){
                has_watermark = true;
            }
            break;
        }

        if (has_watermark){
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.updateInventory();
        }

        boolean has_menu = false;
        String menu = null;

        for (ItemStack item: event.getClickedInventory().getContents()){
            if (item == null) continue;
            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
            if (pdc.has(new NamespacedKey(Ultimate_market.plugin, "menu"), PersistentDataType.STRING)){
                has_menu = true;
                menu = pdc.get(new NamespacedKey(Ultimate_market.plugin, "menu"), PersistentDataType.STRING);
            }
        }

        if (!has_menu) return;

        Ultimate_market.gui.menus.get(menu).onItemClick(event);

//        if (!(event.getView().title() instanceof TextComponent)) return;
//
//        String inventoryName = executor.getMode().equals("LEGACY") ? event.getView().getTitle() : ((TextComponent) event.getView().title()).content();
//
//        boolean contains = false;
//
//        for (String s: executor.gui.titles){
//            if (s.startsWith(inventoryName) || (ChatColor.stripColor(inventoryName).startsWith(ChatColor.stripColor(s)))){
//                contains = true;
//                break;
//            }
//        }
//
//        if (!contains) return;
//
//
//        event.setCancelled(true);
//
//        if (executor.last_clicked.containsKey(playerName)){
//            /* milliseconds */
//            long pause = 100;
//            if (System.currentTimeMillis() - executor.last_clicked.get(playerName) < pause){
//                Bukkit.getPlayer(playerName).sendMessage(executor.gui.wait_before_clicking_again);
//                executor.last_clicked.put(playerName, System.currentTimeMillis());
//                return;
//            }
//        }
//
//        executor.last_clicked.put(playerName, System.currentTimeMillis());
//
//        int rawSlot = event.getRawSlot();
//
//        if (rawSlot >= 0 && rawSlot <= 53){
//            if (event.getCurrentItem() == null) return;
//            executor.onMenuItemClick(Objects.requireNonNull(Bukkit.getPlayer(playerName)), event.getCurrentItem(), event.getClick());
//        }
    }
}
