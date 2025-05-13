package org.intrigger.ultimate_market.utils;

import org.intrigger.ultimate_market.Ultimate_market;
import java.sql.*;
import java.util.ArrayList;

import static org.intrigger.ultimate_market.Ultimate_market.itemCategoriesProcessor;

public class BuyRequestStorage {
    Connection conn;

    public BuyRequestStorage() {
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
            conn = Ultimate_market.itemStorage.conn;
            Statement statement = conn.createStatement();

            String sql = "CREATE TABLE IF NOT EXISTS buy_requests (" +
                    "KEY TEXT PRIMARY KEY NOT NULL," +
                    "TIME INT," +
                    "OWNER TEXT," +
                    "PRICE REAL," +
                    "MATERIAL TEXT," +
                    "BYTES TEXT," +
                    "AMOUNT_NOW INT," +
                    "AMOUNT_TAKEN INT," +
                    "AMOUNT_TOTAL INT" +
                    ");";
            statement.execute(sql);
            statement.closeOnCompletion();


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addRequest(BuyRequestNotation buyRequest){
        String sql = "INSERT INTO buy_requests (KEY, TIME, OWNER, PRICE, MATERIAL, BYTES, AMOUNT_NOW, AMOUNT_TAKEN, AMOUNT_TOTAL) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try {

            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, buyRequest.key);
            statement.setLong(2, buyRequest.time);
            statement.setString(3, buyRequest.owner);
            statement.setDouble(4, buyRequest.price);
            statement.setString(5, buyRequest.material);
            statement.setBytes(6, buyRequest.bytes);
            statement.setInt(7, buyRequest.amount_now);
            statement.setInt(8, buyRequest.amount_taken);
            statement.setInt(9, buyRequest.amount_total);
            statement.execute();
            statement.closeOnCompletion();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public BuyRequestNotation getBuyRequest(String unique_key){
        BuyRequestNotation resultNotation = null;

        String sql = "SELECT * FROM buy_requests WHERE KEY = ? LIMIT 1";
        ResultSet result;
        try {

            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, unique_key);
            result = statement.executeQuery();
            statement.closeOnCompletion();

            if (!result.next()) return null;

            resultNotation = new BuyRequestNotation(
                    result.getString("key"),
                    result.getLong("time"),
                    result.getString("owner"),
                    result.getDouble("price"),
                    result.getString("material"),
                    result.getBytes("bytes"),
                    result.getInt("amount_now"),
                    result.getInt("amount_taken"),
                    result.getInt("amount_total")
            );

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultNotation;
    }

    public void updateBuyRequest(String unique_key, int amount){
        String sql = "UPDATE buy_requests SET amount_now = amount_now + ? where key = ?;";

        try {

            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, amount);
            statement.setString(2, unique_key);
            statement.execute();
            statement.closeOnCompletion();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void finishBuyRequest(String unique_key){
        String sql = "delete from buy_requests where key = ?;";

        try {

            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, unique_key);
            statement.execute();
            statement.closeOnCompletion();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<BuyRequestNotation> getAllBuyRequestsFiltered(ArrayList<String> filters, int page, String sortingType){
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

        String sql = "SELECT * FROM buy_requests WHERE amount_now != amount_total and ";

        for (int i = 0; i < filters.size() - 1; i++){
            sql += "material = " + "\"" + filters.get(i) + "\"" + " OR ";
        }

        sql += "material = " + "\"" + filters.get(filters.size() - 1) + "\"";

        sql += "ORDER BY " + order_by_what + " " + asc_or_desc + " LIMIT 45 OFFSET " + (page * 45);

        ResultSet resultSet;

        try {
            PreparedStatement statement;
            statement = conn.prepareStatement(sql);
            
            resultSet = statement.executeQuery();
            statement.closeOnCompletion();


            ArrayList<BuyRequestNotation> buyRequests = new ArrayList<>();

            while (resultSet.next()){
                buyRequests.add(new BuyRequestNotation(
                        resultSet.getString("key"),
                        resultSet.getLong("time"),
                        resultSet.getString("owner"),
                        resultSet.getDouble("price"),
                        resultSet.getString("material"),
                        resultSet.getBytes("bytes"),
                        resultSet.getInt("amount_now"),
                        resultSet.getInt("amount_taken"),
                        resultSet.getInt("amount_total")
                ));
            }
            return buyRequests;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public ArrayList<BuyRequestNotation> getAllBuyRequests(String playerName, int page){
        String sql = "SELECT * FROM buy_requests where owner = ? limit 45 offset ?;";

        ResultSet resultSet;

        try {
            PreparedStatement statement;
            statement = conn.prepareStatement(sql);
            statement.setString(1, playerName);
            statement.setInt(2, page * 45);

            resultSet = statement.executeQuery();
            statement.closeOnCompletion();


            ArrayList<BuyRequestNotation> buyRequests = new ArrayList<>();

            while (resultSet.next()){
                buyRequests.add(new BuyRequestNotation(
                        resultSet.getString("key"),
                        resultSet.getLong("time"),
                        resultSet.getString("owner"),
                        resultSet.getDouble("price"),
                        resultSet.getString("material"),
                        resultSet.getBytes("bytes"),
                        resultSet.getInt("amount_now"),
                        resultSet.getInt("amount_taken"),
                        resultSet.getInt("amount_total")
                ));
            }
            return buyRequests;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<BuyRequestNotation> getAllBuyRequestsSorted(String sortingType, int page){

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

        String sql = "SELECT * FROM buy_requests order by " + order_by_what + " " + asc_or_desc + " limit 45 offset ?;";

        ResultSet resultSet;

        try {
            PreparedStatement statement;
            statement = conn.prepareStatement(sql);

            statement.setInt(1, page * 45);

            resultSet = statement.executeQuery();
            statement.closeOnCompletion();


            ArrayList<BuyRequestNotation> buyRequests = new ArrayList<>();

            while (resultSet.next()){
                buyRequests.add(new BuyRequestNotation(
                        resultSet.getString("key"),
                        resultSet.getLong("time"),
                        resultSet.getString("owner"),
                        resultSet.getDouble("price"),
                        resultSet.getString("material"),
                        resultSet.getBytes("bytes"),
                        resultSet.getInt("amount_now"),
                        resultSet.getInt("amount_taken"),
                        resultSet.getInt("amount_total")
                ));
            }
            return buyRequests;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public int getAllBuyRequests(String playerName){
        String sql = "SELECT count() FROM buy_requests where owner = ?;";

        ResultSet resultSet;

        int result = 0;

        try {
            PreparedStatement statement;
            statement = conn.prepareStatement(sql);
            statement.setString(1, playerName);

            resultSet = statement.executeQuery();
            statement.closeOnCompletion();

            while (resultSet.next()){
                result = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    public int getPagesNum(){
        String sql = "SELECT count() FROM buy_requests WHERE amount_now != amount_total;";

        ResultSet resultSet;

        int result = 0;
        try {
            PreparedStatement statement;
            statement = conn.prepareStatement(sql);

            resultSet = statement.executeQuery();
            statement.closeOnCompletion();

            while (resultSet.next()){
                result = resultSet.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return (int) (Math.ceil(result / 45.0) - 1);
    }

    public int getPagesNumFiltered(String filter){

        if (filter == null) return getPagesNum();

        StringBuilder sql = new StringBuilder("SELECT count() FROM buy_requests WHERE amount_now != amount_total and ");

        for (int i = 0; i < itemCategoriesProcessor.filterNotations.get(filter).filters.size() - 1; i++){
            sql.append("material = " + "\"").append(itemCategoriesProcessor.filterNotations.get(filter).filters.get(i)).append("\"").append(" OR ");
        }

        sql.append("material = " + "\"").append(itemCategoriesProcessor.filterNotations.get(filter).filters.get(itemCategoriesProcessor.filterNotations.get(filter).filters.size() - 1)).append("\";");

        ResultSet resultSet;

        int result = 0;
        try {
            PreparedStatement statement;
            statement = conn.prepareStatement(sql.toString());

            resultSet = statement.executeQuery();
            statement.closeOnCompletion();

            while (resultSet.next()){
                result = resultSet.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return (int) (Math.ceil(result / 45.0) - 1);
    }

    public int getPagesNum(String playerName){
        String sql = "SELECT count() FROM buy_requests WHERE owner = ?;";

        ResultSet resultSet;

        int result = 0;
        try {
            PreparedStatement statement;
            statement = conn.prepareStatement(sql);
            statement.setString(1, playerName);

            resultSet = statement.executeQuery();
            statement.closeOnCompletion();

            while (resultSet.next()){
                result = resultSet.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return (int) (Math.ceil(result / 45.0) - 1);
    }

    public void updateAmountTaken(String unique_key, int amount_taken){
        String sql = "UPDATE buy_requests SET amount_taken = amount_taken + ? where key = ?;";

        try {

            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, amount_taken);
            statement.setString(2, unique_key);
            statement.execute();
            statement.closeOnCompletion();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
