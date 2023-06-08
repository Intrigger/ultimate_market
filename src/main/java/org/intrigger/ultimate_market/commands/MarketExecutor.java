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
                long price = itemStackNotation.price;
                ArrayList<String> newLore = new ArrayList<>();
                newLore.add(localizedStrings.seller + owner);
                newLore.add(localizedStrings.price + price + localizedStrings.currency);

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
                ItemStackNotation currentItemStackNotation = myItems.get(key);
                ItemStack currentItemStack = (ItemStack.deserializeBytes(currentItemStackNotation.bytes));
                ArrayList<String> newLore = new ArrayList<>();
                long price = currentItemStackNotation.price;

                List<String> currentLore = currentItemStack.getLore();

                newLore.add(localizedStrings.price + price + localizedStrings.currency);
                if (currentLore != null) newLore.addAll(currentLore);
                newLore.addAll(localizedStrings.pressToWithdrawFromSaleLore);

                currentItemStack.setLore(newLore);
                inventory.setItem(key + 9, currentItemStack);
            }
        }

        menus.put("MY_SOLD_ITEMS", inventory);

        return inventory;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        //TODO Т.к. продаваемых предметов у игрока может быть много, надо добавить интерфейс для пролистывания
        //TODO Добавить перевод языка для купленного предмета (ENG --> ANY)

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

            storage.addItem(unique_key, player.getName(), price, System.nanoTime(), itemToSell.getType().toString(), itemToSell);

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
                    long price = notation.price;

                    if (Objects.equals(itemOwner, playerName)) {
                        player.sendMessage(localizedStrings.cannotByYourOwnItem);
                        return;
                    }
                    if (!(balance >= price)) {
                        player.sendMessage(localizedStrings.notEnoughMoney);
                        return;
                    }

                    ItemStack newItem = ItemStack.deserializeBytes(notation.bytes);
                    ItemMeta meta = newItem.getItemMeta();

                    pdc = meta.getPersistentDataContainer();
                    NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
                    pdc.remove(namespacedKey);
                    newItem.setItemMeta(meta);

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

                    storage.removeItem(unique_key);
                    player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName), playerCurrentSortingType.get(playerName)));
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
                        System.out.println("items sold now: " + storage.playerItemsSoldNow(playerName));
                        int pagesNum = getTotalPages(storage.playerItemsSoldNow(playerName)) - 1;
                        int currentPage = playerCurrentPage.get(playerName);
                        playerCurrentPage.put(playerName, Math.max(0, Math.min(currentPage - 1, pagesNum)));
                        player.openInventory(generateMySoldItemsMenu(player));
                    }
                    else if (menu_item_key.equals("PAGE_RIGHT")){
                        System.out.println("items sold now: " + storage.playerItemsSoldNow(playerName));
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
        }

    }
}
