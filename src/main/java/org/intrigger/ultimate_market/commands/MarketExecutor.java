package org.intrigger.ultimate_market.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;


import java.io.*;
import java.util.*;

import static org.bukkit.Bukkit.getServer;
import static org.intrigger.ultimate_market.Ultimate_market.LOGGER;

public class MarketExecutor implements CommandExecutor  {

    public static Map<String, Inventory> menus;
    public static Map<String, String> playerCurrentMenu;

    private static Plugin plugin;

    public MarketExecutor(Plugin _plugin){
        menus = new HashMap<>();
        playerCurrentMenu = new HashMap<>();
        plugin = _plugin;
    }
    public Inventory generateMainMenu(){
        String inventoryName = "Ultimate Market Menu";
        int inventorySize = 54;
        Inventory inventory = Bukkit.createInventory(null, inventorySize, inventoryName);

        ItemStack mySlots = new ItemStack(Material.CHEST);
        ItemMeta mySlotsMeta = mySlots.getItemMeta();
        mySlotsMeta.setDisplayName(ChatColor.GOLD + "Мой Аукцион");
        List<String> lore = Arrays.asList(ChatColor.GREEN + "Нажми, чтобы увидеть",
                ChatColor.GREEN + "свои предметы на продаже.");
        mySlotsMeta.setLore(lore);

        PersistentDataContainer pdc = mySlotsMeta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "MY_SOLD_ITEMS");
        mySlots.setItemMeta(mySlotsMeta);
        inventory.setItem(0, mySlots);

        File file = new File( "plugins/Ultimate Market/sold_items.yml");
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        ArrayList<String> myKeys = new ArrayList<>();

        Set<String> keysSet =  Objects.requireNonNull(configuration.getKeys(false));
        String[] keys = new String[keysSet.size()];
        keysSet.toArray(keys);

        Map<String, Long> uniqueKeyAndTime = new HashMap<>();
        LinkedHashMap<String, Long> sortedMap = new LinkedHashMap<>();


        for (String key: keys){
            uniqueKeyAndTime.put(key, configuration.getLong(key + ".time"));
        }

        ArrayList<Long> sortedKeysByTime = new ArrayList<>();

        for (Map.Entry<String, Long> entry: uniqueKeyAndTime.entrySet()){
            sortedKeysByTime.add(entry.getValue());
        }
        sortedKeysByTime.sort(new Comparator<Long>() {
            public int compare(Long l1, Long l2) {
                long dif = l1 - l2;
                if (dif > 0) return -1;
                else if (dif < 0) return 1;
                else return 0;
            }
        });

        for (Long value: sortedKeysByTime){
            for (Map.Entry<String, Long> entry: uniqueKeyAndTime.entrySet()){
                if (entry.getValue().equals(value)){
                    sortedMap.put(entry.getKey(), value);
                }
            }
        }

        int keysSize = keys.length;

        int currentSlot = 9;

        for (Map.Entry<String, Long> entry : sortedMap.entrySet()){
            if (currentSlot > 53) break;
            String key = entry.getKey();
            ItemStack currentItemStack = ItemSerialization.toInventory(configuration, key);
            String owner = configuration.getString(key + ".owner");
            long price = configuration.getLong(key + ".price");
            ArrayList<String> newLore = new ArrayList<String>();
            newLore.add(ChatColor.BLUE + "Продавец: " + ChatColor.LIGHT_PURPLE + owner);
            newLore.add(ChatColor.BLUE + "Цена: " + ChatColor.GOLD + price + " ✪");

            ItemMeta currentItemMeta = currentItemStack.getItemMeta();
            List<String> currentLore = currentItemStack.getLore();

            if (currentLore != null)
                newLore.addAll(currentLore);

            currentItemMeta.setLore(newLore);
            currentItemStack.setItemMeta(currentItemMeta);
            inventory.setItem(currentSlot, currentItemStack);
            currentSlot++;
        }

        menus.put("MAIN_MENU", inventory);

        return inventory;
    }

    public Inventory generateMySoldItemsMenu(Player player){
        String inventoryName = "Ultimate Market Menu";
        int inventorySize = 54;
        Inventory inventory = Bukkit.createInventory(null, inventorySize, inventoryName);

        File file = new File( "plugins/Ultimate Market/sold_items.yml");
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        ArrayList<String> myKeys = new ArrayList<>();

        Set<String> keysSet =  Objects.requireNonNull(configuration.getKeys(false));
        String[] keys = new String[keysSet.size()];
        keysSet.toArray(keys);
        for (String key: keys){
            String itemOwner = Objects.requireNonNull(configuration.getConfigurationSection(key)).getString("owner");
            if (itemOwner == null) continue;
            if (itemOwner.equals(player.getName())){
                myKeys.add(key);
            }
        }

        ItemStack homeItem = new ItemStack(Material.CHEST);

        ItemMeta mySlotsMeta = homeItem.getItemMeta();
        mySlotsMeta.setDisplayName(ChatColor.GOLD + "Аукцион");
        List<String> lore = Arrays.asList(ChatColor.GREEN + "Нажми, чтобы вернуться",
                ChatColor.GREEN + "в главное меню");
        mySlotsMeta.setLore(lore);
        homeItem.setItemMeta(mySlotsMeta);
        ItemMeta homeItemMeta = homeItem.getItemMeta();

        PersistentDataContainer pdc = homeItemMeta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(plugin, "menu_item_key");
        pdc.set(namespacedKey, PersistentDataType.STRING, "MAIN_MENU");

        homeItem.setItemMeta(homeItemMeta);
        inventory.setItem(0, homeItem);

        int keysSize = myKeys.size();

        for (int key = 0; key < keysSize; key++){
            ItemStack currentItemStack = ItemSerialization.toInventory(configuration, myKeys.get(key));
            ArrayList<String> newLore = new ArrayList<>();
            long price = configuration.getLong(myKeys.get(key) + ".price");
            newLore.add(ChatColor.BLUE + "Цена: " + ChatColor.GOLD + price + " ✪");
            List<String> currentLore = currentItemStack.getLore();
            if (currentLore != null)
                newLore.addAll(currentLore);

            newLore.add(ChatColor.GREEN + "" + ChatColor.ITALIC + "(Нажми, чтобы снять с продажи)");
            currentItemStack.setLore(newLore);
            inventory.setItem(key + 9, currentItemStack);
        }

        menus.put("MY_SOLD_ITEMS", inventory);

        return inventory;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player)){
            commandSender.sendMessage(ChatColor.RED + "This command can be used only by player!");
            return true;
        }

        Player player = (Player) commandSender;

        if (strings.length == 0){
            playerCurrentMenu.put(player.getName(), "MAIN_MENU");
            player.openInventory(generateMainMenu());
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


            File file = new File( "plugins/Ultimate Market/sold_items.yml");
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);

            int itemID = -1;

            if (configuration.getConfigurationSection(player.getName()) != null){
                Set<String> keysSet =  Objects.requireNonNull(configuration.getConfigurationSection(player.getName())).getKeys(false);
                String[] keys = new String[keysSet.size()];
                keysSet.toArray(keys);
                itemID = Integer.parseInt(keys[keys.length - 1]);
            }

            String unique_id = new Random().ints('a', 'z' + 1).limit(64).collect(StringBuilder::new,
                    StringBuilder::appendCodePoint, StringBuilder::append).toString();

            ItemMeta meta = itemToSell.getItemMeta();

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
            pdc.set(namespacedKey, PersistentDataType.STRING, unique_id);

            itemToSell.setItemMeta(meta);

            ItemSerialization.saveInventory(itemToSell, configuration, unique_id, Long.parseLong(strings[1]), player.getName(), System.nanoTime());

            try {
                configuration.save(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            player.getItemInHand().setAmount(0);
            player.sendMessage(ChatColor.GOLD + "Вы успешно выставили предмет на продажу!");

        }


        return true;
    }

    public void onMenuItemClick(Player player, ItemStack item){

        String currentMenu = playerCurrentMenu.get(player.getName());

        if (currentMenu.equals("MAIN_MENU")){

            String menu_item_key = "";

            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
            if (pdc.has(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING)){
                menu_item_key = pdc.get(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING);
                assert menu_item_key != null;
                if (menu_item_key.equals("MY_SOLD_ITEMS")) { // my sold items
                    player.openInventory(generateMySoldItemsMenu(player));
                    playerCurrentMenu.put(player.getName(), "MY_SOLD_ITEMS");
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
                long balance = (long) rsp.getProvider().getBalance(player.getName());
                File file = new File( "plugins/Ultimate Market/sold_items.yml");
                FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
                String unique_key = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "unique_key"), PersistentDataType.STRING);

                if (configuration.getConfigurationSection(unique_key) == null){
                    player.sendMessage(ChatColor.AQUA + "Этот предмет уже продан!");
                    return;
                }

                assert unique_key != null;
                String itemOwner = Objects.requireNonNull(configuration.getConfigurationSection(unique_key)).getString("owner");
                long price = Objects.requireNonNull(configuration.getConfigurationSection(unique_key)).getLong("price");

                if (Objects.equals(itemOwner, player.getName())){
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

                assert unique_key != null;
                configuration.set(unique_key, null);

                try {
                    configuration.save(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (!hasEmptySlot){
                    player.sendMessage(ChatColor.RED + "Освободите место в инвентаре, чтобы снять предмет с продажи!");
                }

                rsp.getProvider().withdrawPlayer(player.getName(), price);
                rsp.getProvider().depositPlayer(itemOwner, price);

                getServer().getPlayer(itemOwner).sendMessage(ChatColor.GREEN + "Игрок " + ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.GREEN + " купил " + ChatColor.AQUA + item.getItemMeta().getDisplayName() + ChatColor.GREEN + " за " + ChatColor.GOLD + price + " ✪ !");
                player.sendMessage(ChatColor.GREEN + "Вы успешно приобрели предмет за " + ChatColor.GOLD + price + "✪ !");
                player.openInventory(generateMainMenu());
            }
        }
        else if (currentMenu.equals("MY_SOLD_ITEMS")){

            String menu_item_key = "";

            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
            if (pdc.has(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING)){
                menu_item_key = pdc.get(new NamespacedKey(plugin, "menu_item_key"), PersistentDataType.STRING);
                if (menu_item_key.equals("MAIN_MENU")){
                    player.openInventory(generateMainMenu());
                    playerCurrentMenu.put(player.getName(), "MAIN_MENU");
                }
            }
            else{
                boolean hasEmptySlot = false;

                for (int slot = 35; slot >= 0; slot--){
                    if (player.getInventory().getItem(slot) == null){
                        File file = new File( "plugins/Ultimate Market/sold_items.yml");
                        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);

                        ItemMeta meta = item.getItemMeta();

                        String unique_key = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "unique_key"), PersistentDataType.STRING);

                        if (configuration.getConfigurationSection(unique_key) == null){
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

                        assert unique_key != null;
                        configuration.set(unique_key, null);
                        try {
                            configuration.save(file);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
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


    }

}
