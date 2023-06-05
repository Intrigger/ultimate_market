package org.intrigger.ultimate_market.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocalizedStrings {
    public String mainMenuTitle;
    public String mySoldItemsTitle;
    public String itemCategoriesTitle;
    public String myMarketButtonTitle;
    public List<String> myMarketButtonLore;

    public String updatePageButtonTitle;
    public List<String> updatePageButtonLore;

    public String previousPageButtonTitle;
    public List<String> previousPageButtonLore;

    public String nextPageButtonTitle;
    public List<String> nextPageButtonLore;

    public String itemCategoriesButtonTitle;
    public List<String> itemCategoriesButtonLore;

    public String seller;
    public String price;
    public String currency;

    public String backToMainMenuButtonTitle;
    public List<String> backToMainMenuButtonLore;
    public List<String> pressToWithdrawFromSaleLore;

    public ArrayList<String> titles;
    public LocalizedStrings(){

        if (!new File("plugins/Ultimate Market/localized_strings.yml").exists()){
            YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/Ultimate Market/localized_strings.yml"));
            config.set("titles.main_menu",   "Main Menu"  );
            config.set("titles.my_sold_items",   "My Sold Items"  );
            config.set("titles.item_categories",   "Item Categories"  );
            
            config.set("strings.my_market_button.title",   "My Market"  );
            config.set("strings.my_market_button.lore", Arrays.asList(  "Press to see"  ,   "your sold items"  ));

            config.set("strings.update_button.title",   "Update Page"  );
            config.set("strings.update_button.lore", Arrays.asList(  "Press to update"  ,   "current page"  ));

            config.set("strings.previous_page_button.title",   "Previous Page"  );
            config.set("strings.previous_page_button.lore", Arrays.asList(  "Press to go"  ,   "one page back"  ));

            config.set("strings.next_page_button.title",   "Next Page"  );
            config.set("strings.next_page_button.lore", Arrays.asList(  "Press to go"  ,   "one page forward"  ));

            config.set("strings.item_categories_button.title",   "Item Categories"  );
            config.set("strings.item_categories_button.lore", Arrays.asList(  "Press to open"  ,   "Item Categories menu"  ));

            config.set("strings.seller",   "Seller: "  );
            config.set("strings.price",   "Price: "  );
            config.set("strings.currency_symbol",   "$"  );

            config.set("strings.back_to_main_menu_button.title",   "Back To Main Menu"  );
            config.set("strings.back_to_main_menu_button.lore", Arrays.asList(  "Press to go"  ,   "back to Main Menu"  ));

            config.set("strings.press_to_withdraw_from_sale.lore", Arrays.asList(  "Press to withdraw"  ,   "from sale"  ));

            try{
                config.save("plugins/Ultimate Market/localized_strings.yml");
            } catch (IOException e){
                e.printStackTrace();
            }


        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/Ultimate Market/localized_strings.yml"));

        mainMenuTitle = config.getString("titles.main_menu");
        mySoldItemsTitle = config.getString("titles.my_sold_items");
        itemCategoriesTitle = config.getString("titles.item_categories");

        myMarketButtonTitle = config.getString("strings.my_market_button.title");
        myMarketButtonLore = config.getStringList("strings.my_market_button.lore");

        updatePageButtonTitle = config.getString("strings.update_button.title");
        updatePageButtonLore = config.getStringList("strings.update_button.lore");

        previousPageButtonTitle = config.getString("strings.previous_page_button.title");
        previousPageButtonLore = config.getStringList("strings.previous_page_button.lore");

        nextPageButtonTitle = config.getString("strings.next_page_button.title");
        nextPageButtonLore = config.getStringList("strings.next_page_button.lore");

        itemCategoriesButtonTitle = config.getString("strings.item_categories_button.title");
        itemCategoriesButtonLore = config.getStringList("strings.item_categories_button.lore");

        seller = config.getString("strings.seller");
        price = config.getString("strings.price");
        currency = config.getString("strings.currency_symbol");

        backToMainMenuButtonTitle = config.getString("strings.back_to_main_menu_button.title");
        backToMainMenuButtonLore = config.getStringList("strings.back_to_main_menu_button.lore");

        pressToWithdrawFromSaleLore = config.getStringList("strings.press_to_withdraw_from_sale.lore");

        titles = new ArrayList<>();
        titles.add(itemCategoriesTitle);
        titles.add(mainMenuTitle);
        titles.add(mySoldItemsTitle);
    }

    public ArrayList<String> getTitles(){
        return titles;
    }

}
