package org.intrigger.ultimate_market.utils.Interface.Menus;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.intrigger.ultimate_market.Ultimate_market;
import org.intrigger.ultimate_market.utils.Interface.Button;
import org.intrigger.ultimate_market.utils.Interface.Menu;
import org.intrigger.ultimate_market.utils.ItemStackNotation;
import org.intrigger.ultimate_market.utils.ItemUtils.PutData;
import org.intrigger.ultimate_market.utils.NumberUtil;
import org.intrigger.ultimate_market.utils.Pair;
import org.intrigger.ultimate_market.utils.StringDeserializer;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static org.intrigger.ultimate_market.Ultimate_market.*;

public class MyShop extends Menu {
    public MyShop(String _name, String _title, Map<String, Button> _buttons) {
        super(_name, _title, _buttons);
    }

    @Override
    public Inventory generateInventory(String playerName){
        int inventorySize = 54;

        Inventory inventory = Bukkit.createInventory(null,
                inventorySize, StringDeserializer.deserialize(
                        gui.cm.format(
                                gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).title
                        )
                )
        );
        

        //
        // Back to main menu button
        //
        
        ItemStack homeItem = PutData.put(new ItemStack(Material.CHEST), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "my_shop"),
                new Pair<>("button", "main_menu")
        ));

        ItemMeta homeItemMeta = homeItem.getItemMeta();
        homeItemMeta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("main_menu").title));
        homeItemMeta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("main_menu").lore));
        homeItem.setItemMeta(homeItemMeta);
        inventory.setItem(0, homeItem);

        //
        //  Update Page Button
        //

        ItemStack updatePage = PutData.put(new ItemStack(Material.SLIME_BALL), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "my_shop"),
                new Pair<>("button", "update_page")
        ));

        ItemMeta updatePageMeta = updatePage.getItemMeta();
        updatePageMeta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("update_page").title));
        updatePageMeta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("update_page").lore));
        updatePage.setItemMeta(updatePageMeta);
        inventory.setItem(4, updatePage);

        //
        // Page Left Button
        //
        ItemStack leftPage = PutData.put(new ItemStack(Material.PAPER), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "my_shop"),
                new Pair<>("button", "page_left")
        ));

        ItemMeta leftPageMeta = leftPage.getItemMeta();
        leftPageMeta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("page_left").title));
        leftPageMeta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("page_left").lore));
        leftPage.setItemMeta(leftPageMeta);
        inventory.setItem(3, leftPage);

        //
        // RIGHT PAGE
        //
        ItemStack rightPage = PutData.put(new ItemStack(Material.PAPER), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "my_shop"),
                new Pair<>("button", "page_right")
        ));

        ItemMeta rightPageMeta = rightPage.getItemMeta();
        rightPageMeta.displayName(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("page_right").title));
        rightPageMeta.lore(gui.cm.parseColoredText(gui.menus.get(Ultimate_market.info.current_menu.get(playerName)).buttons.get("page_right").lore));
        rightPage.setItemMeta(rightPageMeta);
        inventory.setItem(5, rightPage);

        ArrayList<ItemStackNotation> myItems = itemStorage.getPlayerItems(playerName, info.current_my_shop_page.get(playerName));

        if (myItems == null) return inventory;


        int keysSize = myItems.size();
        for (int key = 0; key < Math.min(5 * 9, keysSize); key++){
            Map<String, String> parse_args = new HashMap<>();
            ItemStackNotation itemStackNotation = myItems.get(key);
            String seller = itemStackNotation.owner;
            double price = itemStackNotation.price;


            parse_args.put("{SELLER}", seller);
            parse_args.put("{PRICE}", (itemStackNotation.full == 1) ? NumberUtil.formatClean(price, 2) : NumberUtil.formatClean(price * itemStackNotation.amount, 2));
            parse_args.put("{CURRENCY}", gui.messages.get("currency_symbol").get(0));

            if (!(itemStackNotation.full == 1 || itemStackNotation.amount == 1)){
                parse_args.put("{PRICE_PER_ONE}", NumberUtil.formatClean(price, 2));
            }

            inventory.setItem(key + 9, prepare_goods_item(itemStackNotation, parse_args));
        }

        return inventory;
    }

    private ItemStack prepare_goods_item(ItemStackNotation goods_item, Map<String, String> parse_args){
        ItemStack currentItemStack = PutData.put(ItemStack.deserializeBytes(goods_item.bytes), Arrays.asList(
                new Pair<>("plugin", "ultimate_market"),
                new Pair<>("menu", "my_shop"),
                new Pair<>("key", "goods"),
                new Pair<>("unique_key", goods_item.key)
        ));
        List<Component> current_lore = currentItemStack.getItemMeta().lore();
        List<String> original_lore = new ArrayList<>(gui.menus.get("my_shop").buttons.get("goods").lore);
        List<String> new_lore = new ArrayList<>();

        if ((goods_item.full == 1 || goods_item.amount == 1)){
            original_lore.remove(2);
        }

        for (String s: original_lore){
            new_lore.add(parse(s, parse_args));
        }
        if (current_lore == null) current_lore = new ArrayList<>();
        current_lore.addAll(gui.cm.parseColoredText(new_lore));
        currentItemStack.lore(current_lore);
        ItemMeta meta = currentItemStack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        currentItemStack.setItemMeta(meta);
        return currentItemStack;
    }

    private String parse(String s, Map<String, String> parse_args){
        String res = s;
        for (String i: parse_args.keySet()){
            res = res.replace(i, parse_args.get(i));
        }
        return res;
    }

    @Override
    public void onItemClick(InventoryClickEvent event){
        String playerName = event.getWhoClicked().getName();
        Player player = Bukkit.getPlayer(playerName);

        if (player == null) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;
        PersistentDataContainer pdc = clickedItem.getItemMeta().getPersistentDataContainer();
        if (pdc.has(new NamespacedKey(plugin, "button"), PersistentDataType.STRING)) {
            String menu_item_key = pdc.get(new NamespacedKey(plugin, "button"), PersistentDataType.STRING);
            assert menu_item_key != null;

            switch (menu_item_key) {
                case "main_menu": {
                    info.current_menu.put(playerName, "main_menu");
                    player.openInventory(gui.menus.get("main_menu").generateInventory(playerName));
                    break;
                }
                case "update_page":
                    info.current_my_shop_page.put(playerName, Math.max(0, Math.min(info.current_my_shop_page.get(playerName), itemStorage.getMyShopTotalPages(playerName))));
                    player.openInventory(gui.menus.get("my_shop").generateInventory(playerName));
                    break;
                case "page_left":
                    info.current_my_shop_page.put(playerName, Math.max(0, Math.min(info.current_my_shop_page.get(playerName) - 1, itemStorage.getMyShopTotalPages(playerName))));
                    player.openInventory(gui.menus.get("my_shop").generateInventory(playerName));
                    break;
                case "page_right":
                    info.current_my_shop_page.put(playerName, Math.max(0, Math.min(info.current_my_shop_page.get(playerName) + 1, itemStorage.getMyShopTotalPages(playerName))));
                    player.openInventory(gui.menus.get("my_shop").generateInventory(playerName));
                    break;
            }
            return;
        }
        if (pdc.has(new NamespacedKey(plugin, "key"), PersistentDataType.STRING)) {
            String key = pdc.get(new NamespacedKey(plugin, "key"), PersistentDataType.STRING);
            if (!Objects.equals(key, "goods")) return;
            if (pdc.has(new NamespacedKey(plugin, "unique_key"), PersistentDataType.STRING)) {
                String unique_key = pdc.get(new NamespacedKey(plugin, "unique_key"), PersistentDataType.STRING);

                boolean hasEmptySlot = false;

                for (int slot = 0; slot <= 35; slot++) {
                    if (player.getInventory().getItem(slot) == null) {
                        if (itemStorage.getItem(unique_key) == null) {
                            gui.send_message(player, "item_already_sold");
                            player.openInventory(gui.menus.get("my_shop").generateInventory(playerName));
                            return;
                        }
                        else{
                            ItemStack newItem = ItemStack.deserializeBytes(itemStorage.getItem(unique_key).bytes);
                            ItemMeta meta = newItem.getItemMeta();

                            pdc = meta.getPersistentDataContainer();
                            NamespacedKey namespacedKey = new NamespacedKey(plugin, "unique_key");
                            pdc.remove(namespacedKey);
                            newItem.setItemMeta(meta);

                            itemStorage.removeItem(unique_key);

                            player.getInventory().setItem(slot, newItem);
                            gui.send_message(player, "you_have_withdrawn_item");
                            player.openInventory(gui.menus.get("my_shop").generateInventory(playerName));
                            slot = 36;
                            hasEmptySlot = true;
                        }
                    }
                }

                if (!hasEmptySlot) {
                    gui.send_message(player, "free_up_your_inventory_space");
                }
            }
        }
    }
}
