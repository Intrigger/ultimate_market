package org.intrigger.ultimate_market.utils;

import org.bukkit.Material;

import java.util.ArrayList;

public class ItemFilterNotation {
    public String name;
    public String title;
    public int slot;
    public ArrayList<String> filters;
    public Material material;

    public ArrayList<String> lore;
    
    public ItemFilterNotation(String _name, String _title, ArrayList<String> _lore, int _slot, ArrayList<String> _filters, Material _material){
        name = _name;
        title = _title;
        lore = _lore;
        slot = _slot;
        filters = _filters;
        material = _material;
    }
    
}
