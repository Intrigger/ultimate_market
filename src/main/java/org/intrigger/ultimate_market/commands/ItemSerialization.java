package org.intrigger.ultimate_market.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


public class ItemSerialization {
    public static ItemStack toInventory(FileConfiguration config, String path) {

        if (config.isItemStack(path + ".item")) {
            return config.getItemStack(path + ".item");
        }
        return new ItemStack(Material.AIR);
    }
    public static void saveInventory(ItemStack item, FileConfiguration config, String path, long price, String ownerName, long time) {
        if (item!=null) {
            config.set(path + ".price", price);
            config.set(path + ".item", item);
            config.set(path + ".owner", ownerName);
            config.set(path + ".time", time);
        }
    }
}