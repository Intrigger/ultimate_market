package org.intrigger.ultimate_market.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.intrigger.ultimate_market.Ultimate_market;
import org.intrigger.ultimate_market.utils.*;
import org.jetbrains.annotations.NotNull;
import net.milkbowl.vault.economy.Economy;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getServer;

public class MarketExecutor implements CommandExecutor  {

    public static Map<String, Inventory> menus;
    public static Map<String, String> playerCurrentMenu;

    private static Plugin plugin;

    public static ItemStorage itemStorage;
    private static BuyRequestStorage buyRequestStorage;

    public Map<String, Integer> playerCurrentMarketPage;
    public Map<String, Integer> playerCurrentBuyRequestsPage;
    public Map<String, Integer> playerCurrentMyBuyRequestsPage;
    private static Map<String, String> playerCurrentItemFilter;
    public static ItemCategoriesProcessor itemCategoriesProcessor;

    public Map<String, Boolean> isMarketMenuOpen;

    public LocalizedStrings localizedStrings;

    private Map<String, String> playerCurrentSortingType;
    private GroupsPermissions groupsPermissions;
    private Map<String, ItemStackNotation> currentBuyingItem;
    private Map<String, Integer> currentBuyingItemAmount;
    public Map<String, Long> last_clicked;

    public String mode;

    public MarketExecutor(Plugin _plugin){
        menus = new HashMap<>();
        playerCurrentMenu = new HashMap<>();
        plugin = _plugin;
        itemStorage = new ItemStorage("plugins/Ultimate Market/");
        buyRequestStorage = new BuyRequestStorage("plugins/Ultimate Market/");
        playerCurrentMarketPage = new HashMap<>();
        playerCurrentBuyRequestsPage = new HashMap<>();
        playerCurrentMyBuyRequestsPage = new HashMap<>();
        itemCategoriesProcessor = new ItemCategoriesProcessor("plugins/Ultimate Market/item_categories.yml");
        playerCurrentItemFilter = new HashMap<>();
        isMarketMenuOpen = new HashMap<>();
        localizedStrings = new LocalizedStrings();
        playerCurrentSortingType = new HashMap<>();
        groupsPermissions = new GroupsPermissions();
        currentBuyingItem = new HashMap<>();
        currentBuyingItemAmount = new HashMap<>();
        last_clicked = new HashMap<>();
        mode = "LEGACY";
    }


    public String getMode(){ return this.mode;}

    public void closeDatabase(){
        itemStorage.closeConnection();
    }

    int getTotalPages(int totalItemsNumber){
        return (int) (1 + Math.floor((double)totalItemsNumber / 45.0));
    }

    /* Deserialize Lore*/
    public List<Component> des_lore(List<String> lore){
        LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacy(LegacyComponentSerializer.SECTION_CHAR);
        List<Component> res = lore.stream().map(LEGACY::deserialize).collect(Collectors.toList());
        ArrayList<Component> result = new ArrayList<>();
        for (Component c: res){
            result.add(c.decoration(TextDecoration.ITALIC, false));
        }
        return result;
    }

    public Inventory generateMainMenu(String playerName, String filter, String sortingType){

        String MODE = mode; //"LEGACY" (4 ms total) OR DEPRECATED (8 ms total)

        if (MODE.equals("LEGACY")){
            LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacy(LegacyComponentSerializer.SECTION_CHAR);

            Map<String, Long> timestamps_start = new HashMap<>();
            Map<String, Long> timestamps_stop = new HashMap<>();

            timestamps_start.put("generateMainMenu()", System.nanoTime());
            timestamps_start.put("setLore()", 0L);
            timestamps_stop.put("setLore()", 0L);

            int inventorySize = 54;

            //Inventory inventory = Bukkit.createInventory(null, inventorySize, Component.text(localizedStrings.mainMenuTitle));
            Inventory inventory = Bukkit.createInventory(null, inventorySize, LEGACY.deserialize(localizedStrings.mainMenuTitle));

        /*
            My Auction Slots Page Button
         */

            ItemStack mySlots = new ItemStack(Material.CHEST);
            ItemMeta mySlotsMeta = mySlots.getItemMeta();
            mySlotsMeta.displayName(LEGACY.deserialize(localizedStrings.myMarketButtonTitle).decoration(TextDecoration.ITALIC, false));
            List<String> lore = localizedStrings.myMarketButtonLore;

            timestamps_start.put("setLore()", timestamps_start.get("setLore()") + System.nanoTime());
            mySlotsMeta.lore(des_lore(lore));
            timestamps_stop.put("setLore()", timestamps_stop.get("setLore()") + System.nanoTime());



            PersistentDataContainer pdc = mySlotsMeta.getPersistentDataContainer();
            NamespacedKey namespacedKey = new NamespacedKey(plugin, "menu_item_key");
            pdc.set(namespacedKey, PersistentDataType.STRING, "MY_SOLD_ITEMS");
            mySlots.setItemMeta(mySlotsMeta);
            inventory.setItem(0, mySlots);

            /*
            *
            *   BuyRequests Button (кнопка поставок)
            *
            */

            ItemStack buyRequests = new ItemStack(Material.CHEST_MINECART);
            ItemMeta buyRequestsMeta = buyRequests.getItemMeta();
            buyRequestsMeta.displayName(LEGACY.deserialize(localizedStrings.buyRequestsButton).decoration(TextDecoration.ITALIC, false));
            lore = localizedStrings.buyRequestsButtonLore;
            buyRequestsMeta.lore(des_lore(lore));
            pdc = buyRequestsMeta.getPersistentDataContainer();
            namespacedKey = new NamespacedKey(plugin, "menu_item_key");
            pdc.set(namespacedKey, PersistentDataType.STRING, "BUY_REQUESTS");
            buyRequests.setItemMeta(buyRequestsMeta);
            inventory.setItem(2, buyRequests);


            //
            // UPDATE PAGE
            //
            ItemStack updatePage = new ItemStack(Material.SLIME_BALL);
            ItemMeta updatePageMeta = updatePage.getItemMeta();
            updatePageMeta.displayName(LEGACY.deserialize(localizedStrings.updatePageButtonTitle).decoration(TextDecoration.ITALIC, false));
            lore = localizedStrings.updatePageButtonLore;
            timestamps_start.put("setLore()", timestamps_start.get("setLore()") + System.nanoTime());
            updatePageMeta.lore(des_lore(lore));
            timestamps_stop.put("setLore()", timestamps_stop.get("setLore()") + System.nanoTime());

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
            leftPageMeta.displayName(LEGACY.deserialize(localizedStrings.previousPageButtonTitle).decoration(TextDecoration.ITALIC, false));
            lore = localizedStrings.previousPageButtonLore;

            timestamps_start.put("setLore()", timestamps_start.get("setLore()") + System.nanoTime());
            leftPageMeta.lore(des_lore(lore));
            timestamps_stop.put("setLore()", timestamps_stop.get("setLore()") + System.nanoTime());

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
            rightPageMeta.displayName(LEGACY.deserialize(localizedStrings.nextPageButtonTitle).decoration(TextDecoration.ITALIC, false));
            lore = localizedStrings.nextPageButtonLore;

            timestamps_start.put("setLore()", timestamps_start.get("setLore()") + System.nanoTime());
            rightPageMeta.lore(des_lore(lore));
            timestamps_stop.put("setLore()", timestamps_stop.get("setLore()") + System.nanoTime());

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
            categoriesPageMeta.displayName(LEGACY.deserialize(localizedStrings.itemCategoriesButtonTitle).decoration(TextDecoration.ITALIC, false));
            lore = localizedStrings.itemCategoriesButtonLore;

            timestamps_start.put("setLore()", timestamps_start.get("setLore()") + System.nanoTime());
            categoriesPageMeta.lore(des_lore(lore));
            timestamps_stop.put("setLore()", timestamps_stop.get("setLore()") + System.nanoTime());

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
            sortingButtonMeta.displayName(LEGACY.deserialize(localizedStrings.sortingTypeButtonTitle).decoration(TextDecoration.ITALIC, false));
            lore = new ArrayList<>(localizedStrings.sortingTypeButtonLore);

            switch (playerCurrentSortingType.get(playerName)){
                case "NEW_FIRST":
                    lore.set(0, "✓ " + localizedStrings.sortingTypeButtonLore.get(0));
                    break;
                case "OLD_FIRST":
                    lore.set(1, "✓ " + localizedStrings.sortingTypeButtonLore.get(1));
                    break;
                case "CHEAP_FIRST":
                    lore.set(2, "✓ " + localizedStrings.sortingTypeButtonLore.get(2));
                    break;
                case "EXPENSIVE_FIRST":
                    lore.set(3, "✓ " + localizedStrings.sortingTypeButtonLore.get(3));
                    break;
            }

            timestamps_start.put("setLore()", timestamps_start.get("setLore()") + System.nanoTime());
            sortingButtonMeta.lore(des_lore(lore));
            timestamps_stop.put("setLore()", timestamps_stop.get("setLore()") + System.nanoTime());

            pdc = sortingButtonMeta.getPersistentDataContainer();
            namespacedKey = new NamespacedKey(plugin, "menu_item_key");
            pdc.set(namespacedKey, PersistentDataType.STRING, "SORTING_BUTTON");
            sortingButton.setItemMeta(sortingButtonMeta);
            inventory.setItem(7, sortingButton);

        /*
            Sorting items from 'items sold file' by time
         */

            ArrayList<ItemStackNotation> queryResult;

            timestamps_start.put("SQL", System.nanoTime());


            if (filter == null) queryResult = itemStorage.getAllKeys(playerCurrentMarketPage.get(playerName), playerCurrentSortingType.get(playerName));
            else queryResult = itemStorage.getAllItemsFiltered(itemCategoriesProcessor.filterNotations.get(filter).filters, playerCurrentMarketPage.get(playerName), sortingType);

            timestamps_stop.put("SQL", System.nanoTime());

            if (queryResult != null){
                int currentSlot = 9;

                int queryResultSize = queryResult.size();

                for (int i = 0; i < queryResultSize; i++) {
                    if (currentSlot > 53) break;
                    ItemStackNotation itemStackNotation = queryResult.get(i);
                    ItemStack currentItemStack = ItemStack.deserializeBytes(itemStackNotation.bytes);

                    String owner = itemStackNotation.owner;
                    double price = itemStackNotation.price;
                    ArrayList<Component> newLore = new ArrayList<>();
                    newLore.add(LEGACY.deserialize(localizedStrings.seller + owner).decoration(TextDecoration.ITALIC, false));

                    if (itemStackNotation.full == 1 || itemStackNotation.amount == 1){
                        newLore.add(LEGACY.deserialize(localizedStrings.buyEntirely + " " + price + localizedStrings.currency + " " + localizedStrings.pressLeftButton).decoration(TextDecoration.ITALIC, false));
                    }
                    else{
                        newLore.add(LEGACY.deserialize(localizedStrings.buyEntirely + " " + String.format("%.0f", Math.ceil((double)price  * (double)itemStackNotation.amount)) + localizedStrings.currency + " " + localizedStrings.pressLeftButton).decoration(TextDecoration.ITALIC, false));
                        newLore.add(LEGACY.deserialize(localizedStrings.buyByPieces + " " + String.format("%.3f",(price)) + localizedStrings.currency + " " + localizedStrings.pressRightButton).decoration(TextDecoration.ITALIC, false));
                    }

                    ItemMeta currentItemMeta = currentItemStack.getItemMeta();
                    List<Component> currentLore = currentItemStack.lore();

                    if (currentLore != null)
                        newLore.addAll(currentLore);


                    timestamps_start.put("setLore()", timestamps_start.get("setLore()") + System.nanoTime());
                    currentItemMeta.lore(newLore); //ВОТ ЭТА СТРОКА МЕДЛЕННАЯ (ОКОЛО 6 MS!) была потому что deprecated
                    timestamps_stop.put("setLore()", timestamps_stop.get("setLore()") + System.nanoTime());

                    currentItemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    currentItemStack.setItemMeta(currentItemMeta);

                    inventory.setItem(currentSlot, currentItemStack);
                    currentSlot++;
                }

            }

            menus.put("MAIN_MENU", inventory);

            timestamps_stop.put("generateMainMenu()", System.nanoTime());


            try{
                FileWriter fileWriter = new FileWriter("UltimateMarketTimings.txt", true);
                PrintWriter printWriter = new PrintWriter(fileWriter);

                for (Map.Entry<String, Long> entry: timestamps_stop.entrySet()){
                    printWriter.println(entry.getKey() + ":\t" + (timestamps_stop.get(entry.getKey()) - timestamps_start.get(entry.getKey())) / 1_000_000.0f);
                }
                printWriter.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return inventory;
        }
        else {
            Map<String, Long> timestamps_start = new HashMap<>();
            Map<String, Long> timestamps_stop = new HashMap<>();

            timestamps_start.put("generateMainMenu()", System.nanoTime());
            timestamps_start.put("setLore()", 0L);
            timestamps_stop.put("setLore()", 0L);


            String mainMenuTitle = localizedStrings.mainMenuTitle;
            int inventorySize = 54;

            Inventory inventory = Bukkit.createInventory(null, inventorySize, mainMenuTitle);
            //Inventory inventory = Bukkit.createInventory(null, 54, LEGACY.deserialize(mainMenuTitle));

        /*
            My Auction Slots Page Button
         */

            ItemStack mySlots = new ItemStack(Material.CHEST);
            ItemMeta mySlotsMeta = mySlots.getItemMeta();
            mySlotsMeta.setDisplayName(localizedStrings.myMarketButtonTitle);
            List<String> lore = localizedStrings.myMarketButtonLore;

            timestamps_start.put("setLore()", timestamps_start.get("setLore()") + System.nanoTime());
            mySlotsMeta.setLore(lore);
            timestamps_stop.put("setLore()", timestamps_stop.get("setLore()") + System.nanoTime());



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
            timestamps_start.put("setLore()", timestamps_start.get("setLore()") + System.nanoTime());
            updatePageMeta.setLore(lore);
            timestamps_stop.put("setLore()", timestamps_stop.get("setLore()") + System.nanoTime());

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

            timestamps_start.put("setLore()", timestamps_start.get("setLore()") + System.nanoTime());
            leftPageMeta.setLore(lore);
            timestamps_stop.put("setLore()", timestamps_stop.get("setLore()") + System.nanoTime());

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

            timestamps_start.put("setLore()", timestamps_start.get("setLore()") + System.nanoTime());
            rightPageMeta.setLore(lore);
            timestamps_stop.put("setLore()", timestamps_stop.get("setLore()") + System.nanoTime());

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

            timestamps_start.put("setLore()", timestamps_start.get("setLore()") + System.nanoTime());
            categoriesPageMeta.setLore(lore);
            timestamps_stop.put("setLore()", timestamps_stop.get("setLore()") + System.nanoTime());

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
            lore = localizedStrings.sortingTypeButtonLore;

            switch (playerCurrentSortingType.get(playerName)){
                case "NEW_FIRST":
                    lore.set(0, "§6✓ "+ localizedStrings.sortingTypeButtonLore.get(0));
                    break;
                case "OLD_FIRST":
                    lore.set(1, "§6✓ "+ localizedStrings.sortingTypeButtonLore.get(1));
                    break;
                case "CHEAP_FIRST":
                    lore.set(2, "§6✓ "+ localizedStrings.sortingTypeButtonLore.get(2));
                    break;
                case "EXPENSIVE_FIRST":
                    lore.set(3, "§6✓ "+ localizedStrings.sortingTypeButtonLore.get(3));
                    break;
            }

            timestamps_start.put("setLore()", timestamps_start.get("setLore()") + System.nanoTime());
            sortingButtonMeta.setLore(lore);
            timestamps_stop.put("setLore()", timestamps_stop.get("setLore()") + System.nanoTime());

            pdc = sortingButtonMeta.getPersistentDataContainer();
            namespacedKey = new NamespacedKey(plugin, "menu_item_key");
            pdc.set(namespacedKey, PersistentDataType.STRING, "SORTING_BUTTON");
            sortingButton.setItemMeta(sortingButtonMeta);
            inventory.setItem(7, sortingButton);

        /*
            Sorting items from 'items sold file' by time
         */

            ArrayList<ItemStackNotation> queryResult;

            timestamps_start.put("SQL", System.nanoTime());


            if (filter == null) queryResult = itemStorage.getAllKeys(playerCurrentMarketPage.get(playerName), playerCurrentSortingType.get(playerName));
            else queryResult = itemStorage.getAllItemsFiltered(itemCategoriesProcessor.filterNotations.get(filter).filters, playerCurrentMarketPage.get(playerName), sortingType);

            timestamps_stop.put("SQL", System.nanoTime());



            if (queryResult != null){
                int currentSlot = 9;

                int queryResultSize = queryResult.size();

                for (int i = 0; i < queryResultSize; i++) {
                    if (currentSlot > 53) break;
                    ItemStackNotation itemStackNotation = queryResult.get(i);
                    ItemStack currentItemStack = ItemStack.deserializeBytes(itemStackNotation.bytes);

                    String owner = itemStackNotation.owner;
                    double price = itemStackNotation.price;
                    ArrayList<String> newLore = new ArrayList<>();
                    newLore.add(localizedStrings.seller + owner);

                    if (itemStackNotation.full == 1 || itemStackNotation.amount == 1){
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

                    timestamps_start.put("setLore()", timestamps_start.get("setLore()") + System.nanoTime());
                    currentItemMeta.setLore(newLore); //ВОТ ЭТА СТРОКА МЕДЛЕННАЯ (ОКОЛО 6 MS!) была потому что deprecated
                    timestamps_stop.put("setLore()", timestamps_stop.get("setLore()") + System.nanoTime());

                    currentItemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    currentItemStack.setItemMeta(currentItemMeta);

                    inventory.setItem(currentSlot, currentItemStack);
                    currentSlot++;
                }

            }

            menus.put("MAIN_MENU", inventory);

            timestamps_stop.put("generateMainMenu()", System.nanoTime());


            try{
                FileWriter fileWriter = new FileWriter("UltimateMarketTimings.txt", true);
                PrintWriter printWriter = new PrintWriter(fileWriter);

                for (Map.Entry<String, Long> entry: timestamps_stop.entrySet()){
                    printWriter.println(entry.getKey() + ":\t" + (timestamps_stop.get(entry.getKey()) - timestamps_start.get(entry.getKey())) / 1_000_000.0f);
                }
                printWriter.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return inventory;
        }

    }

    //TODO Оптимизировать данную функцию по аналогии с генерацией основного меню

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
            newItemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            newItemMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
            newItemMeta.addItemFlags(ItemFlag.HIDE_DYE);
            newItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            newItemMeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
            newItemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            newItemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            newItemMeta.setLore(filterNotation.lore);

            pdc = newItemMeta.getPersistentDataContainer();
            pdc.set(namespacedKey, PersistentDataType.STRING, "FILTER:" + filterNotation.name);

            currentItem.setItemMeta(newItemMeta);
            inventory.setItem(slot, currentItem);
        }

        return inventory;
    }

    //TODO Оптимизировать данную функцию по аналогии с генерацией основного меню
    public Inventory generateMySoldItemsMenu(Player player){
        String inventoryName = localizedStrings.mySoldItemsTitle;
        int inventorySize = 54;
        Inventory inventory = Bukkit.createInventory(null, inventorySize, inventoryName);

        ArrayList<ItemStackNotation> myItems = itemStorage.getPlayerItems(player.getName(), playerCurrentMarketPage.get(player.getName()));

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
                double price = itemStackNotation.price;

                List<String> currentLore = currentItemStack.getLore();

                if (itemStackNotation.full == 1 || itemStackNotation.amount == 1){
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

    //TODO Оптимизировать данную функцию по аналогии с генерацией основного меню
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

    //TODO Оптимизировать данную функцию по аналогии с генерацией основного меню
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

    public Inventory generateMyBuyRequestsMenu(String playerName){
        LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacy(LegacyComponentSerializer.SECTION_CHAR);

        Inventory myBuyRequestsInventory = Bukkit.createInventory(null, 54, LEGACY.deserialize(localizedStrings.myBuyRequestsMenuTitle));
        List<String> lore;
        NamespacedKey namespacedKey = new NamespacedKey(plugin, "menu_item_key");

        ItemStack buyRequests = new ItemStack(Material.CHEST_MINECART);
        ItemMeta buyRequestsMeta = buyRequests.getItemMeta();
        PersistentDataContainer pdc = buyRequestsMeta.getPersistentDataContainer();

        buyRequestsMeta.displayName(LEGACY.deserialize(localizedStrings.buyRequestsButton).decoration(TextDecoration.ITALIC, false));
        lore = localizedStrings.buyRequestsButtonLore;
        buyRequestsMeta.lore(des_lore(lore));

        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "BUY_REQUESTS");
        buyRequests.setItemMeta(buyRequestsMeta);
        myBuyRequestsInventory.setItem(0, buyRequests);

        //
        // UPDATE PAGE
        //
        ItemStack updatePage = new ItemStack(Material.SLIME_BALL);
        ItemMeta updatePageMeta = updatePage.getItemMeta();
        updatePageMeta.displayName(LEGACY.deserialize(localizedStrings.updatePageButtonTitle).decoration(TextDecoration.ITALIC, false));
        lore = localizedStrings.updatePageButtonLore;

        updatePageMeta.lore(des_lore(lore));


        pdc = updatePageMeta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "UPDATE_PAGE");
        updatePage.setItemMeta(updatePageMeta);
        myBuyRequestsInventory.setItem(4, updatePage);

        //
        // LEFT PAGE
        //
        ItemStack leftPage = new ItemStack(Material.PAPER);
        ItemMeta leftPageMeta = leftPage.getItemMeta();
        leftPageMeta.displayName(LEGACY.deserialize(localizedStrings.previousPageButtonTitle).decoration(TextDecoration.ITALIC, false));
        lore = localizedStrings.previousPageButtonLore;


        leftPageMeta.lore(des_lore(lore));


        pdc = leftPageMeta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "PAGE_LEFT");
        leftPage.setItemMeta(leftPageMeta);
        myBuyRequestsInventory.setItem(3, leftPage);

        //
        // RIGHT PAGE
        //
        ItemStack rightPage = new ItemStack(Material.PAPER);
        ItemMeta rightPageMeta = rightPage.getItemMeta();
        rightPageMeta.displayName(LEGACY.deserialize(localizedStrings.nextPageButtonTitle).decoration(TextDecoration.ITALIC, false));
        lore = localizedStrings.nextPageButtonLore;

        rightPageMeta.lore(des_lore(lore));

        pdc = rightPageMeta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "PAGE_RIGHT");
        rightPage.setItemMeta(rightPageMeta);
        myBuyRequestsInventory.setItem(5, rightPage);

        ArrayList<BuyRequestNotation> myBuyRequests = buyRequestStorage.getAllBuyRequests(playerName, playerCurrentMyBuyRequestsPage.get(playerName));

        int currentSlot = 9;

        for (int i = 0; i < myBuyRequests.size(); i++){
            if (currentSlot > 53) break;
            BuyRequestNotation requestNotation = myBuyRequests.get(i);
            ItemStack request = ItemStack.deserializeBytes(requestNotation.bytes);

            ItemMeta request_meta = request.getItemMeta();
            lore = (List<String>) new ArrayList<>(localizedStrings.my_buy_requests_lore).clone();

            for (int temp = 0; temp < lore.size(); temp++){
                String s = lore.get(temp);
                s = s.replace("{AMOUNT_NOW}", String.valueOf(requestNotation.amount_now));
                s = s.replace("{AMOUNT_TOTAL}", String.valueOf(requestNotation.amount_total));
                s = s.replace("{AVAILABLE}", String.valueOf(requestNotation.amount_now - requestNotation.amount_taken));
                s = s.replace("{PRICE}", String.valueOf(requestNotation.price));
                s = s.replace("{CURRENCY}", localizedStrings.currency);
                lore.set(temp, s);
            }

            pdc = request_meta.getPersistentDataContainer();
            namespacedKey = new NamespacedKey(plugin, "unique_key");
            pdc.set(namespacedKey, PersistentDataType.STRING, requestNotation.key);

            request_meta.lore(des_lore(lore));
            request.setItemMeta(request_meta);

            myBuyRequestsInventory.setItem(currentSlot, request);

            currentSlot++;
        }

        return myBuyRequestsInventory;
    }

    public Inventory generateBuyRequestsMainMenu(String playerName){
        LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacy(LegacyComponentSerializer.SECTION_CHAR);

        //TODO Сделать локализацию через yml файл
        Inventory buyRequestsInventory = Bukkit.createInventory(null, 54, LEGACY.deserialize(localizedStrings.buyRequestsMenuTitle));

        /*
         * Кнопка "Мои поставки"
         */

        ItemStack myBuyRequests = new ItemStack(Material.CHEST_MINECART);
        ItemMeta myBuyRequestsMeta = myBuyRequests.getItemMeta();
        myBuyRequestsMeta.displayName(LEGACY.deserialize(localizedStrings.myBuyRequests).decoration(TextDecoration.ITALIC, false));
        List<String> lore = localizedStrings.myBuyRequestsButtonLore;
        myBuyRequestsMeta.lore(des_lore(lore));

        PersistentDataContainer pdc = myBuyRequestsMeta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "MY_BUY_REQUESTS");
        myBuyRequests.setItemMeta(myBuyRequestsMeta);

        buyRequestsInventory.setItem(0, myBuyRequests);

        /*
         * Кнопка "Назад в магазин"
         */

        ItemStack backToMarket = new ItemStack(Material.CHEST);
        ItemMeta backToMarketMeta = backToMarket.getItemMeta();
        backToMarketMeta.displayName(LEGACY.deserialize(localizedStrings.backToMainMenuButtonTitle).decoration(TextDecoration.ITALIC, false));
        lore = localizedStrings.backToMainMenuButtonLore;
        backToMarketMeta.lore(des_lore(lore));

        pdc = backToMarketMeta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "MAIN_MENU");
        backToMarket.setItemMeta(backToMarketMeta);

        buyRequestsInventory.setItem(2, backToMarket);

        //
        // UPDATE PAGE
        //
        ItemStack updatePage = new ItemStack(Material.SLIME_BALL);
        ItemMeta updatePageMeta = updatePage.getItemMeta();
        updatePageMeta.displayName(LEGACY.deserialize(localizedStrings.updatePageButtonTitle).decoration(TextDecoration.ITALIC, false));
        lore = localizedStrings.updatePageButtonLore;

        updatePageMeta.lore(des_lore(lore));


        pdc = updatePageMeta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "UPDATE_PAGE");
        updatePage.setItemMeta(updatePageMeta);
        buyRequestsInventory.setItem(4, updatePage);

        //
        // LEFT PAGE
        //
        ItemStack leftPage = new ItemStack(Material.PAPER);
        ItemMeta leftPageMeta = leftPage.getItemMeta();
        leftPageMeta.displayName(LEGACY.deserialize(localizedStrings.previousPageButtonTitle).decoration(TextDecoration.ITALIC, false));
        lore = localizedStrings.previousPageButtonLore;


        leftPageMeta.lore(des_lore(lore));


        pdc = leftPageMeta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "PAGE_LEFT");
        leftPage.setItemMeta(leftPageMeta);
        buyRequestsInventory.setItem(3, leftPage);

        //
        // RIGHT PAGE
        //
        ItemStack rightPage = new ItemStack(Material.PAPER);
        ItemMeta rightPageMeta = rightPage.getItemMeta();
        rightPageMeta.displayName(LEGACY.deserialize(localizedStrings.nextPageButtonTitle).decoration(TextDecoration.ITALIC, false));
        lore = localizedStrings.nextPageButtonLore;

        rightPageMeta.lore(des_lore(lore));

        pdc = rightPageMeta.getPersistentDataContainer();
        namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "PAGE_RIGHT");
        rightPage.setItemMeta(rightPageMeta);
        buyRequestsInventory.setItem(5, rightPage);

        ArrayList<BuyRequestNotation> buyRequests = buyRequestStorage.getAllBuyRequests(playerCurrentBuyRequestsPage.get(playerName));

        int currentSlot = 9;

        for (int i = 0; i < buyRequests.size(); i++){
            if (currentSlot > 53) break;
            BuyRequestNotation requestNotation = buyRequests.get(i);
            ItemStack request = ItemStack.deserializeBytes(requestNotation.bytes);

            ItemMeta request_meta = request.getItemMeta();
            lore = (List<String>) new ArrayList<>(localizedStrings.buy_requests_lore).clone();

            for (int temp = 0; temp < lore.size(); temp++){
                String s = lore.get(temp);
                s = s.replace("{AMOUNT}", String.valueOf(requestNotation.amount_total - requestNotation.amount_now));
                s = s.replace("{PRICE}", String.valueOf(requestNotation.price));
                s = s.replace("{CURRENCY}", localizedStrings.currency);
                s = s.replace("{PLAYER}", requestNotation.owner);
                lore.set(temp, s);
            }

            pdc = request_meta.getPersistentDataContainer();
            namespacedKey = new NamespacedKey(plugin, "unique_key");
            pdc.set(namespacedKey, PersistentDataType.STRING, requestNotation.key);

            request_meta.lore(des_lore(lore));
            request.setItemMeta(request_meta);

            buyRequestsInventory.setItem(currentSlot, request);

            currentSlot++;
        }

        return buyRequestsInventory;
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

        if (Bukkit.getPluginManager().isPluginEnabled("OmniLegacyEvolution")){
            if (player.hasPermission("OmniLegacyEvo.stage.1") && (!player.isOp())){
                for (String iter: localizedStrings.low_evo_level) player.sendMessage(iter);
                return true;
            }
        }


        if (strings.length == 0){
            playerCurrentMenu.put(playerName, "MAIN_MENU");
            playerCurrentMarketPage.put(playerName, 0);
            playerCurrentBuyRequestsPage.put(playerName, 0);
            playerCurrentMyBuyRequestsPage.put(playerName, 0);
            playerCurrentSortingType.put(playerName, "NEW_FIRST");
            playerCurrentItemFilter.put(playerName, null);
            isMarketMenuOpen.put(playerName, true);
            player.openInventory(generateMainMenu(playerName, null, playerCurrentSortingType.get(playerName)));
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
            if (strings[0].equalsIgnoreCase("sell")){
                ItemStack itemToSell = player.getItemInHand();

                if (itemToSell.getTranslationKey().equals("block.minecraft.air")){
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


                if (itemStorage.playerItemsSoldNow(playerName) >= groupsPermissions.maxItemsToSell.get(playerGroup)){
                    player.sendMessage(localizedStrings.itemSoldLimitReached);
                    return true;
                }

                double price = Math.ceil(priceDouble);

                if (price > Math.pow(10, 12)){
                    for (String temp: localizedStrings.youSpecifiedWrongPrice){
                        player.sendMessage(temp);
                    }
                    return true;
                }

                String unique_key = new Random().ints('a', 'z' + 1).limit(64).collect(StringBuilder::new,
                        StringBuilder::appendCodePoint, StringBuilder::append).toString();

                ItemMeta meta = itemToSell.getItemMeta();

                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
                pdc.set(namespacedKey, PersistentDataType.STRING, unique_key);

                itemToSell.setItemMeta(meta);

                int sell_full = 0;

                itemStorage.addItem(unique_key, player.getName(), (price / itemToSell.getAmount()), System.nanoTime(), itemToSell.getType().toString(), itemToSell, itemToSell.getAmount(), sell_full);

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
            else if (strings[0].equalsIgnoreCase("buy")){
                //TODO Сделать покупку по средней цене на данный товар
                for (String iter : localizedStrings.specifyThePrice) player.sendMessage(iter);
            }
        }
        else if (strings.length == 3){
            if (strings[0].equalsIgnoreCase("sell")){
                String arg3 = strings[2];
                if (!MarketTabComplete.list.get(2).contains(arg3)){
                    for (String temp: localizedStrings.wrongCommandUsage){
                        player.sendMessage(temp);
                    }
                    return true;
                }

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

                ItemStack itemToSell = player.getItemInHand();

                if (player.getItemInHand().getTranslationKey().equals("block.minecraft.air")){
                    for (String temp: localizedStrings.theItemToBeSoldMustBeHeldInTheHand){
                        player.sendMessage(temp);
                    }
                    return true;
                }

                priceDouble = Math.round(priceDouble);

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


                if (itemStorage.playerItemsSoldNow(playerName) >= groupsPermissions.maxItemsToSell.get(playerGroup)){
                    player.sendMessage(localizedStrings.itemSoldLimitReached);
                    return true;
                }

                double price = Math.ceil(priceDouble);

                if (price > Math.pow(10, 12)){
                    for (String temp: localizedStrings.youSpecifiedWrongPrice){
                        player.sendMessage(temp);
                    }
                    return true;
                }

                String unique_key = new Random().ints('a', 'z' + 1).limit(64).collect(StringBuilder::new,
                        StringBuilder::appendCodePoint, StringBuilder::append).toString();

                ItemMeta meta = itemToSell.getItemMeta();

                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
                pdc.set(namespacedKey, PersistentDataType.STRING, unique_key);

                itemToSell.setItemMeta(meta);

                int sell_full = arg3.equalsIgnoreCase("full") ? 1 : 0;

                itemStorage.addItem(unique_key, player.getName(), sell_full == 1 ? price : price / itemToSell.getAmount(), System.nanoTime(), itemToSell.getType().toString(), itemToSell, itemToSell.getAmount(), sell_full);

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
            else if (strings[0].equalsIgnoreCase("buy")){


                ItemStack itemToSell = player.getItemInHand().clone();

                if (itemToSell.getTranslationKey().equals("block.minecraft.air")){
                    for (String x: localizedStrings.theItemToBeBoughtMustBeHeldInTheHand) player.sendMessage(x);
                    return true;
                }
                String amount_str = strings[1];
                String price_str = strings[2];
                
                if (!NumberUtils.isNumber(amount_str)){
                    for (String x: localizedStrings.incorrectPrice) player.sendMessage(x);
                    return true;
                }
                else if (Integer.parseInt(amount_str) < 0){
                    for (String x: localizedStrings.negativePrice) player.sendMessage(x);
                    return true;
                }

                String priceStrCopy = price_str;
                double priceDouble;

                for (String el : Arrays.asList("k", "K", "kk", "KK", "m", "M", "kkk", "b", "KKK", "B")){
                    priceStrCopy = priceStrCopy.replaceAll(el, "");
                }

                priceDouble = Double.parseDouble(priceStrCopy);

                if (price_str.endsWith("kkk") || (price_str.endsWith("b")) || price_str.endsWith("KKK") || (price_str.endsWith("B"))){
                    priceDouble *= 1000000000;
                }
                else if (price_str.endsWith("kk") || (price_str.endsWith("m")) || (price_str.endsWith("KK")) || (price_str.endsWith("M"))){
                    priceDouble *= 1000000;
                }
                else if (price_str.endsWith("k") || (price_str.endsWith("K"))){
                    priceDouble *= 1000;
                }
                else{
                    if (!NumberUtils.isNumber(price_str)){
                        for (String temp: localizedStrings.youSpecifiedWrongPrice){
                            player.sendMessage(temp);
                        }
                        return true;
                    }
                }

                if (priceDouble < 0){
                    for (String temp: localizedStrings.youSpecifiedWrongPrice){
                        player.sendMessage(temp);
                    }
                    return true;
                }

                //TODO Ограничить максимальное возможное значение для amount_str
                //System.out.println("Игрок " + playerName + " хочет купить " + itemToSell.getTranslationKey() + " в кол-во " + amount_str + " за " + priceDouble);

                String unique_key = new Random().ints('a', 'z' + 1).limit(64).collect(StringBuilder::new,
                        StringBuilder::appendCodePoint, StringBuilder::append).toString();


//                PersistentDataContainer pdc = meta.getPersistentDataContainer();
//                NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
//                pdc.set(namespacedKey, PersistentDataType.STRING, unique_key);

                itemToSell.setAmount(1);

                BuyRequestNotation buyRequestNotation = new BuyRequestNotation(unique_key, System.nanoTime(), playerName, priceDouble, itemToSell.getType().toString(), itemToSell.serializeAsBytes(), 0, 0, Integer.parseInt(amount_str));

                if (getServer().getPluginManager().getPlugin("Vault") == null) {
                    return true;
                }
                RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
                if (rsp == null) {
                    return true;
                }

                rsp = getServer().getServicesManager().getRegistration(Economy.class);

                if (rsp == null) {
                    return true;
                }

                long balance = (long) rsp.getProvider().getBalance(playerName);

                if (balance < buyRequestNotation.price * buyRequestNotation.amount_total){
                    player.sendMessage(localizedStrings.notEnoughMoney);
                    return true;
                }

                rsp.getProvider().withdrawPlayer(playerName, buyRequestNotation.price * buyRequestNotation.amount_total);

                buyRequestStorage.addRequest(buyRequestNotation);

                player.sendMessage(localizedStrings.buy_request_created + (buyRequestNotation.price * buyRequestNotation.amount_total) + localizedStrings.currency);

            }
            else {
                //TODO Сделать ошибку ввода команды
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

                    int pagesNum = getTotalPages(itemStorage.getTotalItems(filters)) - 1;

                    int currentPage = playerCurrentMarketPage.get(playerName);

                    switch (menu_item_key) {
                        case "MY_SOLD_ITEMS":  // my sold items
                            playerCurrentMenu.put(playerName, "MY_SOLD_ITEMS");
                            playerCurrentMarketPage.put(playerName, 0);
                            player.openInventory(generateMySoldItemsMenu(player));
                            break;
                        case "UPDATE_PAGE":
                            playerCurrentMarketPage.put(playerName, Math.max(0, Math.min(currentPage, pagesNum)));
                            player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName), playerCurrentSortingType.get(playerName)));
                            break;
                        case "PAGE_LEFT":
                            playerCurrentMarketPage.put(playerName, Math.max(0, Math.min(currentPage - 1, pagesNum)));
                            player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName), playerCurrentSortingType.get(playerName)));
                            break;
                        case "PAGE_RIGHT":
                            playerCurrentMarketPage.put(playerName, Math.max(0, Math.min(currentPage + 1, pagesNum)));
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
                        case "BUY_REQUESTS":
                            playerCurrentMenu.put(playerName, "BUY_REQUESTS");
                            player.openInventory(generateBuyRequestsMainMenu(playerName));
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

                    if (itemStorage.getItem(unique_key) == null) {
                        player.sendMessage(localizedStrings.itemAlreadySold);
                        player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName), playerCurrentSortingType.get(playerName)));
                        return;
                    }

                    ItemStackNotation notation = itemStorage.getItem(unique_key);
                    String itemOwner = notation.owner;
                    double price = notation.price;

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
                        int pagesNum = getTotalPages(itemStorage.playerItemsSoldNow(playerName)) - 1;
                        int currentPage = playerCurrentMarketPage.get(playerName);
                        playerCurrentMarketPage.put(playerName, Math.max(0, Math.min(currentPage - 1, pagesNum)));
                        player.openInventory(generateMySoldItemsMenu(player));
                    }
                    else if (menu_item_key.equals("PAGE_RIGHT")){
                        int pagesNum = getTotalPages(itemStorage.playerItemsSoldNow(playerName)) - 1;
                        int currentPage = playerCurrentMarketPage.get(playerName);
                        playerCurrentMarketPage.put(playerName, Math.max(0, Math.min(currentPage + 1, pagesNum)));
                        player.openInventory(generateMySoldItemsMenu(player));
                    }
                } else {
                    boolean hasEmptySlot = false;

                    for (int slot = 35; slot >= 0; slot--) {
                        if (player.getInventory().getItem(slot) == null) {

                            String unique_key = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "unique_key"), PersistentDataType.STRING);

                            if (itemStorage.getItem(unique_key) == null) {
                                player.sendMessage(localizedStrings.itemAlreadySold);
                                player.openInventory(generateMySoldItemsMenu(player));
                                return;
                            }

                            ItemStack newItem = ItemStack.deserializeBytes(itemStorage.getItem(unique_key).bytes);
                            ItemMeta meta = newItem.getItemMeta();

                            pdc = meta.getPersistentDataContainer();
                            NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
                            pdc.remove(namespacedKey);
                            item.setItemMeta(meta);

                            itemStorage.removeItem(unique_key);

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
                        playerCurrentMarketPage.put(playerName, 0);
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

                        double price = (notation.full == 1) ? notation.price : notation.price * currentBuyingItemAmount.get(playerName);

                        if (currentBuyingItemAmount.get(playerName) > itemStorage.getAmount(unique_key)){
                            player.sendMessage(localizedStrings.itemAlreadySold);
                            if (itemStorage.getAmount(unique_key) == 0){
                                playerCurrentMenu.put(playerName, "MAIN_MENU");
                                player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName), playerCurrentSortingType.get(playerName)));
                                return;
                            }
                            playerCurrentMenu.put(playerName, "SELECT_AMOUNT_MENU");
                            currentBuyingItemAmount.put(playerName, itemStorage.getAmount(unique_key));
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


                        if (itemStorage.getAmount(unique_key) == currentBuyingItemAmount.get(playerName))
                            itemStorage.removeItem(unique_key);
                        else{
                            ItemStack item_ = ItemStack.deserializeBytes(notation.bytes);
                            item_.setAmount(itemStorage.getAmount(unique_key) - currentBuyingItemAmount.get(playerName));

                            itemStorage.setAmount(unique_key, itemStorage.getAmount(unique_key) - currentBuyingItemAmount.get(playerName), item_.serializeAsBytes());
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
            case "BUY_REQUESTS": {
                LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacy(LegacyComponentSerializer.SECTION_CHAR);
                PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
                if (pdc.has(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING)) {
                    String menu_item_key = pdc.get(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING);
                    assert menu_item_key != null;

                    int currentPage = playerCurrentBuyRequestsPage.get(playerName);
                    int pagesNum = buyRequestStorage.getPagesNum() - 1;
                    if (menu_item_key.equalsIgnoreCase("MAIN_MENU")) {
                        player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName), playerCurrentSortingType.get(playerName)));
                        playerCurrentMenu.put(playerName, "MAIN_MENU");
                    }
                    else if (menu_item_key.equalsIgnoreCase("MY_BUY_REQUESTS")) {
                        player.openInventory(generateMyBuyRequestsMenu(playerName));
                        playerCurrentMenu.put(playerName, "MY_BUY_REQUESTS");
                    }
                    else if (menu_item_key.equalsIgnoreCase("UPDATE_PAGE")) {
                        playerCurrentBuyRequestsPage.put(playerName, Math.max(0, Math.min(currentPage, pagesNum)));
                        player.openInventory(generateBuyRequestsMainMenu(playerName));
                    }
                    else if (menu_item_key.equalsIgnoreCase("PAGE_LEFT")) {
                        playerCurrentBuyRequestsPage.put(playerName, Math.max(0, Math.min(currentPage - 1, pagesNum)));
                        player.openInventory(generateBuyRequestsMainMenu(playerName));
                    }
                    else if (menu_item_key.equalsIgnoreCase("PAGE_RIGHT")) {
                        playerCurrentBuyRequestsPage.put(playerName, Math.max(0, Math.min(currentPage + 1, pagesNum)));
                        player.openInventory(generateBuyRequestsMainMenu(playerName));
                    }
                }
                else {
                    String unique_key = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "unique_key"), PersistentDataType.STRING);
                    BuyRequestNotation buyRequestNotation = buyRequestStorage.getBuyRequest(unique_key);

                    if (buyRequestNotation == null){
                        player.sendMessage(localizedStrings.buy_request_is_closed);
                        player.openInventory(generateBuyRequestsMainMenu(playerName));
                        return;
                    }

                    if (buyRequestNotation.owner.equals(playerName)){
                        player.sendMessage(localizedStrings.cannot_sell_to_yourself);
                        player.openInventory(generateBuyRequestsMainMenu(playerName));
                        return;
                    }

                    if (clickEvent.isLeftClick() && (!clickEvent.isShiftClick())) {
                        if (buyRequestNotation.amount_now + 1 <= buyRequestNotation.amount_total) {
                            Inventory playerInv = player.getInventory();

                            int player_has_items = 0;
                            for (ItemStack playerItem : playerInv.getContents()) {
                                if (playerItem != null) {

                                    ItemStack playerItemCopy = playerItem.clone();
                                    ItemMeta playerItemCopyMeta = playerItemCopy.getItemMeta();
                                    playerItemCopyMeta.displayName(LEGACY.deserialize(""));
                                    playerItemCopy.setItemMeta(playerItemCopyMeta);
                                    playerItemCopy.setAmount(1);

                                    ItemStack shopItemCopy = ItemStack.deserializeBytes(buyRequestNotation.bytes);
                                    ItemMeta shopItemCopyMeta = shopItemCopy.getItemMeta();
                                    shopItemCopyMeta.displayName(LEGACY.deserialize(""));
                                    shopItemCopy.setItemMeta(shopItemCopyMeta);
                                    shopItemCopy.setAmount(1);

                                    if (playerItemCopy.equals(shopItemCopy)) {
                                        player_has_items += playerItem.getAmount();
                                    }
                                }
                            }

                            if (player_has_items == 0){
                                player.sendMessage(localizedStrings.you_dont_have_this_item);
                                return;
                            }

                            for (ItemStack playerItem : playerInv.getContents()) {
                                if (playerItem != null) {

                                    ItemStack playerItemCopy = playerItem.clone();
                                    ItemMeta playerItemCopyMeta = playerItemCopy.getItemMeta();
                                    playerItemCopyMeta.displayName(LEGACY.deserialize(""));
                                    playerItemCopy.setItemMeta(playerItemCopyMeta);
                                    playerItemCopy.setAmount(1);

                                    ItemStack shopItemCopy = ItemStack.deserializeBytes(buyRequestNotation.bytes);
                                    ItemMeta shopItemCopyMeta = shopItemCopy.getItemMeta();
                                    shopItemCopyMeta.displayName(LEGACY.deserialize(""));
                                    shopItemCopy.setItemMeta(shopItemCopyMeta);
                                    shopItemCopy.setAmount(1);

                                    if (playerItemCopy.equals(shopItemCopy)) {
                                        playerItem.setAmount(playerItem.getAmount() - 1);
                                        String msg = localizedStrings.you_sold_item_notification;
                                        msg = msg.replace("{ITEM}", buyRequestNotation.material);
                                        msg = msg.replace("{PLAYER}", buyRequestNotation.owner);
                                        msg = msg.replace("{AMOUNT}", "1");
                                        msg = msg.replace("{PRICE}", String.valueOf(buyRequestNotation.price));
                                        msg = msg.replace("{CURRENCY}", localizedStrings.currency);
                                        player.sendMessage(msg);


                                        if (Bukkit.getPlayer(buyRequestNotation.owner).isOnline()){
                                            msg = localizedStrings.buy_request_received;
                                            msg = msg.replace("{ITEM}", buyRequestNotation.material);
                                            msg = msg.replace("{PLAYER}", playerName);
                                            msg = msg.replace("{AMOUNT}", String.valueOf(1));
                                            Objects.requireNonNull(Bukkit.getPlayer(buyRequestNotation.owner)).sendMessage(msg);
                                        }

                                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                                        if (getServer().getPluginManager().getPlugin("Vault") == null) {
                                            return;
                                        }
                                        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
                                        if (rsp == null) {
                                            return;
                                        }

                                        rsp = getServer().getServicesManager().getRegistration(Economy.class);

                                        rsp.getProvider().depositPlayer(playerName, buyRequestNotation.price);

                                        buyRequestStorage.updateBuyRequest(unique_key, 1);

                                        if (Bukkit.getPlayer(buyRequestNotation.owner).isOnline())
                                            Bukkit.getPlayer(buyRequestNotation.owner).sendMessage("Поставка получена: " + buyRequestNotation.material + " x1");

                                        break;
                                    }
                                    else {
                                        //
                                    }
                                }
                            }
                            player.updateInventory();
                            player.openInventory(generateBuyRequestsMainMenu(playerName));
                        }
                        else {
                            player.openInventory(generateBuyRequestsMainMenu(playerName));
                            player.sendMessage(localizedStrings.buy_request_is_closed);
                        }
                    }
                    else if (clickEvent.isRightClick() && (!clickEvent.isShiftClick())){
                        int stack_size = new ItemStack(Objects.requireNonNull(Material.getMaterial(buyRequestNotation.material))).getMaxStackSize();
                        if (buyRequestNotation.amount_now + stack_size <= buyRequestNotation.amount_total){
                            Inventory playerInv = player.getInventory();
                            int valid_items_counter = 0;

                            for (ItemStack playerItem : playerInv.getContents()) {
                                if (playerItem != null) {

                                    ItemStack playerItemCopy = playerItem.clone();
                                    ItemMeta playerItemCopyMeta = playerItemCopy.getItemMeta();
                                    playerItemCopyMeta.displayName(LEGACY.deserialize(""));
                                    playerItemCopy.setItemMeta(playerItemCopyMeta);
                                    playerItemCopy.setAmount(1);

                                    ItemStack shopItemCopy = ItemStack.deserializeBytes(buyRequestNotation.bytes);
                                    ItemMeta shopItemCopyMeta = shopItemCopy.getItemMeta();
                                    shopItemCopyMeta.displayName(LEGACY.deserialize(""));
                                    shopItemCopy.setItemMeta(shopItemCopyMeta);
                                    shopItemCopy.setAmount(1);

                                    if (playerItemCopy.equals(shopItemCopy)) {
                                        valid_items_counter += playerItem.getAmount();
                                    }
                                }
                            }

                            if (valid_items_counter < stack_size){
                                player.sendMessage(localizedStrings.not_enough_items);
                                return;
                            }

                            valid_items_counter = stack_size;

                            for (ItemStack playerItem : playerInv.getContents()) {
                                if (playerItem != null) {

                                    ItemStack playerItemCopy = playerItem.clone();
                                    ItemMeta playerItemCopyMeta = playerItemCopy.getItemMeta();
                                    playerItemCopyMeta.displayName(LEGACY.deserialize(""));
                                    playerItemCopy.setItemMeta(playerItemCopyMeta);
                                    playerItemCopy.setAmount(1);

                                    ItemStack shopItemCopy = ItemStack.deserializeBytes(buyRequestNotation.bytes);
                                    ItemMeta shopItemCopyMeta = shopItemCopy.getItemMeta();
                                    shopItemCopyMeta.displayName(LEGACY.deserialize(""));
                                    shopItemCopy.setItemMeta(shopItemCopyMeta);
                                    shopItemCopy.setAmount(1);

                                    if (playerItemCopy.equals(shopItemCopy)) {
                                        int d = Math.min(valid_items_counter, playerItem.getAmount());
                                        buyRequestStorage.updateBuyRequest(unique_key, d);
                                        playerItem.setAmount(playerItem.getAmount() - d);
                                        valid_items_counter -= d;
                                        if (valid_items_counter == 0) break;
                                    }
                                }
                            }

                            String msg = localizedStrings.you_sold_item_notification;
                            msg = msg.replace("{ITEM}", buyRequestNotation.material);
                            msg = msg.replace("{PLAYER}", buyRequestNotation.owner);
                            msg = msg.replace("{AMOUNT}", String.valueOf(stack_size));
                            msg = msg.replace("{PRICE}", String.valueOf(buyRequestNotation.price * stack_size));
                            msg = msg.replace("{CURRENCY}", localizedStrings.currency);
                            player.sendMessage(msg);

                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                            if (getServer().getPluginManager().getPlugin("Vault") == null) {
                                return;
                            }
                            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
                            if (rsp == null) {
                                return;
                            }

                            rsp = getServer().getServicesManager().getRegistration(Economy.class);

                            rsp.getProvider().depositPlayer(playerName, buyRequestNotation.price * stack_size);

                            if (Bukkit.getPlayer(buyRequestNotation.owner).isOnline()){
                                msg = localizedStrings.buy_request_received;
                                msg = msg.replace("{ITEM}", buyRequestNotation.material);
                                msg = msg.replace("{PLAYER}", playerName);
                                msg = msg.replace("{AMOUNT}", String.valueOf(stack_size));
                                Objects.requireNonNull(Bukkit.getPlayer(buyRequestNotation.owner)).sendMessage(msg);
                            }
                        }
                        else{
                            player.sendMessage(localizedStrings.cannot_sell_one_stack);
                        }
                        player.updateInventory();
                        player.openInventory(generateBuyRequestsMainMenu(playerName));
                    }
                    else if (clickEvent.isRightClick()){ //Shift + ПКМ
                        if (buyRequestNotation.amount_now < buyRequestNotation.amount_total){
                            int player_has_items = 0;
                            Inventory playerInv = player.getInventory();
                            for (ItemStack playerItem : playerInv.getContents()) {
                                if (playerItem != null) {

                                    ItemStack playerItemCopy = playerItem.clone();
                                    ItemMeta playerItemCopyMeta = playerItemCopy.getItemMeta();
                                    playerItemCopyMeta.displayName(LEGACY.deserialize(""));
                                    playerItemCopy.setItemMeta(playerItemCopyMeta);
                                    playerItemCopy.setAmount(1);

                                    ItemStack shopItemCopy = ItemStack.deserializeBytes(buyRequestNotation.bytes);
                                    ItemMeta shopItemCopyMeta = shopItemCopy.getItemMeta();
                                    shopItemCopyMeta.displayName(LEGACY.deserialize(""));
                                    shopItemCopy.setItemMeta(shopItemCopyMeta);
                                    shopItemCopy.setAmount(1);

                                    if (playerItemCopy.equals(shopItemCopy)) {
                                        player_has_items += playerItem.getAmount();
                                    }
                                }
                            }

                            if (player_has_items == 0){
                                player.sendMessage(localizedStrings.you_dont_have_this_item);
                                return;
                            }

                            int can_be_sold = Math.min(buyRequestNotation.amount_total - buyRequestNotation.amount_now, player_has_items);

                            String msg = localizedStrings.you_sold_item_notification;
                            msg = msg.replace("{ITEM}", buyRequestNotation.material);
                            msg = msg.replace("{PLAYER}", buyRequestNotation.owner);
                            msg = msg.replace("{AMOUNT}", String.valueOf(can_be_sold));
                            msg = msg.replace("{PRICE}", String.valueOf(buyRequestNotation.price * can_be_sold));
                            msg = msg.replace("{CURRENCY}", localizedStrings.currency);
                            player.sendMessage(msg);

                            if (Bukkit.getPlayer(buyRequestNotation.owner).isOnline()){
                                msg = localizedStrings.buy_request_received;
                                msg = msg.replace("{ITEM}", buyRequestNotation.material);
                                msg = msg.replace("{PLAYER}", playerName);
                                msg = msg.replace("{AMOUNT}", String.valueOf(can_be_sold));
                                Objects.requireNonNull(Bukkit.getPlayer(buyRequestNotation.owner)).sendMessage(msg);
                            }

                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                            if (getServer().getPluginManager().getPlugin("Vault") == null) {
                                return;
                            }
                            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
                            if (rsp == null) {
                                return;
                            }

                            rsp = getServer().getServicesManager().getRegistration(Economy.class);

                            rsp.getProvider().depositPlayer(playerName, buyRequestNotation.price * can_be_sold);

                            for (ItemStack playerItem : playerInv.getContents()) {
                                if (playerItem != null) {

                                    ItemStack playerItemCopy = playerItem.clone();
                                    ItemMeta playerItemCopyMeta = playerItemCopy.getItemMeta();
                                    playerItemCopyMeta.displayName(LEGACY.deserialize(""));
                                    playerItemCopy.setItemMeta(playerItemCopyMeta);
                                    playerItemCopy.setAmount(1);

                                    ItemStack shopItemCopy = ItemStack.deserializeBytes(buyRequestNotation.bytes);
                                    ItemMeta shopItemCopyMeta = shopItemCopy.getItemMeta();
                                    shopItemCopyMeta.displayName(LEGACY.deserialize(""));
                                    shopItemCopy.setItemMeta(shopItemCopyMeta);
                                    shopItemCopy.setAmount(1);

                                    if (playerItemCopy.equals(shopItemCopy)) {
                                        int d = Math.min(playerItem.getAmount(), can_be_sold);
                                        can_be_sold -= d;
                                        buyRequestStorage.updateBuyRequest(unique_key, d);
                                        playerItem.setAmount(playerItem.getAmount() - d);
                                        if (can_be_sold == 0) break;
                                    }
                                }
                            }
                            player.updateInventory();
                        }
                        player.openInventory(generateBuyRequestsMainMenu(playerName));
                    }
                }
                break;
            }
            case "MY_BUY_REQUESTS":{
                LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacy(LegacyComponentSerializer.SECTION_CHAR);
                PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
                if (pdc.has(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING)) {
                    String menu_item_key = pdc.get(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING);
                    assert menu_item_key != null;
                    int currentPage = playerCurrentMyBuyRequestsPage.get(playerName);
                    int pagesNum = buyRequestStorage.getPagesNum(playerName) - 1;

                    if (menu_item_key.equalsIgnoreCase("BUY_REQUESTS")) {
                        playerCurrentMenu.put(playerName, "BUY_REQUESTS");
                        player.openInventory(generateBuyRequestsMainMenu(playerName));
                    }
                    else if (menu_item_key.equalsIgnoreCase("UPDATE_PAGE")) {
                        playerCurrentMyBuyRequestsPage.put(playerName, Math.max(0, Math.min(currentPage, pagesNum)));
                        player.openInventory(generateMyBuyRequestsMenu(playerName));
                    }
                    else if (menu_item_key.equalsIgnoreCase("PAGE_LEFT")) {
                        playerCurrentMyBuyRequestsPage.put(playerName, Math.max(0, Math.min(currentPage - 1, pagesNum)));
                        player.openInventory(generateMyBuyRequestsMenu(playerName));
                    }
                    else if (menu_item_key.equalsIgnoreCase("PAGE_RIGHT")) {
                        playerCurrentMyBuyRequestsPage.put(playerName, Math.max(0, Math.min(currentPage + 1, pagesNum)));
                        player.openInventory(generateMyBuyRequestsMenu(playerName));
                    }
                }
                else{
                    if (pdc.has(new NamespacedKey(plugin, "unique_key"), PersistentDataType.STRING)){
                        String unique_key = pdc.get(new NamespacedKey(plugin, "unique_key"), PersistentDataType.STRING);
                        assert unique_key != null;

                        BuyRequestNotation myRequest = buyRequestStorage.getBuyRequest(unique_key);

                        if (myRequest == null) return;

                        // Если это чисто ЛКМ
                        if (clickEvent.isLeftClick() && (!clickEvent.isShiftClick())) {
                            boolean hasEmptySlot = false;
                            if (myRequest.amount_taken + 1 > myRequest.amount_now){
                                player.sendMessage(localizedStrings.no_items_to_withdraw);
                                return;
                            }
                            ItemStack newItem = ItemStack.deserializeBytes(myRequest.bytes);
                            ItemMeta meta = newItem.getItemMeta();

                            pdc = meta.getPersistentDataContainer();
                            NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
                            pdc.remove(namespacedKey);
                            newItem.setItemMeta(meta);

                            for (int slot = 35; slot >= 0; slot--) {
                                if (player.getInventory().getItem(slot) == null) {
                                    buyRequestStorage.updateAmountTaken(unique_key, 1);

                                    player.getInventory().setItem(slot, newItem);
                                    player.sendMessage(localizedStrings.you_took_one_item_from_buy_requests);
                                    player.openInventory(generateMyBuyRequestsMenu(playerName));
                                    slot = -1;
                                    hasEmptySlot = true;
                                }
                            }

                            if (!hasEmptySlot) {
                                player.sendMessage(localizedStrings.freeUpInventorySpace);
                            }
                        }
                        else if (clickEvent.isLeftClick()) { //shift + ЛКМ = снять все что есть
                            int empty_slots = 0;
                            for (int slot = 35; slot >= 0; slot--) {
                                if (player.getInventory().getItem(slot) == null) {
                                    empty_slots++;
                                }
                            }
                            if (empty_slots == 0){
                                player.sendMessage(localizedStrings.freeUpInventorySpace);
                                return;
                            }

                            ItemStack newItem = ItemStack.deserializeBytes(myRequest.bytes);
                            ItemMeta meta = newItem.getItemMeta();
                            pdc = meta.getPersistentDataContainer();
                            NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
                            pdc.remove(namespacedKey);
                            newItem.setItemMeta(meta);

                            int max_stack_size = newItem.getMaxStackSize();
                            int max_can_be_taken = max_stack_size * empty_slots;
                            int available = myRequest.amount_now - myRequest.amount_taken;

                            if (available == 0){
                                player.sendMessage(localizedStrings.no_items_to_withdraw);
                                return;
                            }

                            int to_be_taken = Math.min(available, max_can_be_taken);

                            for (int slot = 35; slot >= 0; slot--) {
                                if (player.getInventory().getItem(slot) == null) {
                                    newItem.setAmount(Math.min(max_stack_size, to_be_taken));

                                    buyRequestStorage.updateAmountTaken(unique_key, Math.min(max_stack_size, to_be_taken));

                                    player.getInventory().setItem(slot, newItem);
                                    to_be_taken -= Math.min(max_stack_size, to_be_taken);

                                    if (to_be_taken == 0) slot = -1;
                                }
                            }
                            player.openInventory(generateMyBuyRequestsMenu(playerName));
                        }
                        else if (clickEvent.isRightClick() && clickEvent.isShiftClick()){
                            int available = myRequest.amount_now - myRequest.amount_taken;

                            ItemStack newItem = ItemStack.deserializeBytes(myRequest.bytes);
                            ItemMeta meta = newItem.getItemMeta();
                            pdc = meta.getPersistentDataContainer();
                            NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
                            pdc.remove(namespacedKey);
                            newItem.setItemMeta(meta);

                            int max_stack_size = newItem.getMaxStackSize();

                            while (available != 0){
                                ItemStack itemCopy = newItem.clone();
                                itemCopy.setAmount(Math.min(available, max_stack_size));

                                available -= Math.min(available, max_stack_size);
                                player.getWorld().dropItem(player.getLocation(), itemCopy);
                            }

                            if (getServer().getPluginManager().getPlugin("Vault") == null) {
                                return;
                            }
                            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
                            if (rsp == null) {
                                return;
                            }

                            rsp = getServer().getServicesManager().getRegistration(Economy.class);

                            if (rsp == null) {
                                return;
                            }

                            rsp.getProvider().depositPlayer(playerName, (myRequest.amount_total - available) * myRequest.price);

                            player.sendMessage(localizedStrings.returned_money + (myRequest.amount_total - myRequest.amount_now) * myRequest.price + localizedStrings.currency);

                            buyRequestStorage.finishBuyRequest(unique_key);
                            player.openInventory(generateMyBuyRequestsMenu(playerName));
                        }
                    }
                }
                break;
            }
        }
    }
}
