package org.intrigger.ultimate_market.utils;

import java.util.*;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.intrigger.ultimate_market.commands.MarketExecutor;
import org.json.simple.JSONObject;

import java.sql.*;


@SuppressWarnings("unchecked")
public class ItemStorage {

    String storageFilePath;
    JSONObject data;
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
            conn = DriverManager.getConnection("jdbc:sqlite:" + storageFilePath + "sold_items.db");
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

        Bukkit.getLogger().info(ChatColor.GREEN + "Ultimate Market's Items Database Created Successfully!");
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

    public ArrayList<ItemStackNotation> getAllKeys(int page, String sortingType){

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

        String sql = "SELECT * FROM items ORDER BY " + order_by_what + " " + asc_or_desc + ";";
        ResultSet result;
        try {
            PreparedStatement statement;
            statement = conn.prepareStatement(sql);

            result = statement.executeQuery();
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
