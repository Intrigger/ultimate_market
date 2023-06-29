package org.intrigger.ultimate_market.commands;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.checkerframework.checker.units.qual.A;
import org.intrigger.ultimate_market.Ultimate_market;
import org.intrigger.ultimate_market.utils.*;
import org.jetbrains.annotations.NotNull;

import net.milkbowl.vault.economy.Economy;
import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class MarketExecutor implements CommandExecutor  {

    public static Map<String, Inventory> menus;
    public static Map<String, String> playerCurrentMenu;

    private static Plugin plugin;

    private static ItemStorage storage;

    private static Map<String, Integer> playerCurrentPage;
    private static Map<String, String> playerCurrentItemFilter;
    public static ItemCategoriesProcessor itemCategoriesProcessor;

    public Map<String, Boolean> isMarketMenuOpen;

    public LocalizedStrings localizedStrings;

    private Map<String, String> playerCurrentSortingType;
    private GroupsPermissions groupsPermissions;
    private Map<String, ItemStackNotation> currentBuyingItem;
    private Map<String, Integer> currentBuyingItemAmount;

    public MarketExecutor(Plugin _plugin){
        menus = new HashMap<>();
        playerCurrentMenu = new HashMap<>();
        plugin = _plugin;
        storage = new ItemStorage("plugins/Ultimate Market/");
        playerCurrentPage = new HashMap<>();
        itemCategoriesProcessor = new ItemCategoriesProcessor("plugins/Ultimate Market/item_categories.yml");
        playerCurrentItemFilter = new HashMap<>();
        isMarketMenuOpen = new HashMap<>();
        localizedStrings = new LocalizedStrings();
        playerCurrentSortingType = new HashMap<>();
        groupsPermissions = new GroupsPermissions();
        currentBuyingItem = new HashMap<>();
        currentBuyingItemAmount = new HashMap<>();
    }

    public void closeDatabase(){
        storage.closeConnection();
    }

    int getTotalPages(int totalItemsNumber){
        return (int) (1 + Math.floor((double)totalItemsNumber / 45.0));
    }

    public Inventory generateMainMenu(String playerName, String filter, String sortingType){

        String mainMenuTitle = localizedStrings.mainMenuTitle;
        int inventorySize = 54;
        Inventory inventory = Bukkit.createInventory(null, inventorySize, mainMenuTitle);

        /*
            My Auction Slots Page Button
         */
        ItemStack mySlots = new ItemStack(Material.CHEST);
        ItemMeta mySlotsMeta = mySlots.getItemMeta();
        mySlotsMeta.setDisplayName(localizedStrings.myMarketButtonTitle);
        List<String> lore = localizedStrings.myMarketButtonLore;
        mySlotsMeta.setLore(lore);

        PersistentDataContainer pdc = mySlotsMeta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "MY_SOLD_ITEMS");
        mySlots.setItemMeta(mySlotsMeta);
        inventory.setItem(0, mySlots);

        /*
            Update Page Button
         */
        ItemStack updatePage = new ItemStack(Material.SLIME_BALL);
        ItemMeta updatePageMeta = updatePage.getItemMeta();
        updatePageMeta.setDisplayName(localizedStrings.updatePageButtonTitle);
        lore = localizedStrings.updatePageButtonLore;
        updatePageMeta.setLore(lore);

        pdc = updatePageMeta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "UPDATE_PAGE");
        updatePage.setItemMeta(updatePageMeta);
        inventory.setItem(4, updatePage);

        //
        // LEFT PAGE
        //
        ItemStack leftPage = new ItemStack(Material.PAPER);
        ItemMeta leftPageMeta = leftPage.getItemMeta();
        leftPageMeta.setDisplayName(localizedStrings.previousPageButtonTitle);
        lore = localizedStrings.previousPageButtonLore;
        leftPageMeta.setLore(lore);

        pdc = leftPageMeta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "PAGE_LEFT");
        leftPage.setItemMeta(leftPageMeta);
        inventory.setItem(3, leftPage);

        //
        // RIGHT PAGE
        //
        ItemStack rightPage = new ItemStack(Material.PAPER);
        ItemMeta rightPageMeta = rightPage.getItemMeta();
        rightPageMeta.setDisplayName(localizedStrings.nextPageButtonTitle);
        lore = localizedStrings.nextPageButtonLore;
        rightPageMeta.setLore(lore);

        pdc = rightPageMeta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "PAGE_RIGHT");
        rightPage.setItemMeta(rightPageMeta);
        inventory.setItem(5, rightPage);

        //
        // CATEGORIES PAGE
        //
        ItemStack categoriesPage = new ItemStack(Material.FEATHER);
        ItemMeta categoriesPageMeta = categoriesPage.getItemMeta();
        categoriesPageMeta.setDisplayName(localizedStrings.itemCategoriesButtonTitle);
        lore = localizedStrings.itemCategoriesButtonLore;
        categoriesPageMeta.setLore(lore);

        pdc = categoriesPageMeta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "CATEGORIES_MENU");
        categoriesPage.setItemMeta(categoriesPageMeta);
        inventory.setItem(8, categoriesPage);

        //
        // SORTING BUTTON
        //
        ItemStack sortingButton = new ItemStack(Material.HOPPER);
        ItemMeta sortingButtonMeta = sortingButton.getItemMeta();
        sortingButtonMeta.setDisplayName(localizedStrings.sortingTypeButtonTitle);
        lore = new ArrayList<>(localizedStrings.sortingTypeButtonLore);

        switch (playerCurrentSortingType.get(playerName)){
            case "NEW_FIRST":
                lore.set(0, "§6✓ " + localizedStrings.sortingTypeButtonLore.get(0));
                break;
            case "OLD_FIRST":
                lore.set(1, "§6✓ " + localizedStrings.sortingTypeButtonLore.get(1));
                break;
            case "CHEAP_FIRST":
                lore.set(2, "§6✓ " + localizedStrings.sortingTypeButtonLore.get(2));
                break;
            case "EXPENSIVE_FIRST":
                lore.set(3, "§6✓ " + localizedStrings.sortingTypeButtonLore.get(3));
                break;
        }

        sortingButtonMeta.setLore(lore);

        pdc = sortingButtonMeta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "SORTING_BUTTON");
        sortingButton.setItemMeta(sortingButtonMeta);
        inventory.setItem(7, sortingButton);

        /*
            Sorting items from 'items sold file' by time
         */

        ArrayList<ItemStackNotation> queryResult;

        if (filter == null) queryResult = storage.getAllKeys(playerCurrentPage.get(playerName), playerCurrentSortingType.get(playerName));
        else queryResult = storage.getAllItemsFiltered(itemCategoriesProcessor.filterNotations.get(filter).filters, playerCurrentPage.get(playerName), sortingType);

        if (queryResult != null){
            int currentSlot = 9;


            for (ItemStackNotation itemStackNotation : queryResult) {
                if (currentSlot > 53) break;

                ItemStack currentItemStack = ItemStack.deserializeBytes(itemStackNotation.bytes);

                String owner = itemStackNotation.owner;
                float price = itemStackNotation.price;
                ArrayList<String> newLore = new ArrayList<>();
                newLore.add(localizedStrings.seller + owner);
                if (itemStackNotation.full == 1){
                    newLore.add(localizedStrings.buyEntirely + " " + price + localizedStrings.currency + " " + localizedStrings.pressLeftButton);
                }
                else{
                    newLore.add(localizedStrings.buyEntirely + " " + String.format("%.0f", Math.ceil((double)price  * (double)itemStackNotation.amount)) + localizedStrings.currency + " " + localizedStrings.pressLeftButton);
                    newLore.add(localizedStrings.buyByPieces + " " + String.format("%.3f",(price)) + localizedStrings.currency + " " + localizedStrings.pressRightButton);
                }

                ItemMeta currentItemMeta = currentItemStack.getItemMeta();
                List<String> currentLore = currentItemStack.getLore();

                if (currentLore != null)
                    newLore.addAll(currentLore);

                currentItemMeta.setLore(newLore);
                currentItemStack.setItemMeta(currentItemMeta);
                inventory.setItem(currentSlot, currentItemStack);
                currentSlot++;
            }
        }

        menus.put("MAIN_MENU", inventory);

        return inventory;
    }

    public Inventory generateFiltersInventory(){

        String inventoryName = localizedStrings.itemCategoriesTitle;
        int inventorySize = 54;
        Inventory inventory = Bukkit.createInventory(null, inventorySize, inventoryName);

        ItemStack homeItem = new ItemStack(Material.CHEST);

        ItemMeta mySlotsMeta = homeItem.getItemMeta();
        mySlotsMeta.setDisplayName(localizedStrings.backToMainMenuButtonTitle);
        List<String> lore = localizedStrings.backToMainMenuButtonLore;
        mySlotsMeta.setLore(lore);
        homeItem.setItemMeta(mySlotsMeta);
        ItemMeta homeItemMeta = homeItem.getItemMeta();

        PersistentDataContainer pdc = homeItemMeta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(Ultimate_market.plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "MAIN_MENU");

        homeItem.setItemMeta(homeItemMeta);
        inventory.setItem(0, homeItem);

        for (Map.Entry<String, ItemFilterNotation> entry: itemCategoriesProcessor.filterNotations.entrySet()){
            ItemFilterNotation filterNotation = entry.getValue();
            String filterKey = filterNotation.title;
            int slot = filterNotation.slot;
            ItemStack currentItem = new ItemStack(filterNotation.material);

            ItemMeta newItemMeta = currentItem.getItemMeta();
            newItemMeta.setDisplayName(filterKey);
            newItemMeta.setLore(filterNotation.lore);

            pdc = newItemMeta.getPersistentDataContainer();
            pdc.set(namespacedKey, PersistentDataType.STRING, "FILTER:" + filterNotation.name);

            currentItem.setItemMeta(newItemMeta);
            inventory.setItem(slot, currentItem);
        }

        return inventory;
    }

    public Inventory generateMySoldItemsMenu(Player player){
        String inventoryName = localizedStrings.mySoldItemsTitle;
        int inventorySize = 54;
        Inventory inventory = Bukkit.createInventory(null, inventorySize, inventoryName);

        ArrayList<ItemStackNotation> myItems = storage.getPlayerItems(player.getName(), playerCurrentPage.get(player.getName()));

        ItemStack homeItem = new ItemStack(Material.CHEST);

        ItemMeta mySlotsMeta = homeItem.getItemMeta();
        mySlotsMeta.setDisplayName(localizedStrings.backToMainMenuButtonTitle);
        List<String> lore = localizedStrings.backToMainMenuButtonLore;
        mySlotsMeta.setLore(lore);
        homeItem.setItemMeta(mySlotsMeta);
        ItemMeta homeItemMeta = homeItem.getItemMeta();

        PersistentDataContainer pdc = homeItemMeta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "MAIN_MENU");

        homeItem.setItemMeta(homeItemMeta);
        inventory.setItem(0, homeItem);

        /*
            Update Page Button
         */
        ItemStack updatePage = new ItemStack(Material.SLIME_BALL);
        ItemMeta updatePageMeta = updatePage.getItemMeta();
        updatePageMeta.setDisplayName(localizedStrings.updatePageButtonTitle);
        lore = localizedStrings.updatePageButtonLore;
        updatePageMeta.setLore(lore);

        pdc = updatePageMeta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "UPDATE_PAGE");
        updatePage.setItemMeta(updatePageMeta);
        inventory.setItem(4, updatePage);

        //
        // LEFT PAGE
        //
        ItemStack leftPage = new ItemStack(Material.PAPER);
        ItemMeta leftPageMeta = leftPage.getItemMeta();
        leftPageMeta.setDisplayName(localizedStrings.previousPageButtonTitle);
        lore = localizedStrings.previousPageButtonLore;
        leftPageMeta.setLore(lore);

        pdc = leftPageMeta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "PAGE_LEFT");
        leftPage.setItemMeta(leftPageMeta);
        inventory.setItem(3, leftPage);

        //
        // RIGHT PAGE
        //
        ItemStack rightPage = new ItemStack(Material.PAPER);
        ItemMeta rightPageMeta = rightPage.getItemMeta();
        rightPageMeta.setDisplayName(localizedStrings.nextPageButtonTitle);
        lore = localizedStrings.nextPageButtonLore;
        rightPageMeta.setLore(lore);

        pdc = rightPageMeta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "PAGE_RIGHT");
        rightPage.setItemMeta(rightPageMeta);
        inventory.setItem(5, rightPage);


        if (myItems != null){
            int keysSize = myItems.size();

            for (int key = 0; key < Math.min(54-9, keysSize); key++){
                ItemStackNotation itemStackNotation = myItems.get(key);
                ItemStack currentItemStack = (ItemStack.deserializeBytes(itemStackNotation.bytes));
                ArrayList<String> newLore = new ArrayList<>();
                float price = itemStackNotation.price;

                List<String> currentLore = currentItemStack.getLore();

                if (itemStackNotation.full == 1){
                    newLore.add(localizedStrings.buyEntirely + " " + price + localizedStrings.currency);
                }
                else{
                    newLore.add(localizedStrings.buyEntirely + " " + String.format("%.0f", Math.ceil((double)price  * (double)itemStackNotation.amount)) + localizedStrings.currency);
                    newLore.add(localizedStrings.buyByPieces + " " + String.format("%.3f", price) + localizedStrings.currency);
                }
                if (currentLore != null) newLore.addAll(currentLore);
                newLore.addAll(localizedStrings.pressToWithdrawFromSaleLore);

                currentItemStack.setLore(newLore);
                inventory.setItem(key + 9, currentItemStack);
            }
        }

        menus.put("MY_SOLD_ITEMS", inventory);

        return inventory;
    }

    public Inventory generateConfirmFullMenu(String playerName, ItemStackNotation item){
        String inventoryName = localizedStrings.confirmBuyingMenuTitle;
        int inventorySize = 54;
        Inventory inventory = Bukkit.createInventory(null, inventorySize, inventoryName);
        
        //
        // GREEN BUTTON
        //
        ItemStack green_button = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta green_button_meta = green_button.getItemMeta();
        green_button_meta.setDisplayName(localizedStrings.confirmBuyingButtonTitle);
        List<String> lore = localizedStrings.confirmBuyingButtonLore;
        green_button_meta.setLore(lore);
        green_button.setItemMeta(green_button_meta);
        green_button_meta = green_button.getItemMeta();

        PersistentDataContainer pdc = green_button_meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "CONFIRM_BUYING");
        green_button.setItemMeta(green_button_meta);
        //
        // RED BUTTON
        //
        ItemStack red_button = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta red_button_meta = red_button.getItemMeta();
        red_button_meta.setDisplayName(localizedStrings.cancelBuyingButtonTitle);
        lore = localizedStrings.cancelBuyingButtonLore;
        red_button_meta.setLore(lore);
        red_button.setItemMeta(red_button_meta);
        red_button_meta = red_button.getItemMeta();

        pdc = red_button_meta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "CANCEL_BUYING");
        red_button.setItemMeta(red_button_meta);
        //
        // BLACK BUTTON (DOING NOTHING)
        //
        ItemStack black_button = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta black_button_meta = black_button.getItemMeta();
        black_button_meta.setDisplayName(" ");
        lore = Arrays.asList(" ");
        black_button_meta.setLore(lore);
        black_button.setItemMeta(black_button_meta);
        
        for (int row = 0; row <= 4; row++){
            for (int column = 0; column < 9; column++){
                if (column == 4) {
                    inventory.setItem(row * 9 + column, black_button);
                    continue;
                }
                if (column < 4){
                    inventory.setItem(row * 9 + column, green_button);
                }
                else{
                    inventory.setItem(row * 9 + column, red_button);
                }
            }
        }

        for (int column = 0; column < 9; column++){
            inventory.setItem(5 * 9 + column, black_button);
        }

        ItemStack item_ = ItemStack.deserializeBytes(item.bytes);

        item_.setAmount(currentBuyingItemAmount.get(playerName));

        inventory.setItem(2 * 9 + 4, item_);

        ItemStack homeItem = new ItemStack(Material.CHEST);

        ItemMeta mySlotsMeta = homeItem.getItemMeta();
        mySlotsMeta.setDisplayName(localizedStrings.backToMainMenuButtonTitle);
        lore = localizedStrings.backToMainMenuButtonLore;
        mySlotsMeta.setLore(lore);
        homeItem.setItemMeta(mySlotsMeta);
        ItemMeta homeItemMeta = homeItem.getItemMeta();

        pdc = homeItemMeta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "MAIN_MENU");

        homeItem.setItemMeta(homeItemMeta);
        inventory.setItem(0, homeItem);
        
        return inventory;
    }

    public Inventory generateSelectAmountMenu(String playerName, ItemStackNotation item){
        String inventoryName = localizedStrings.selectAmountMenuTitle;
        int inventorySize = 54;
        Inventory inventory = Bukkit.createInventory(null, inventorySize, inventoryName);

        List<String> lore;
        //
        // BLACK BUTTON (DOING NOTHING)
        //
        ItemStack black_button = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta black_button_meta = black_button.getItemMeta();
        black_button_meta.setDisplayName(" ");
        lore = Arrays.asList(" ");
        black_button_meta.setLore(lore);
        black_button.setItemMeta(black_button_meta);

        //
        // GREEN BUTTON + 1
        //
        ItemStack green_button_1 = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta green_button_1_meta = green_button_1.getItemMeta();
        green_button_1_meta.setDisplayName("§2+1");
        lore = Arrays.asList(" ");
        green_button_1_meta.setLore(lore);
        green_button_1.setItemMeta(green_button_1_meta);
        green_button_1_meta = green_button_1.getItemMeta();

        PersistentDataContainer pdc = green_button_1_meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "AMOUNT_PLUS_1");
        green_button_1.setItemMeta(green_button_1_meta);

        //
        // GREEN BUTTON + 2
        //
        ItemStack green_button_2 = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta green_button_2_meta = green_button_2.getItemMeta();
        green_button_2_meta.setDisplayName("§2+2");
        lore = Arrays.asList(" ");
        green_button_2_meta.setLore(lore);
        green_button_2.setItemMeta(green_button_2_meta);
        green_button_2_meta = green_button_2.getItemMeta();

        pdc = green_button_2_meta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "AMOUNT_PLUS_2");
        green_button_2.setItemMeta(green_button_2_meta);

        //
        // GREEN BUTTON + 4
        //
        ItemStack green_button_4 = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta green_button_4_meta = green_button_4.getItemMeta();
        green_button_4_meta.setDisplayName("§2+4");
        lore = Arrays.asList(" ");
        green_button_4_meta.setLore(lore);
        green_button_4.setItemMeta(green_button_4_meta);
        green_button_4_meta = green_button_4.getItemMeta();

        pdc = green_button_4_meta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "AMOUNT_PLUS_4");
        green_button_4.setItemMeta(green_button_4_meta);

        //
        // GREEN BUTTON + 8
        //
        ItemStack green_button_8 = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta green_button_8_meta = green_button_8.getItemMeta();
        green_button_8_meta.setDisplayName("§2+8");
        lore = Arrays.asList(" ");
        green_button_8_meta.setLore(lore);
        green_button_8.setItemMeta(green_button_8_meta);
        green_button_8_meta = green_button_8.getItemMeta();

        pdc = green_button_8_meta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "AMOUNT_PLUS_8");
        green_button_8.setItemMeta(green_button_8_meta);

        //
        // RED BUTTON - 1
        //
        ItemStack red_button_1 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta red_button_1_meta = red_button_1.getItemMeta();
        red_button_1_meta.setDisplayName("§4-1");
        lore = Arrays.asList(" ");
        red_button_1_meta.setLore(lore);
        red_button_1.setItemMeta(red_button_1_meta);
        red_button_1_meta = red_button_1.getItemMeta();

        pdc = red_button_1_meta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "AMOUNT_MINUS_1");
        red_button_1.setItemMeta(red_button_1_meta);

        //
        // RED BUTTON - 2
        //
        ItemStack red_button_2 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta red_button_2_meta = red_button_2.getItemMeta();
        red_button_2_meta.setDisplayName("§4-2");
        lore = Arrays.asList(" ");
        red_button_2_meta.setLore(lore);
        red_button_2.setItemMeta(red_button_2_meta);
        red_button_2_meta = red_button_2.getItemMeta();

        pdc = red_button_2_meta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "AMOUNT_MINUS_2");
        red_button_2.setItemMeta(red_button_2_meta);

        //
        // RED BUTTON - 4
        //
        ItemStack red_button_4 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta red_button_4_meta = red_button_4.getItemMeta();
        red_button_4_meta.setDisplayName("§4-4");
        lore = Arrays.asList(" ");
        red_button_4_meta.setLore(lore);
        red_button_4.setItemMeta(red_button_4_meta);
        red_button_4_meta = red_button_4.getItemMeta();

        pdc = red_button_4_meta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "AMOUNT_MINUS_4");
        red_button_4.setItemMeta(red_button_4_meta);

        //
        // RED BUTTON - 8
        //
        ItemStack red_button_8 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta red_button_8_meta = red_button_8.getItemMeta();
        red_button_8_meta.setDisplayName("§4-8");
        lore = Arrays.asList(" ");
        red_button_8_meta.setLore(lore);
        red_button_8.setItemMeta(red_button_8_meta);
        red_button_8_meta = red_button_8.getItemMeta();

        pdc = red_button_8_meta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "AMOUNT_MINUS_8");
        red_button_8.setItemMeta(red_button_8_meta);

        for (int i = 0; i < 6; i++){
            for (int j = 0; j < 9; j++){
                inventory.setItem(i * 9 + j, black_button);
            }
        }

        ItemStack item_ = ItemStack.deserializeBytes(item.bytes);
        item_.setAmount(currentBuyingItemAmount.get(playerName));

        inventory.setItem(2 * 9 + 4, item_);

        inventory.setItem(2 * 9 + 4 + 1, green_button_1);
        inventory.setItem(2 * 9 + 4 + 2, green_button_2);
        inventory.setItem(2 * 9 + 4 + 3, green_button_4);
        inventory.setItem(2 * 9 + 4 + 4, green_button_8);

        inventory.setItem(2 * 9 + 4 - 1, red_button_1);
        inventory.setItem(2 * 9 + 4 - 2, red_button_2);
        inventory.setItem(2 * 9 + 4 - 3, red_button_4);
        inventory.setItem(2 * 9 + 4 - 4, red_button_8);

        //
        // BUY BUTTON
        //
        ItemStack buy_button = new ItemStack(Material.EMERALD);
        ItemMeta buy_button_meta = buy_button.getItemMeta();
        buy_button_meta.setDisplayName(localizedStrings.confirmBuyingButtonTitle);
        lore = localizedStrings.confirmBuyingButtonLore;
        buy_button_meta.setLore(lore);
        buy_button.setItemMeta(buy_button_meta);
        buy_button_meta = buy_button.getItemMeta();

        pdc = buy_button_meta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "BUY_BUTTON");
        buy_button.setItemMeta(buy_button_meta);

        inventory.setItem(4 * 9 + 4, buy_button);

        ItemStack homeItem = new ItemStack(Material.CHEST);

        ItemMeta mySlotsMeta = homeItem.getItemMeta();
        mySlotsMeta.setDisplayName(localizedStrings.backToMainMenuButtonTitle);
        lore = localizedStrings.backToMainMenuButtonLore;
        mySlotsMeta.setLore(lore);
        homeItem.setItemMeta(mySlotsMeta);
        ItemMeta homeItemMeta = homeItem.getItemMeta();

        pdc = homeItemMeta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "MAIN_MENU");

        homeItem.setItemMeta(homeItemMeta);
        inventory.setItem(0, homeItem);

        return inventory;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player)){
            for (String temp: localizedStrings.commandCanBeUsedOnlyByPlayer)
                commandSender.sendMessage(temp);
            return true;
        }

        Player player = (Player) commandSender;
        String playerName = player.getName();

        if (strings.length == 0){
            playerCurrentMenu.put(playerName, "MAIN_MENU");
            playerCurrentPage.put(playerName, 0);
            playerCurrentSortingType.put(playerName, "NEW_FIRST");
            playerCurrentItemFilter.put(player.getName(), null);
            isMarketMenuOpen.put(playerName, true);
            player.openInventory(generateMainMenu(player.getName(), null, playerCurrentSortingType.get(playerName)));
        } else if (strings.length == 1) {
            if (!MarketTabComplete.list.get(0).contains(strings[0])){
                for (String temp: localizedStrings.wrongCommandUsage){
                    player.sendMessage(temp);
                }
            }
            else{
                for (String temp: localizedStrings.specifyThePrice){
                    player.sendMessage(temp);
                }
            }
        }
        else if (strings.length == 2){
            ItemStack itemToSell = player.getItemInHand();

            if (player.getItemInHand().getTranslationKey().equals("block.minecraft.air")){
                for (String temp: localizedStrings.theItemToBeSoldMustBeHeldInTheHand){
                    player.sendMessage(temp);
                }
                return true;
            }

            String priceStr = strings[1];
            String priceStrCopy = priceStr;
            double priceDouble;

            for (String el : Arrays.asList("k", "K", "kk", "KK", "m", "M", "kkk", "b", "KKK", "B")){
                priceStrCopy = priceStrCopy.replaceAll(el, "");
            }

            priceDouble = Double.parseDouble(priceStrCopy);

            if (priceStr.endsWith("kkk") || (priceStr.endsWith("b")) || priceStr.endsWith("KKK") || (priceStr.endsWith("B"))){
                priceDouble *= 1000000000;
            }
            else if (priceStr.endsWith("kk") || (priceStr.endsWith("m")) || (priceStr.endsWith("KK")) || (priceStr.endsWith("M"))){
                priceDouble *= 1000000;
            }
            else if (priceStr.endsWith("k") || (priceStr.endsWith("K"))){
                priceDouble *= 1000;
            }
            else{
                if (!NumberUtils.isNumber(priceStr)){
                    for (String temp: localizedStrings.youSpecifiedWrongPrice){
                        player.sendMessage(temp);
                    }
                    return true;
                }
            }


            if (priceDouble < 0){
                for (String temp: localizedStrings.negativePrice){
                    player.sendMessage(temp);
                }
                return true;
            }

            String playerGroup = "default";

            ArrayList<String> groups = new ArrayList<>(groupsPermissions.maxItemsToSell.keySet());

            for (int i = groups.size() - 1; i >= 0; i--){
                String group = groups.get(i);
                if (player.hasPermission("group." + group)){
                    playerGroup = group;
                    break;
                }
            }


            if (storage.playerItemsSoldNow(playerName) >= groupsPermissions.maxItemsToSell.get(playerGroup)){
                player.sendMessage(localizedStrings.itemSoldLimitReached);
                return true;
            }

            long price = Math.round(priceDouble);

            String unique_key = new Random().ints('a', 'z' + 1).limit(64).collect(StringBuilder::new,
                    StringBuilder::appendCodePoint, StringBuilder::append).toString();

            ItemMeta meta = itemToSell.getItemMeta();

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
            pdc.set(namespacedKey, PersistentDataType.STRING, unique_key);

            itemToSell.setItemMeta(meta);

            int sell_full = 0;

            storage.addItem(unique_key, player.getName(), ((float) price / (float) itemToSell.getAmount()), System.nanoTime(), itemToSell.getType().toString(), itemToSell, itemToSell.getAmount(), sell_full);

            player.getItemInHand().setAmount(0);
            for (String temp: localizedStrings.successfulPuttingUp){
                player.sendMessage(temp);
            }

            if (price == 0){
                for (String temp: localizedStrings.zeroPriceNotice){
                    player.sendMessage(temp);
                }
            }

        }
        else if (strings.length == 3){
            String arg3 = strings[2];
            if (!MarketTabComplete.list.get(2).contains(arg3)){
                for (String temp: localizedStrings.wrongCommandUsage){
                    player.sendMessage(temp);
                }
                return true;
            }
            ItemStack itemToSell = player.getItemInHand();

            if (player.getItemInHand().getTranslationKey().equals("block.minecraft.air")){
                for (String temp: localizedStrings.theItemToBeSoldMustBeHeldInTheHand){
                    player.sendMessage(temp);
                }
                return true;
            }

            String priceStr = strings[1];

            if (!NumberUtils.isNumber(priceStr)){
                for (String temp: localizedStrings.youSpecifiedWrongPrice){
                    player.sendMessage(temp);
                }
                return true;
            }

            double priceDouble = Double.parseDouble(priceStr);

            if (priceDouble < 0){
                for (String temp: localizedStrings.negativePrice){
                    player.sendMessage(temp);
                }
                return true;
            }

            String playerGroup = "default";

            ArrayList<String> groups = new ArrayList<>(groupsPermissions.maxItemsToSell.keySet());

            for (int i = groups.size() - 1; i >= 0; i--){
                String group = groups.get(i);
                if (player.hasPermission("group." + group)){
                    playerGroup = group;
                    break;
                }
            }


            if (storage.playerItemsSoldNow(playerName) >= groupsPermissions.maxItemsToSell.get(playerGroup)){
                player.sendMessage(localizedStrings.itemSoldLimitReached);
                return true;
            }

            long price = Math.round(priceDouble);

            String unique_key = new Random().ints('a', 'z' + 1).limit(64).collect(StringBuilder::new,
                    StringBuilder::appendCodePoint, StringBuilder::append).toString();

            ItemMeta meta = itemToSell.getItemMeta();

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
            pdc.set(namespacedKey, PersistentDataType.STRING, unique_key);

            itemToSell.setItemMeta(meta);

            int sell_full = arg3.equalsIgnoreCase("full") ? 1 : 0;

            storage.addItem(unique_key, player.getName(), sell_full == 1 ? price : (float) price / (float) itemToSell.getAmount(), System.nanoTime(), itemToSell.getType().toString(), itemToSell, itemToSell.getAmount(), sell_full);

            player.getItemInHand().setAmount(0);
            for (String temp: localizedStrings.successfulPuttingUp){
                player.sendMessage(temp);
            }

            if (price == 0){
                for (String temp: localizedStrings.zeroPriceNotice){
                    player.sendMessage(temp);
                }
            }

        }
        return true;
    }

    public void onMenuItemClick(Player player, ItemStack item, ClickType clickEvent){

        String playerName = player.getName();
        String currentMenu = playerCurrentMenu.get(playerName);


        switch (currentMenu) {
            case "MAIN_MENU": {
                PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
                if (pdc.has(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING)) {
                    String menu_item_key = pdc.get(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING);
                    assert menu_item_key != null;

                    String currentFilter = playerCurrentItemFilter.get(playerName);
                    ArrayList<String> filters;
                    if (currentFilter == null) filters = null;
                    else {
                        filters = itemCategoriesProcessor.filterNotations.get(currentFilter).filters;
                    }

                    int pagesNum = getTotalPages(storage.getTotalItems(filters)) - 1;

                    int currentPage = playerCurrentPage.get(playerName);

                    switch (menu_item_key) {
                        case "MY_SOLD_ITEMS":  // my sold items
                            playerCurrentMenu.put(playerName, "MY_SOLD_ITEMS");
                            playerCurrentPage.put(playerName, 0);
                            player.openInventory(generateMySoldItemsMenu(player));
                            break;
                        case "UPDATE_PAGE":
                            playerCurrentPage.put(playerName, Math.max(0, Math.min(currentPage, pagesNum)));
                            player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName), playerCurrentSortingType.get(playerName)));
                            break;
                        case "PAGE_LEFT":
                            playerCurrentPage.put(playerName, Math.max(0, Math.min(currentPage - 1, pagesNum)));
                            player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName), playerCurrentSortingType.get(playerName)));
                            break;
                        case "PAGE_RIGHT":
                            playerCurrentPage.put(playerName, Math.max(0, Math.min(currentPage + 1, pagesNum)));
                            player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName), playerCurrentSortingType.get(playerName)));
                            break;
                        case "CATEGORIES_MENU":
                            playerCurrentMenu.put(playerName, "CATEGORIES_MENU");
                            playerCurrentItemFilter.put(playerName, null);
                            player.openInventory(generateFiltersInventory());
                            break;
                        case "SORTING_BUTTON":
                            String newSortingType = "";
                            if (clickEvent.isLeftClick()){
                                switch (playerCurrentSortingType.get(playerName)){
                                    case "NEW_FIRST":
                                        newSortingType = "OLD_FIRST";
                                        break;
                                    case "OLD_FIRST":
                                        newSortingType = "CHEAP_FIRST";
                                        break;
                                    case "CHEAP_FIRST":
                                        newSortingType = "EXPENSIVE_FIRST";
                                        break;
                                    case "EXPENSIVE_FIRST":
                                        newSortingType = "NEW_FIRST";
                                        break;
                                }
                            }
                            else if (clickEvent.isRightClick()){
                                switch (playerCurrentSortingType.get(playerName)){
                                    case "NEW_FIRST":
                                        newSortingType = "EXPENSIVE_FIRST";
                                        break;
                                    case "OLD_FIRST":
                                        newSortingType = "NEW_FIRST";
                                        break;
                                    case "CHEAP_FIRST":
                                        newSortingType = "OLD_FIRST";
                                        break;
                                    case "EXPENSIVE_FIRST":
                                        newSortingType = "CHEAP_FIRST";
                                        break;
                                }
                            }

                            playerCurrentSortingType.put(playerName, newSortingType);
                            player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName), playerCurrentSortingType.get(playerName)));
                            break;
                    }
                } else {
                    if (getServer().getPluginManager().getPlugin("Vault") == null) {
                        return;
                    }
                    RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
                    if (rsp == null) {
                        return;
                    }
                    long balance = (long) rsp.getProvider().getBalance(playerName);

                    String unique_key = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "unique_key"), PersistentDataType.STRING);

                    if (storage.getItem(unique_key) == null) {
                        player.sendMessage(localizedStrings.itemAlreadySold);
                        player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName), playerCurrentSortingType.get(playerName)));
                        return;
                    }

                    ItemStackNotation notation = storage.getItem(unique_key);
                    String itemOwner = notation.owner;
                    float price = notation.price;

                    if (Objects.equals(itemOwner, playerName)) {
                        player.sendMessage(localizedStrings.cannotByYourOwnItem);
                        return;
                    }

                    if (notation.full == 0){
                        if (clickEvent.isLeftClick()){
                            currentBuyingItemAmount.put(playerName, notation.amount);
                            playerCurrentMenu.put(playerName, "CONFIRM_MENU");
                            currentBuyingItem.put(playerName, notation);
                            player.openInventory(generateConfirmFullMenu(playerName, notation));
                        }
                        else if (clickEvent.isRightClick()){
                            playerCurrentMenu.put(playerName, "SELECT_AMOUNT_MENU");
                            currentBuyingItem.put(playerName, notation);
                            currentBuyingItemAmount.put(playerName, 1);
                            player.openInventory(generateSelectAmountMenu(playerName, notation));
                        }
                    }
                    else{
                        if (!(balance >= price)) {
                            player.sendMessage(localizedStrings.notEnoughMoney);
                            return;
                        }
                        currentBuyingItemAmount.put(playerName, notation.amount);
                        playerCurrentMenu.put(playerName, "CONFIRM_MENU");
                        currentBuyingItem.put(playerName, notation);
                        player.openInventory(generateConfirmFullMenu(playerName, notation));
                    }

                }
                break;
            }
            case "MY_SOLD_ITEMS": {

                PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
                if (pdc.has(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING)) {
                    String menu_item_key = pdc.get(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING);
                    if (menu_item_key.equals("MAIN_MENU")) {
                        player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName), playerCurrentSortingType.get(playerName)));
                        playerCurrentMenu.put(playerName, "MAIN_MENU");
                    }
                    else if (menu_item_key.equals("UPDATE_PAGE")){
                        player.openInventory(generateMySoldItemsMenu(player));
                    }
                    else if (menu_item_key.equals("PAGE_LEFT")){
                        int pagesNum = getTotalPages(storage.playerItemsSoldNow(playerName)) - 1;
                        int currentPage = playerCurrentPage.get(playerName);
                        playerCurrentPage.put(playerName, Math.max(0, Math.min(currentPage - 1, pagesNum)));
                        player.openInventory(generateMySoldItemsMenu(player));
                    }
                    else if (menu_item_key.equals("PAGE_RIGHT")){
                        int pagesNum = getTotalPages(storage.playerItemsSoldNow(playerName)) - 1;
                        int currentPage = playerCurrentPage.get(playerName);
                        playerCurrentPage.put(playerName, Math.max(0, Math.min(currentPage + 1, pagesNum)));
                        player.openInventory(generateMySoldItemsMenu(player));
                    }
                } else {
                    boolean hasEmptySlot = false;

                    for (int slot = 35; slot >= 0; slot--) {
                        if (player.getInventory().getItem(slot) == null) {

                            String unique_key = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "unique_key"), PersistentDataType.STRING);

                            if (storage.getItem(unique_key) == null) {
                                player.sendMessage(localizedStrings.itemAlreadySold);
                                player.openInventory(generateMySoldItemsMenu(player));
                                return;
                            }

                            ItemStack newItem = ItemStack.deserializeBytes(storage.getItem(unique_key).bytes);
                            ItemMeta meta = newItem.getItemMeta();

                            pdc = meta.getPersistentDataContainer();
                            NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
                            pdc.remove(namespacedKey);
                            item.setItemMeta(meta);

                            storage.removeItem(unique_key);

                            player.getInventory().setItem(slot, item);
                            player.sendMessage(localizedStrings.youHaveWithdrawnItem);
                            player.openInventory(generateMySoldItemsMenu(player));
                            slot = -1;
                            hasEmptySlot = true;
                        }
                    }

                    if (!hasEmptySlot) {
                        player.sendMessage(localizedStrings.freeUpInventorySpace);
                    }
                }
                break;
            }
            case "CATEGORIES_MENU": {

                PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
                if (pdc.has(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING)) {
                    String menu_item_key = pdc.get(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING);
                    if (menu_item_key.equals("MAIN_MENU")) {
                        player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName), playerCurrentSortingType.get(playerName)));
                        playerCurrentMenu.put(playerName, "MAIN_MENU");
                    } else if (menu_item_key.contains("FILTER:")) {
                        String filterName = menu_item_key.split(":")[1];
                        playerCurrentPage.put(playerName, 0);
                        playerCurrentItemFilter.put(playerName, filterName);
                        playerCurrentMenu.put(playerName, "MAIN_MENU");

                        player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName), playerCurrentSortingType.get(playerName)));
                    }
                }
                break;
            }
            case "CONFIRM_MENU":{
                PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
                if (pdc.has(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING)) {
                    String menu_item_key = pdc.get(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING);
                    if (menu_item_key.equals("CONFIRM_BUYING")){

                        if (getServer().getPluginManager().getPlugin("Vault") == null) {
                            return;
                        }
                        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
                        if (rsp == null) {
                            return;
                        }
                        long balance = (long) rsp.getProvider().getBalance(playerName);

                        ItemStackNotation notation = currentBuyingItem.get(playerName);
                        String unique_key = notation.key;
                        ItemStack newItem = ItemStack.deserializeBytes(notation.bytes);
                        newItem.setAmount(currentBuyingItemAmount.get(playerName));
                        ItemMeta meta = newItem.getItemMeta();
                        pdc = meta.getPersistentDataContainer();
                        NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
                        pdc.remove(namespacedKey);
                        newItem.setItemMeta(meta);

                        float price = (notation.full == 1) ? notation.price : notation.price * currentBuyingItemAmount.get(playerName);

                        if (currentBuyingItemAmount.get(playerName) > storage.getAmount(unique_key)){
                            player.sendMessage(localizedStrings.itemAlreadySold);
                            if (storage.getAmount(unique_key) == 0){
                                playerCurrentMenu.put(playerName, "MAIN_MENU");
                                player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName), playerCurrentSortingType.get(playerName)));
                                return;
                            }
                            playerCurrentMenu.put(playerName, "SELECT_AMOUNT_MENU");
                            currentBuyingItemAmount.put(playerName, storage.getAmount(unique_key));
                            player.openInventory(generateSelectAmountMenu(playerName, currentBuyingItem.get(playerName)));
                            return;
                        }

                        if (balance < price){
                            player.sendMessage(localizedStrings.notEnoughMoney);
                            playerCurrentMenu.put(playerName, "SELECT_AMOUNT_MENU");
                            player.openInventory(generateSelectAmountMenu(playerName, currentBuyingItem.get(playerName)));
                            return;
                        }

                        boolean hasEmptySlot = false;

                        for (int slot = 35; slot >= 0; slot--) {
                            if (player.getInventory().getItem(slot) == null) {
                                player.getInventory().setItem(slot, newItem);
                                slot = -1;
                                hasEmptySlot = true;
                            }
                        }

                        if (!hasEmptySlot) {
                            player.sendMessage(localizedStrings.freeUpInventorySpace);
                            return;
                        }
                        rsp = getServer().getServicesManager().getRegistration(Economy.class);
                        String itemOwner = notation.owner;



                        rsp.getProvider().withdrawPlayer(playerName, price);
                        rsp.getProvider().depositPlayer(itemOwner, price);

                        String message = "";
                        String item_name = newItem.getItemMeta().getDisplayName().isEmpty() ? newItem.getI18NDisplayName() : newItem.getItemMeta().getDisplayName();

                        message = localizedStrings.playerBoughtItemNotification;
                        message = message.replace("{PLAYER}", playerName);
                        message = message.replace("{ITEM}", item_name);
                        message = message.replace("{AMOUNT}", String.valueOf(newItem.getAmount()));
                        message = message.replace("{PRICE}", String.valueOf(price));
                        message = message.replace("{CURRENCY}", localizedStrings.currency);
                        if (getServer().getOnlinePlayers().contains(getServer().getPlayer(itemOwner)))
                            Objects.requireNonNull(getServer().getPlayer(itemOwner)).sendMessage(message);

                        message = localizedStrings.youBoughtItemNotification;
                        message = message.replace("{ITEM}", item_name);
                        message = message.replace("{AMOUNT}", String.valueOf(newItem.getAmount()));
                        message = message.replace("{PRICE}", String.valueOf(price));
                        message = message.replace("{CURRENCY}", localizedStrings.currency);
                        player.sendMessage(message);


                        if (storage.getAmount(unique_key) == currentBuyingItemAmount.get(playerName))
                            storage.removeItem(unique_key);
                        else{
                            ItemStack item_ = ItemStack.deserializeBytes(notation.bytes);
                            item_.setAmount(storage.getAmount(unique_key) - currentBuyingItemAmount.get(playerName));

                            storage.setAmount(unique_key, storage.getAmount(unique_key) - currentBuyingItemAmount.get(playerName), item_.serializeAsBytes());
                        }
                        playerCurrentMenu.put(playerName, "MAIN_MENU");
                        player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName), playerCurrentSortingType.get(playerName)));
                    }
                    else if (menu_item_key.equals("CANCEL_BUYING")){
                        playerCurrentMenu.put(playerName, "MAIN_MENU");
                        player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName), playerCurrentSortingType.get(playerName)));
                    }
                    else if (menu_item_key.equals("MAIN_MENU")){
                        playerCurrentMenu.put(playerName, "MAIN_MENU");
                        player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName), playerCurrentSortingType.get(playerName)));
                    }
                }

                break;
            }
            case "SELECT_AMOUNT_MENU":{
                PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
                if (pdc.has(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING)) {
                    String menu_item_key = pdc.get(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING);
                    assert menu_item_key != null;
                    if (menu_item_key.equals("BUY_BUTTON")){
                        ItemStackNotation notation = currentBuyingItem.get(playerName);
                        notation.amount = currentBuyingItemAmount.get(playerName);
                        playerCurrentMenu.put(playerName, "CONFIRM_MENU");
                        player.openInventory(generateConfirmFullMenu(playerName, notation));
                    }
                    else if (menu_item_key.equals("MAIN_MENU")){
                        playerCurrentMenu.put(playerName, "MAIN_MENU");
                        player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName), playerCurrentSortingType.get(playerName)));
                    }else {
                        String type = menu_item_key.split("_")[1];
                        int delta = Integer.parseInt(menu_item_key.split("_")[2]);

                        if (Objects.equals(type, "MINUS")) delta *= -1;

                        int new_amount = currentBuyingItemAmount.get(playerName) + delta;

                        new_amount = Math.min(new_amount, currentBuyingItem.get(playerName).amount);
                        new_amount = Math.max(1, new_amount);

                        currentBuyingItemAmount.put(playerName, new_amount);

                        player.openInventory(generateSelectAmountMenu(playerName, currentBuyingItem.get(playerName)));
                    }

                }
            }
        }

    }
}
