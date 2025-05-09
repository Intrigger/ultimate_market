package org.intrigger.ultimate_market.utils;

import org.bukkit.configuration.file.YamlConfiguration;
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

        if (!new File("plugins/Ultimate Market/groups_config.yml").exists()) {
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(new File("plugins/Ultimate Market/groups_config.yml"));

            configuration.set("sell.default", 5);
            configuration.set("sell.any_other_group_here", 10);
            configuration.set("sell.admin", Integer.MAX_VALUE);

            configuration.set("buy.default", 2);
            configuration.set("buy.any_other_group_here", 10);
            configuration.set("buy.admin", Integer.MAX_VALUE);

            try{
                configuration.save("plugins/Ultimate Market/groups_config.yml");
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(new File("plugins/Ultimate Market/groups_config.yml"));
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
