package org.intrigger.ultimate_market.utils;

import java.util.*;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
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

    public void createParser() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + storageFilePath + "sold_items.db");
            Statement statement = conn.createStatement();

            String sql = "CREATE TABLE IF NOT EXISTS items (" +
                    "KEY TEXT PRIMARY KEY NOT NULL," +
                    "TIME INT," +
                    "OWNER TEXT," +
                    "PRICE INT," +
                    "BYTES TEXT" +
                    ");";
            statement.execute(sql);
            statement.closeOnCompletion();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Bukkit.getLogger().info(ChatColor.GREEN + "Ultimate Market's Items Database Created Successfully!");
    }

    public void addItem(String key, String ownerName, long price, long time, ItemStack item){
        byte[] itemBytes = item.serializeAsBytes();

        String sql = "INSERT INTO items (KEY, TIME, OWNER, PRICE, BYTES) VALUES(?, ?, ?, ?, ?);";

        try {

            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, key);
            statement.setLong(2, time);
            statement.setString(3, ownerName);
            statement.setLong(4, price);
            statement.setBytes(5, itemBytes);
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
                    result.getLong("price"),
                    result.getLong("time"),
                    result.getBytes("bytes"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultNotation;
    }


    public ArrayList<ItemStackNotation> getAllKeysOrderByTime(int page){

        String sql = "SELECT * FROM items ORDER BY time DESC;";
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
                                                            result.getLong("price"),
                                                            result.getLong("time"),
                                                            result.getBytes("bytes")
                        )
                );
            }


            return itemsToReturn;

        } catch (SQLException e) {
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
                                result.getLong("price"),
                                result.getLong("time"),
                                result.getBytes("bytes")
                        )
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
    public int getTotalItems(){
        String sql = "SELECT COUNT() FROM items;";
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
}
