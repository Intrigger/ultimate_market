package org.intrigger.ultimate_market.listeners;

import com.google.gson.JsonSerializer;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.intrigger.ultimate_market.Ultimate_market;
import org.intrigger.ultimate_market.commands.MarketExecutor;

import java.util.Objects;
import java.util.logging.Logger;

public class ClickHandler implements Listener {

    private static Logger LOGGER;
    private static Plugin plugin;

    private static MarketExecutor executor;

    public ClickHandler(Ultimate_market _plugin, Logger _logger, MarketExecutor marketExecutor){
        Bukkit.getPluginManager().registerEvents(this, _plugin);
        plugin = _plugin;
        LOGGER = _logger;
        executor = marketExecutor;
    }

    @EventHandler
    public void processClick(InventoryClickEvent event){

        if (!(event.getView().title() instanceof TextComponent)) return;

        Inventory inventory = event.getInventory();

        String inventoryName = ((TextComponent) event.getView().title()).content();
        if (!inventoryName.equals("Ultimate Market Menu")) return;

        event.setCancelled(true);

        int rawSlot = event.getRawSlot();

        if (rawSlot >= 0 && rawSlot <= 53){
            if (event.getCurrentItem() == null) return;
            executor.onMenuItemClick(Objects.requireNonNull(Bukkit.getPlayer(event.getWhoClicked().getName())), event.getCurrentItem());
        }
    }
}
