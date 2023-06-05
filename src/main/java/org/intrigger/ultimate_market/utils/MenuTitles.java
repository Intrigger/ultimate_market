package org.intrigger.ultimate_market.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.checkerframework.checker.units.qual.A;
import org.yaml.snakeyaml.Yaml;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MenuTitles {
    public String mainMenuTitle;
    public String mySoldItemsTitle;
    public String itemCategoriesTitle;
    public ArrayList<String> titles;
    public MenuTitles(){

        if (!new File("plugins/Ultimate Market/menu_titles.yml").exists()){
            try {
                System.out.println(ChatColor.GREEN + "Generating menu_titles.yml file!");
                BufferedWriter writer = new BufferedWriter(new FileWriter("plugins/Ultimate Market/menu_titles.yml"));

                writer.write("mainMenuTitle: " + "\"Market\"" + "\n");
                writer.write("mySoldItemsTitle: " + "\"My Sold Items\"" + "\n");
                writer.write("itemCategoriesTitle: " + "\"Item Categories\"" + "\n");

                writer.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        else{
            System.out.println(ChatColor.GREEN + "YAML File already exists!");
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/Ultimate Market/menu_titles.yml"));
        mainMenuTitle = config.getString("mainMenuTitle");
        mySoldItemsTitle = config.getString("mySoldItemsTitle");
        itemCategoriesTitle = config.getString("itemCategoriesTitle");


        titles = new ArrayList<>();
        titles.add(itemCategoriesTitle);
        titles.add(mainMenuTitle);
        titles.add(mySoldItemsTitle);
    }

    public ArrayList<String> getTitles(){
        return titles;
    }

}
