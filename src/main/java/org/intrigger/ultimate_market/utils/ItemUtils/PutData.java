package org.intrigger.ultimate_market.utils.ItemUtils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.intrigger.ultimate_market.Ultimate_market;
import org.intrigger.ultimate_market.utils.Pair;
import java.util.List;

public class PutData {
    public static ItemStack put(ItemStack item, String key, String val){
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(Ultimate_market.plugin, key);
        pdc.set(namespacedKey, PersistentDataType.STRING, val);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack put(ItemStack item, List<Pair<String, String>> map){
        for (Pair<String, String> p: map){
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            NamespacedKey namespacedKey = new NamespacedKey(Ultimate_market.plugin, p.first);
            pdc.set(namespacedKey, PersistentDataType.STRING, p.second);
            item.setItemMeta(meta);
        }
        return item;
    }
}
