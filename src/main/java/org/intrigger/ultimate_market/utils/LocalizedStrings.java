package org.intrigger.ultimate_market.utils;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocalizedStrings {
    public String mainMenuTitle;
    public String mySoldItemsTitle;
    public String itemCategoriesTitle;
    public String confirmBuyingMenuTitle;
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
    public List<String> wrongCommandUsage;
    public List<String> specifyThePrice;
    public List<String> theItemToBeSoldMustBeHeldInTheHand;
    public List<String> commandCanBeUsedOnlyByPlayer;
    public List<String> youSpecifiedWrongPrice;
    public List<String> negativePrice;
    public List<String> successfulPuttingUp;
    public List<String> zeroPriceNotice;
    public String itemAlreadySold;
    public String cannotByYourOwnItem;
    public String notEnoughMoney;
    public String freeUpInventorySpace;
    public String playerBoughtItemNotification;
    public String youBoughtItemNotification;
    public String youHaveWithdrawnItem;
    public String sortingTypeButtonTitle;
    public List<String> sortingTypeButtonLore;
    public String itemSoldLimitReached;
    public String confirmBuyingButtonTitle;
    public List<String> confirmBuyingButtonLore;

    public String selectAmountMenuTitle;
    public String cancelBuyingButtonTitle;
    public String buyEntirely;
    public String buyByPieces;
    public String pressLeftButton;
    public String pressRightButton;
    public List<String> cancelBuyingButtonLore;
    public ArrayList<String> titles;
    public LocalizedStrings(){

        if (!new File("plugins/Ultimate Market/localized_strings.yml").exists()){
            YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/Ultimate Market/localized_strings.yml"));
            config.set("titles.main_menu",   "&2&lUltimate &4&lMarket"  );
            config.set("titles.my_sold_items",   "&2&lMy Market"  );
            config.set("titles.item_categories",   "&2&lItem Categories"  );
            config.set("titles.confirm_buying_menu", "&2&lConfirm Buying");
            config.set("titles.select_amount_menu", "&2&lSelect Amount");
            
            config.set("strings.my_market_button.title",   "&6My Market"  );
            config.set("strings.my_market_button.lore", Arrays.asList(  "&aPress to see"  ,   "&ayour sold items"  ));

            config.set("strings.update_button.title",   "&6Update Page"  );
            config.set("strings.update_button.lore", Arrays.asList(  "&aPress to update"  ,   "&acurrent page"  ));

            config.set("strings.previous_page_button.title",   "&6Previous Page"  );
            config.set("strings.previous_page_button.lore", Arrays.asList(  "&aPress to go"  ,   "&aone page back"  ));

            config.set("strings.next_page_button.title",   "&6Next Page"  );
            config.set("strings.next_page_button.lore", Arrays.asList(  "&aPress to go"  ,   "&aone page forward"  ));

            config.set("strings.item_categories_button.title",   "&6Item Categories"  );
            config.set("strings.item_categories_button.lore", Arrays.asList(  "&aPress to open"  ,   "&aItem Categories menu"  ));

            config.set("strings.seller",   "&6Seller: &a"  );
            config.set("strings.price",   "&6Price: &a"  );
            config.set("strings.currency_symbol",   "&2$"  );

            config.set("strings.back_to_main_menu_button.title",   "&6Back To Main Menu"  );
            config.set("strings.back_to_main_menu_button.lore", Arrays.asList(  "&aPress to go"  ,   "&aback to Main Menu"  ));

            config.set("strings.press_to_withdraw_from_sale.lore", Arrays.asList(  "&aPress to withdraw"  ,   "&afrom sale"  ));

            config.set("strings.wrong_command_usage", Arrays.asList("&cWrong usage of command /ah (/market)",
                                                                    "&cTry like this: /ah <sell | buy | bid> <price> <full | solo>",
                                                                    "&7(hold the item you sold in your main hand)"));

            config.set("strings.specify_the_price", Arrays.asList("&cSpecify the price!",
                                                                  "&7e.g. /ah <sell> 500"));

            config.set("strings.the_item_to_be_sold_must_be_held_in_the_hand", Arrays.asList("&cThe item to be sold must be held in the hand!"));

            config.set("strings.command_can_be_used_only_by_player", Arrays.asList("&cThis command can be used only by player!"));

            config.set("strings.you_specified_wrong_price", Arrays.asList("&cYou specified wrong price!"));

            config.set("strings.negative_price", Arrays.asList("&cYou specified negative price!"));

            config.set("strings.successful_putting_up", Arrays.asList("&6You have successfully placed an item for sale"));

            config.set("strings.zero_price_notice", Arrays.asList("&7Since you have entered a zero price, you will not receive money for the sale of this item!"));

            config.set("strings.item_already_sold", "&4This item is already sold!");
            config.set("strings.cannot_by_your_own_item", "&4Cannot buy your own item!");

            config.set("strings.not_enough_money", "&4You have not enough money!");

            config.set("strings.free_up_inventory_space", "&4Free up inventory space to get this item!");
            config.set("strings.player_bought_item_notification", "&aPlayer &b{PLAYER} &abought &d{ITEM} &7(x&b{AMOUNT}&7) &afor &6{PRICE}{CURRENCY}");

            config.set("strings.you_bought_item_notification", "&aYou successfully bought &d{ITEM} &7(x&b{AMOUNT}&7) &afor &6{PRICE}{CURRENCY}");

            config.set("strings.you_have_withdrawn_item", "&aYou have withdrawn item from sale!");

            config.set("strings.sorting_type_button.title", "&6Sorting Type");
            config.set("strings.sorting_type_button.lore", Arrays.asList("&aNew First", "&aOld First","&aCheap First", "&aExpensive First"));

            config.set("strings.items_sold_limit_reached", "&cYou have reached the limit of items being sold at the same time!");

            config.set("strings.confirm_buying_button.title", "&2Confirm buying");
            config.set("strings.confirm_buying_button.lore", Arrays.asList("&7Press to confirm", "&7buying the item"));

            config.set("strings.cancel_buying_button.title", "&4Cancel buying");
            config.set("strings.cancel_buying_button.lore", Arrays.asList("&7Press to cancel", "&7buying the item"));

            config.set("strings.buy_entirely", "&6Entirely:");
            config.set("strings.buy_by_pieces", "&6By pieces:");

            config.set("strings.press_left_button", "&7 Press LMB");
            config.set("strings.press_right_button", "&7 Press RMB");

            try{
                config.save("plugins/Ultimate Market/localized_strings.yml");
            } catch (IOException e){
                e.printStackTrace();
            }


        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/Ultimate Market/localized_strings.yml"));

        mainMenuTitle = config.getString("titles.main_menu").replaceAll("&", "§");

        mySoldItemsTitle = config.getString("titles.my_sold_items").replaceAll("&", "§");
        itemCategoriesTitle = config.getString("titles.item_categories").replaceAll("&", "§");

        confirmBuyingMenuTitle = config.getString("titles.confirm_buying_menu").replaceAll("&", "§");

        myMarketButtonTitle = config.getString("strings.my_market_button.title").replaceAll("&", "§");
        
        myMarketButtonLore = config.getStringList("strings.my_market_button.lore");
        myMarketButtonLore.replaceAll(s -> s.replaceAll("&", "§"));

        updatePageButtonTitle = config.getString("strings.update_button.title").replaceAll("&", "§");
        
        updatePageButtonLore = config.getStringList("strings.update_button.lore");
        updatePageButtonLore.replaceAll(s -> s.replaceAll("&", "§"));
        

        previousPageButtonTitle = config.getString("strings.previous_page_button.title").replaceAll("&", "§");
        
        previousPageButtonLore = config.getStringList("strings.previous_page_button.lore");
        previousPageButtonLore.replaceAll(s -> s.replaceAll("&", "§"));

        nextPageButtonTitle = config.getString("strings.next_page_button.title").replaceAll("&", "§");
        
        nextPageButtonLore = config.getStringList("strings.next_page_button.lore");
        nextPageButtonLore.replaceAll(s -> s.replaceAll("&", "§"));

        itemCategoriesButtonTitle = config.getString("strings.item_categories_button.title").replaceAll("&", "§");
        
        itemCategoriesButtonLore = config.getStringList("strings.item_categories_button.lore");
        itemCategoriesButtonLore.replaceAll(s -> s.replaceAll("&", "§"));
        

        seller = config.getString("strings.seller").replaceAll("&", "§");
        price = config.getString("strings.price").replaceAll("&", "§");
        currency = config.getString("strings.currency_symbol").replaceAll("&", "§");

        backToMainMenuButtonTitle = config.getString("strings.back_to_main_menu_button.title").replaceAll("&", "§");

        backToMainMenuButtonLore = config.getStringList("strings.back_to_main_menu_button.lore");
        backToMainMenuButtonLore.replaceAll(s -> s.replaceAll("&", "§"));

        pressToWithdrawFromSaleLore = config.getStringList("strings.press_to_withdraw_from_sale.lore");
        pressToWithdrawFromSaleLore.replaceAll(s -> s.replaceAll("&", "§"));
        
        wrongCommandUsage = config.getStringList("strings.wrong_command_usage");
        wrongCommandUsage.replaceAll(s -> s.replaceAll("&", "§"));

        specifyThePrice = config.getStringList("strings.specify_the_price");
        specifyThePrice.replaceAll(s -> s.replaceAll("&", "§"));

        theItemToBeSoldMustBeHeldInTheHand = config.getStringList("strings.the_item_to_be_sold_must_be_held_in_the_hand");
        theItemToBeSoldMustBeHeldInTheHand.replaceAll(s -> s.replaceAll("&", "§"));

        commandCanBeUsedOnlyByPlayer = config.getStringList("strings.command_can_be_used_only_by_player");
        commandCanBeUsedOnlyByPlayer.replaceAll(s -> s.replaceAll("&", "§"));

        youSpecifiedWrongPrice = config.getStringList("strings.you_specified_wrong_price");
        youSpecifiedWrongPrice.replaceAll(s -> s.replaceAll("&", "§"));

        negativePrice = config.getStringList("strings.negative_price");
        negativePrice.replaceAll(s -> s.replaceAll("&", "§"));

        successfulPuttingUp = config.getStringList("strings.successful_putting_up");
        successfulPuttingUp.replaceAll(s -> s.replaceAll("&", "§"));

        zeroPriceNotice = config.getStringList("strings.zero_price_notice");
        zeroPriceNotice.replaceAll(s -> s.replaceAll("&", "§"));

        itemAlreadySold = config.getString("strings.item_already_sold").replaceAll("&", "§");

        cannotByYourOwnItem = config.getString("strings.cannot_by_your_own_item").replaceAll("&", "§");

        notEnoughMoney = config.getString("strings.not_enough_money").replaceAll("&", "§");

        freeUpInventorySpace = config.getString("strings.free_up_inventory_space").replaceAll("&", "§");

        playerBoughtItemNotification = config.getString("strings.player_bought_item_notification").replaceAll("&", "§");

        youBoughtItemNotification = config.getString("strings.you_bought_item_notification").replaceAll("&", "§");

        youHaveWithdrawnItem = config.getString("strings.you_have_withdrawn_item").replaceAll("&", "§");

        sortingTypeButtonTitle = config.getString("strings.sorting_type_button.title").replaceAll("&", "§");
        sortingTypeButtonLore = config.getStringList("strings.sorting_type_button.lore");
        sortingTypeButtonLore.replaceAll(s -> s.replaceAll("&", "§"));

        itemSoldLimitReached = config.getString("strings.items_sold_limit_reached").replaceAll("&", "§");

        confirmBuyingButtonTitle = config.getString("strings.confirm_buying_button.title").replaceAll("&", "§");
        confirmBuyingButtonLore =  config.getStringList("strings.confirm_buying_button.lore");
        confirmBuyingButtonLore.replaceAll(s -> s.replaceAll("&", "§"));

        cancelBuyingButtonTitle = config.getString("strings.cancel_buying_button.title").replaceAll("&", "§");
        cancelBuyingButtonLore =  config.getStringList("strings.cancel_buying_button.lore");
        cancelBuyingButtonLore.replaceAll(s -> s.replaceAll("&", "§"));

        selectAmountMenuTitle = config.getString("titles.select_amount_menu").replaceAll("&", "§");

        buyByPieces = config.getString("strings.buy_by_pieces").replaceAll("&", "§");
        buyEntirely = config.getString("strings.buy_entirely").replaceAll("&", "§");

        pressLeftButton = config.getString("strings.press_left_button").replaceAll("&", "§");
        pressRightButton = config.getString("strings.press_right_button").replaceAll("&", "§");

        titles = new ArrayList<>();
        titles.add(itemCategoriesTitle);
        titles.add(mainMenuTitle);
        titles.add(mySoldItemsTitle);
        titles.add(confirmBuyingMenuTitle);
        titles.add(selectAmountMenuTitle);
    }
}
