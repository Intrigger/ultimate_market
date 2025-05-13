package org.intrigger.ultimate_market.utils.Interface;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import java.util.Map;

public class Menu {
    public String name;
    public String title; //Display name
    public Map<String,Button> buttons;

    public Menu(String _name, String _title, Map<String, Button> _buttons) {
        name = _name;
        title = _title;
        buttons = _buttons;
    }

    public Inventory generateInventory(String playerName) {
        return null;
    }

    public void onItemClick(InventoryClickEvent event){ return;};
}
