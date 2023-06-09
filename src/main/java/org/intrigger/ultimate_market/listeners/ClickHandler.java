package org.intrigger.ultimate_market.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.intrigger.ultimate_market.Ultimate_market;
import org.intrigger.ultimate_market.commands.MarketExecutor;

import java.util.Objects;

public class ClickHandler implements Listener {

    private static MarketExecutor executor;

    public ClickHandler(Ultimate_market _plugin, MarketExecutor marketExecutor){
        Bukkit.getPluginManager().registerEvents(this, _plugin);
        executor = marketExecutor;
    }

    @EventHandler
    public void processClick(InventoryClickEvent event){

        if (!(event.getView().title() instanceof TextComponent)) return;

        String inventoryName = event.getView().getTitle();

        boolean contains = false;

        for (String s: executor.localizedStrings.titles){
            if (s.equals(inventoryName)){
                contains = true;
                break;
            }
        }

        if (!contains) return;

        String playerName = event.getWhoClicked().getName();

        event.setCancelled(true);

        int rawSlot = event.getRawSlot();

        if (rawSlot >= 0 && rawSlot <= 53){
            if (event.getCurrentItem() == null) return;
            executor.onMenuItemClick(Objects.requireNonNull(Bukkit.getPlayer(playerName)), event.getCurrentItem(), event.getClick());
        }
    }
}
