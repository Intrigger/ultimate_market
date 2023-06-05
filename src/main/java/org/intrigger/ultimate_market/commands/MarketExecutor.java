package org.intrigger.ultimate_market.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.intrigger.ultimate_market.Ultimate_market;
import org.intrigger.ultimate_market.utils.*;
import org.jetbrains.annotations.NotNull;

import net.milkbowl.vault.economy.Economy;


import java.io.*;
import java.util.*;

import static org.bukkit.Bukkit.getServer;
import static org.intrigger.ultimate_market.Ultimate_market.LOGGER;

public class MarketExecutor implements CommandExecutor  {

    public static Map<String, Inventory> menus;
    public static Map<String, String> playerCurrentMenu;

    private static Plugin plugin;

    private static File file;
    private static FileConfiguration configuration;

    private static ItemStorage storage;

    private static Map<String, Integer> playerCurrentPage;
    private static Map<String, String> playerCurrentItemFilter;
    public static ItemCategoriesProcessor itemCategoriesProcessor;

    public Map<String, Boolean> isMarketMenuOpen;

    public LocalizedStrings localizedStrings;

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
    }

    public void closeDatabase(){
        storage.closeConnection();
    }

    int getTotalPages(int totalItemsNumber){
        return (int) (1 + Math.floor((double)totalItemsNumber / 45.0));
    }

    public Inventory generateMainMenu(String playerName, String filter){

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

        /*
            Sorting items from 'items sold file' by time
         */

        long now = System.currentTimeMillis();



        ArrayList<ItemStackNotation> queryResult;

        if (filter == null) queryResult = storage.getAllKeysOrderByTime(playerCurrentPage.get(playerName));
        else queryResult = storage.getAllItemsFiltered(itemCategoriesProcessor.filterNotations.get(filter).filters, playerCurrentPage.get(playerName));

        if (queryResult != null){
            int currentSlot = 9;


            for (int key = 0; key < queryResult.size(); key++){
                if (currentSlot > 53) break;
                ItemStackNotation currentItemStackNotation = queryResult.get(key);

                ItemStack currentItemStack = ItemStack.deserializeBytes(currentItemStackNotation.bytes);

                String owner = currentItemStackNotation.owner;
                long price = currentItemStackNotation.price;
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

        ArrayList<ItemStackNotation> myItems = storage.getAllPlayerItems(player.getName());

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

        if (myItems != null){
            int keysSize = myItems.size();

            for (int key = 0; key < Math.min(54-9, keysSize); key++){
                ItemStackNotation currentItemStackNotation = myItems.get(key);
                ItemStack currentItemStack = (ItemStack.deserializeBytes(currentItemStackNotation.bytes));
                ArrayList<String> newLore = new ArrayList<>();
                long price = currentItemStackNotation.price;

                List<String> currentLore = currentItemStack.getLore();

                if (currentLore == null){
                    newLore.add(localizedStrings.price + ChatColor.GOLD + price + localizedStrings.currency);
                    newLore.addAll(localizedStrings.pressToWithdrawFromSaleLore);
                }
                else{
                    if (!currentLore.contains(localizedStrings.price + ChatColor.GOLD + price + localizedStrings.currency)){
                        newLore.add(localizedStrings.price + ChatColor.GOLD + price + localizedStrings.currency);
                    }

                    newLore.addAll(currentLore);

                    if (!new HashSet<>(currentLore).containsAll(localizedStrings.pressToWithdrawFromSaleLore)){
                        newLore.addAll(localizedStrings.pressToWithdrawFromSaleLore);
                    }
                }

                currentItemStack.setLore(newLore);
                inventory.setItem(key + 9, currentItemStack);
            }
        }

        menus.put("MY_SOLD_ITEMS", inventory);

        return inventory;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        //TODO Добавить макс. количество предметов для продажи для каждой привилегии
        //TODO Добавить перевод языка для купленного предмета (ENG --> ANY)

        if (!(commandSender instanceof Player)){
            commandSender.sendMessage(ChatColor.RED + "This command can be used only by player!");
            return true;
        }

        Player player = (Player) commandSender;
        String playerName = player.getName();

        if (strings.length == 0){
            playerCurrentMenu.put(player.getName(), "MAIN_MENU");
            playerCurrentPage.put(player.getName(), 0);
            playerCurrentItemFilter.put(player.getName(), null);
            isMarketMenuOpen.put(playerName, true);
            player.openInventory(generateMainMenu(player.getName(), null));
        } else if (strings.length == 1) {
            if (!MarketTabComplete.list.get(0).contains(strings[0])){
                player.sendMessage(ChatColor.RED + "Неверное использования команды /market (/ah).");
                player.sendMessage(ChatColor.GREEN + "Попробуйте так: /market <sell | buy | bid> <ЦенаТовара> <full | solo>");
                player.sendMessage(ChatColor.GRAY + "(продаваемый предмет надо держать в руке)");
            }
            else{
                player.sendMessage(ChatColor.GOLD + "Укажите цену!" + ChatColor.DARK_GREEN +  " Например," +  ChatColor.GOLD + " /ah <sell | buy | bid> 500 ...");
            }
        }
        else if (strings.length == 2){
            ItemStack itemToSell = player.getItemInHand();

            if (player.getItemInHand().getTranslationKey().equals("block.minecraft.air")){
                player.sendMessage(ChatColor.RED + "Продаваемый предмет надо держать в руке!");
                return true;
            }

            long price = Long.parseLong(strings[1]);

            if (price < 0){
                player.sendMessage(ChatColor.RED + "Вы указали цену меньше 0. Цена должна быть неотрицательной!");
                return true;
            }

            String unique_key = new Random().ints('a', 'z' + 1).limit(64).collect(StringBuilder::new,
                    StringBuilder::appendCodePoint, StringBuilder::append).toString();

            ItemMeta meta = itemToSell.getItemMeta();

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
            pdc.set(namespacedKey, PersistentDataType.STRING, unique_key);

            itemToSell.setItemMeta(meta);

            //ItemSerialization.saveInventory(itemToSell, configuration, unique_id, Long.parseLong(strings[1]), player.getName(), System.nanoTime());


            storage.addItem(unique_key, player.getName(), price, System.nanoTime(), itemToSell.getType().toString(), itemToSell);

            player.getItemInHand().setAmount(0);
            player.sendMessage(ChatColor.GOLD + "Вы успешно выставили предмет на продажу!");

            if (price == 0){
                player.sendMessage(ChatColor.AQUA + "" + ChatColor.ITALIC + "Так как Вы указали цену равную 0, Вы не получите денег за продажу товара.");
            }

        }
        return true;
    }

    public void onMenuItemClick(Player player, ItemStack item){

        String playerName = player.getName();
        String currentMenu = playerCurrentMenu.get(playerName);


        if (currentMenu.equals("MAIN_MENU")){

            String menu_item_key = "";

            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
            if (pdc.has(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING)){
                menu_item_key = pdc.get(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING);
                assert menu_item_key != null;

                String currentFilter = playerCurrentItemFilter.get(playerName);
                ArrayList<String> filters;
                if (currentFilter == null) filters = null;
                else{
                    filters = itemCategoriesProcessor.filterNotations.get(currentFilter).filters;
                }

                int pagesNum = getTotalPages(storage.getTotalItems(filters)) - 1;

                int currentPage = playerCurrentPage.get(playerName);

                if (menu_item_key.equals("MY_SOLD_ITEMS")) { // my sold items
                    playerCurrentMenu.put(playerName, "MY_SOLD_ITEMS");
                    player.openInventory(generateMySoldItemsMenu(player));
                }
                else if (menu_item_key.equals("UPDATE_PAGE")){
                    playerCurrentPage.put(playerName, Math.max(0, Math.min(currentPage, pagesNum)));
                    player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName)));
                }
                else if (menu_item_key.equals("PAGE_LEFT")){
                    playerCurrentPage.put(playerName, Math.max(0, Math.min(currentPage - 1, pagesNum)));
                    player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName)));
                }
                else if (menu_item_key.equals("PAGE_RIGHT")){
                    playerCurrentPage.put(playerName, Math.max(0, Math.min(currentPage + 1, pagesNum)));
                    player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName)));
                }
                else if (menu_item_key.equals("CATEGORIES_MENU")){
                    playerCurrentMenu.put(playerName, "CATEGORIES_MENU");
                    playerCurrentItemFilter.put(playerName, null);
                    player.openInventory(generateFiltersInventory());
                }
            }
            else{
                if (getServer().getPluginManager().getPlugin("Vault") == null) {
                    return;
                }
                RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
                if (rsp == null) {
                    return;
                }
                long balance = (long) rsp.getProvider().getBalance(playerName);

                String unique_key = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "unique_key"), PersistentDataType.STRING);

                if (storage.getItem(unique_key) == null){
                    player.sendMessage(ChatColor.AQUA + "Этот предмет уже продан!");
                    player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName)));
                    return;
                }

                ItemStackNotation notation = storage.getItem(unique_key);
                String itemOwner = notation.owner;
                long price = notation.price;

                if (Objects.equals(itemOwner, playerName)){
                    player.sendMessage(ChatColor.GOLD + "Нельзя купить свой предмет!");
                    return;
                }
                if (!(balance >= price)){
                    player.sendMessage(ChatColor.RED + "Вы не можете купить это! Недостаточно монет!");
                    return;
                }

                ItemMeta meta = item.getItemMeta();


                pdc = meta.getPersistentDataContainer();
                NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
                pdc.remove(namespacedKey);
                item.setItemMeta(meta);

                ArrayList<String> currentLore = new ArrayList<>(Objects.requireNonNull(item.getLore()));
                ArrayList<String> newLore = new ArrayList<>();
                for (String loreString: currentLore){
                    if (!((loreString.startsWith(ChatColor.BLUE + "Цена: " + ChatColor.GOLD) && loreString.contains("✪"))
                            || (loreString.equals(ChatColor.GREEN + "" + ChatColor.ITALIC + "(Нажми, чтобы снять с продажи)"))
                            || (loreString.startsWith(ChatColor.BLUE + "Продавец: ")))){
                        newLore.add(loreString);
                    }
                }
                item.setLore(newLore);

                boolean hasEmptySlot = false;

                for (int slot = 35; slot >= 0; slot--){
                    if (player.getInventory().getItem(slot) == null){
                        player.getInventory().setItem(slot, item);
                        slot = -1;
                        hasEmptySlot = true;
                    }
                }


//                try {
//                    configuration.save(file);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }

                if (!hasEmptySlot){
                    player.sendMessage(ChatColor.RED + "Освободите место в инвентаре, чтобы снять предмет с продажи!");
                    return;
                }

                rsp.getProvider().withdrawPlayer(playerName, price);
                rsp.getProvider().depositPlayer(itemOwner, price);


                if (getServer().getOnlinePlayers().contains(getServer().getPlayer(itemOwner))) {
                    String message = "";
                    if (item.getItemMeta().getDisplayName().isEmpty()){
                        message = ChatColor.GREEN + "Игрок " + ChatColor.LIGHT_PURPLE + playerName + ChatColor.GREEN + " купил " + ChatColor.AQUA + item.getI18NDisplayName() + ChatColor.GRAY + " (x" + item.getAmount() + ")" + ChatColor.GREEN + " за " + ChatColor.GOLD + price + " ✪";
                    }
                    else{
                        message = ChatColor.GREEN + "Игрок " + ChatColor.LIGHT_PURPLE + playerName + ChatColor.GREEN + " купил " + ChatColor.AQUA + item.getItemMeta().getDisplayName() + ChatColor.GRAY + " (x" + item.getAmount() + ")" + ChatColor.GREEN + " за " + ChatColor.GOLD + price + " ✪";
                    }
                    Objects.requireNonNull(getServer().getPlayer(itemOwner)).sendMessage(message);
                }
                player.sendMessage(ChatColor.GREEN + "Вы успешно приобрели " + ChatColor.AQUA + item.getI18NDisplayName() + ChatColor.GRAY + " (x" + item.getAmount() + ")" + ChatColor.GREEN + " за " + ChatColor.GOLD + price + "✪ !");

                storage.removeItem(unique_key);
                player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName)));
            }
        }
        else if (currentMenu.equals("MY_SOLD_ITEMS")){

            String menu_item_key = "";

            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
            if (pdc.has(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING)){
                menu_item_key = pdc.get(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING);
                if (menu_item_key.equals("MAIN_MENU")){
                    player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName)));
                    playerCurrentMenu.put(playerName, "MAIN_MENU");
                }
            }
            else{
                boolean hasEmptySlot = false;

                for (int slot = 35; slot >= 0; slot--){
                    if (player.getInventory().getItem(slot) == null){

                        ItemMeta meta = item.getItemMeta();

                        String unique_key = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "unique_key"), PersistentDataType.STRING);

                        if (storage.getItem(unique_key) == null){
                            player.sendMessage(ChatColor.AQUA + "Этот предмет уже продан!");
                            player.openInventory(generateMySoldItemsMenu(player));
                            return;
                        }

                        pdc = meta.getPersistentDataContainer();
                        NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
                        pdc.remove(namespacedKey);
                        item.setItemMeta(meta);

                        ArrayList<String> currentLore = new ArrayList<>(Objects.requireNonNull(item.getLore()));
                        ArrayList<String> newLore = new ArrayList<>();
                        for (String loreString: currentLore){
                            if (!((loreString.startsWith(ChatColor.BLUE + "Цена: " + ChatColor.GOLD) && loreString.contains("✪")) || (loreString.equals(ChatColor.GREEN + "" + ChatColor.ITALIC + "(Нажми, чтобы снять с продажи)")))){
                                newLore.add(loreString);
                            }
                        }

                        item.setLore(newLore);

                        storage.removeItem(unique_key);

                        player.getInventory().setItem(slot, item);
                        player.openInventory(generateMySoldItemsMenu(player));
                        slot = -1;
                        hasEmptySlot = true;
                    }
                }

                if (!hasEmptySlot){
                    player.sendMessage(ChatColor.RED + "Освободите место в инвентаре, чтобы снять предмет с продажи!");
                }
            }


        }
        else if (currentMenu.equals("CATEGORIES_MENU")){
            String menu_item_key = "";

            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
            if (pdc.has(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING)){
                menu_item_key = pdc.get(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING);
                if (menu_item_key.equals("MAIN_MENU")){
                    player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName)));
                    playerCurrentMenu.put(playerName, "MAIN_MENU");
                }
                else if (menu_item_key.contains("FILTER:")){
                    String filterName = menu_item_key.split(":")[1];
                    playerCurrentPage.put(playerName, 0);
                    playerCurrentItemFilter.put(playerName, filterName);
                    playerCurrentMenu.put(playerName, "MAIN_MENU");

                    player.openInventory(generateMainMenu(playerName, playerCurrentItemFilter.get(playerName)));
                }
            }
        }

    }
}
