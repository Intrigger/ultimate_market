package org.intrigger.ultimate_market.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupsPermissions {
    public Map<String, Integer> maxItemsToSell;
    public GroupsPermissions(){

        maxItemsToSell = new HashMap<>();

        if (!new File("plugins/Ultimate Market/groups_config.yml").exists()) {
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(new File("plugins/Ultimate Market/groups_config.yml"));
            configuration.set("default", 5);
            configuration.set("any_other_group_here", 10);
            configuration.set("admin", Integer.MAX_VALUE);
            try{
                configuration.save("plugins/Ultimate Market/groups_config.yml");
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(new File("plugins/Ultimate Market/groups_config.yml"));
        ArrayList<String> groups = new ArrayList<>(configuration.getKeys(false));
        for (String group: groups){
            maxItemsToSell.put(group, configuration.getInt(group));
        }
    }
}
