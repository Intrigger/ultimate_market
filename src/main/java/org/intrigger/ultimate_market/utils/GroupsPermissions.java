package org.intrigger.ultimate_market.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.intrigger.ultimate_market.Ultimate_market;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupsPermissions {
    public Map<String, Integer> maxItemsToSell;
    public Map<String, Integer> maxBuyRequests;
    public Map<String, Integer> maxBuyAmount;

    public GroupsPermissions(){

        maxItemsToSell = new HashMap<>();
        maxBuyRequests = new HashMap<>();
        maxBuyAmount = new HashMap<>();

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(new File(Ultimate_market.plugin.getDataFolder() + "/groups_config.yml"));
        ArrayList<String> groups = new ArrayList<>(configuration.getConfigurationSection("sell").getKeys(false));
        for (String group: groups){
            maxItemsToSell.put(group, configuration.getConfigurationSection("sell").getInt(group));
        }

        groups = new ArrayList<>(configuration.getConfigurationSection("buy").getKeys(false));
        for (String group: groups){
            maxBuyRequests.put(group, configuration.getInt("buy." + group + ".requests"));
            maxBuyAmount.put(group, configuration.getInt("buy." + group + ".amount"));
        }
    }
}
