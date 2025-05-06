package org.intrigger.ultimate_market.utils;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import java.sql.*;

@SuppressWarnings("unchecked")
public class ItemStorage {

    String storageFilePath;
    Connection conn;

    public ItemStorage(String _storageFilePath) {
        storageFilePath = _storageFilePath;
        conn = null;
        createParser();
    }

    public void closeConnection() {
        try{
            conn.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void createParser() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + storageFilePath + "db.db");
            Statement statement = conn.createStatement();

            String sql = "CREATE TABLE IF NOT EXISTS items (" +
                    "KEY TEXT PRIMARY KEY NOT NULL," +
                    "TIME INT," +
                    "OWNER TEXT," +
                    "PRICE REAL," +
                    "MATERIAL TEXT," +
                    "BYTES TEXT," +
                    "AMOUNT INT," +
                    "FULL INT" +
                    ");";
            statement.execute(sql);
            statement.closeOnCompletion();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        Bukkit.getLogger().info(ChatColor.GREEN + "Ultimate Market's ITEMS table was Created Successfully!");
    }

    public void addItem(String key, String ownerName, double price, long time, String material, ItemStack item, int amount, int full){
        byte[] itemBytes = item.serializeAsBytes();

        String sql = "INSERT INTO items (KEY, TIME, OWNER, PRICE, MATERIAL, BYTES, AMOUNT, FULL) VALUES(?, ?, ?, ?, ?, ?, ?, ?);";

        try {

            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, key);
            statement.setLong(2, time);
            statement.setString(3, ownerName);
            statement.setDouble(4, price);
            statement.setString(5, material);
            statement.setBytes(6, itemBytes);
            statement.setInt(7, amount);
            statement.setInt(8, full);
            statement.execute();
            statement.closeOnCompletion();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public ItemStackNotation getItem(String key){

        ItemStackNotation resultNotation = null;

        String sql = "SELECT * FROM items WHERE KEY = ? LIMIT 1";
        ResultSet result;
        try {

            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, key);
            result = statement.executeQuery();
            statement.closeOnCompletion();

            if (!result.next()) return null;

            resultNotation = new ItemStackNotation(
                    result.getString("key"),
                    result.getString("owner"),
                    result.getDouble("price"),
                    result.getLong("time"),
                    result.getBytes("bytes"),
                    result.getInt("amount"),
                    result.getInt("full"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultNotation;
    }

    public void setAmount(String key, int amount, byte[] bytes){
        String sql = "UPDATE items SET amount = ?, bytes = ? WHERE key = ?";
        try {

            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, amount);
            statement.setBytes(2, bytes);
            statement.setString(3, key);
            statement.execute();
            statement.closeOnCompletion();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getAmount(String key){

        if (getItem(key) == null) return 0;

        String sql = "SELECT amount FROM items WHERE key = ?";
        ResultSet result;
        try {

            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, key);
            result =  statement.executeQuery();
            statement.closeOnCompletion();
            return result.getInt("amount");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int playerItemsSoldNow(String playerName){
        String sql = "SELECT COUNT() FROM items WHERE owner = \"" + playerName + "\";";
        try{
            PreparedStatement statement;
            statement = conn.prepareStatement(sql);

            ResultSet result = statement.executeQuery();
            statement.closeOnCompletion();

            result.next();
            return result.getInt(1);

        } catch (SQLException e){
            e.printStackTrace();
        }
        return -1;
    }

    public ArrayList<ItemStackNotation> getAllKeys(int page, String sortingType) {

        Map<String, Long> timestamps_start = new HashMap<>();
        Map<String, Long> timestamps_stop = new HashMap<>();

        String order_by_what = "TIME", asc_or_desc = "DESC";
        switch (sortingType){
            case "NEW_FIRST":
                order_by_what = "TIME";
                asc_or_desc = "DESC";
                break;
            case "OLD_FIRST":
                order_by_what = "TIME";
                asc_or_desc = "ASC";
                break;
            case "CHEAP_FIRST":
                order_by_what = "PRICE";
                asc_or_desc = "ASC";
                break;
            case "EXPENSIVE_FIRST":
                order_by_what = "PRICE";
                asc_or_desc = "DESC";
                break;
        }

        String sql = "SELECT * FROM items ORDER BY " + order_by_what + " + 0 " + asc_or_desc + " limit 45 offset ?;";
        //String sql = "SELECT * FROM items;";

        // 100 элементов -> 0.15 ms
        // 1000 -> 0.35 ms
        // 10000 -> 1.25 ms
        // 100'000 -> 9.0 ms
        // 1'000'000 -> 82 ms


        ResultSet result;
        try {
            PreparedStatement statement;
            statement = conn.prepareStatement(sql);

            statement.setInt(1, page * 45);

            timestamps_start.put("SQL_QUERY", System.nanoTime());

            result = statement.executeQuery();
            statement.closeOnCompletion();

            timestamps_stop.put("SQL_QUERY", System.nanoTime());

            ArrayList<ItemStackNotation> itemsToReturn = new ArrayList<>();

            //Временно убираю это для проверки (00:26 01.05.2025)

//            for (int i = 0; i < (page * 45); i++){
//                result.next();
//            }

            while (result.next()){
                itemsToReturn.add(new ItemStackNotation(result.getString("key"),
                                                            result.getString("owner"),
                                                            result.getDouble("price"),
                                                            result.getLong("time"),
                                                            result.getBytes("bytes"),
                                                            result.getInt("amount"),
                                                            result.getInt("full"))
                );
            }

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

            //itemsToReturn.sort((a, b) -> {return Double.compare(a.price, b.price); });

            //itemsToReturn = new ArrayList<>(itemsToReturn.subList(45 * page, 45 * page + 45));

            //allItems = itemsToReturn;

            return itemsToReturn;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<ItemStackNotation> getPlayerItems(String playerName, int page){
        String sql = "SELECT * FROM items WHERE owner = ?;";
        try{
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, playerName);
            ResultSet result = statement.executeQuery();
            statement.closeOnCompletion();

            ArrayList<ItemStackNotation> itemsToReturn = new ArrayList<>();


            for (int i = 0; i < (page * 45); i++){
                result.next();
            }

            for (int i = 0; i < 45; i++){
                if (!result.next()) break;
                itemsToReturn.add(new ItemStackNotation(result.getString("key"),
                                result.getString("owner"),
                                result.getDouble("price"),
                                result.getLong("time"),
                                result.getBytes("bytes"),
                                result.getInt("amount"),
                                result.getInt("full"))
                );
            }
            return itemsToReturn;

        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<ItemStackNotation> getAllPlayerItems(String playerName){
        String sql = "SELECT * FROM items WHERE owner = ? ORDER BY TIME DESC;";
        try{
            PreparedStatement statement = conn.prepareStatement(sql);

            statement.setString(1, playerName);

            ResultSet result = statement.executeQuery();

            if (result.isClosed()) return null;

            ArrayList<ItemStackNotation> itemsToReturn = new ArrayList<>();

            while (result.next()){
                itemsToReturn.add(
                        new ItemStackNotation(result.getString("key"),
                                result.getString("owner"),
                                result.getDouble("price"),
                                result.getLong("time"),
                                result.getBytes("bytes"),
                                result.getInt("amount"),
                                result.getInt("full"))
                );
            }
            return itemsToReturn;

        } catch (SQLException e){
            e.printStackTrace();
        }

        return null;
    }
    public void removeItem(String unique_key){
        String sql = "DELETE FROM items WHERE key = ?;";
        try{
            PreparedStatement statement = conn.prepareStatement(sql);

            statement.setString(1, unique_key);

            statement.execute();
            statement.closeOnCompletion();

        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public int getTotalItems(ArrayList<String> filters){
        String sql;
        if (filters == null) sql = "SELECT COUNT() FROM items;";
        else {
            sql = "SELECT COUNT() FROM items WHERE ";

            for (int i = 0; i < filters.size() - 1; i++){
                sql += "material = " + "\"" + filters.get(i) + "\"" + " OR ";
            }

            sql += "material = " + "\"" + filters.get(filters.size() - 1) + "\"";
        }
        try{
            PreparedStatement statement = conn.prepareStatement(sql);
            ResultSet result = statement.executeQuery();

            result.next();
            statement.closeOnCompletion();

            return result.getInt(1);

        } catch (SQLException e){
            e.printStackTrace();
        }
        return 0;
    }

    public ArrayList<ItemStackNotation> getAllItemsFiltered(ArrayList<String> filters, int page, String sortingType){
        String order_by_what = "TIME", asc_or_desc = "DESC";
        switch (sortingType){
            case "NEW_FIRST":
                order_by_what = "TIME";
                asc_or_desc = "DESC";
                break;
            case "OLD_FIRST":
                order_by_what = "TIME";
                asc_or_desc = "ASC";
                break;
            case "CHEAP_FIRST":
                order_by_what = "PRICE";
                asc_or_desc = "ASC";
                break;
            case "EXPENSIVE_FIRST":
                order_by_what = "PRICE";
                asc_or_desc = "DESC";
                break;
        }

        String sql = "SELECT * FROM items WHERE ";

        for (int i = 0; i < filters.size() - 1; i++){
            sql += "material = " + "\"" + filters.get(i) + "\"" + " OR ";
        }

        sql += "material = " + "\"" + filters.get(filters.size() - 1) + "\"";

        sql += "ORDER BY " + order_by_what + " " + asc_or_desc;

        try{
            PreparedStatement statement = conn.prepareStatement(sql);
            ResultSet result = statement.executeQuery();
            statement.closeOnCompletion();

            ArrayList<ItemStackNotation> itemsToReturn = new ArrayList<>();


            for (int i = 0; i < (page * 45); i++){
                result.next();
            }

            for (int i = 0; i < 45; i++){
                if (!result.next()) break;
                itemsToReturn.add(new ItemStackNotation(result.getString("key"),
                                result.getString("owner"),
                                result.getDouble("price"),
                                result.getLong("time"),
                                result.getBytes("bytes"),
                                result.getInt("amount"),
                                result.getInt("full"))
                );
            }
            return itemsToReturn;


        } catch (SQLException e){
            e.printStackTrace();
        }

        return null;
    }
}
