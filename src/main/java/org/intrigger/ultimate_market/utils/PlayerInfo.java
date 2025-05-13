package org.intrigger.ultimate_market.utils;

import java.util.HashMap;
import java.util.Map;

public class PlayerInfo {
    public Map<String, String> current_menu;
    public Map<String, String> current_item_filter;
    public Map<String, String> current_buy_requests_item_filter;
    public Map<String, String> current_sorting_type;
    public Map<String, Integer> current_market_page;
    public Map<String, Integer> current_my_shop_page;
    public Map<String, Integer> current_my_buy_requests_page;
    public Map<String, String> current_buying_item;
    public Map<String, Integer> current_buying_item_amount;
    public Map<String, Integer> current_buy_requests_page;
    public Map<String, String> current_buy_requests_sorting_type;
    public PlayerInfo(){
        current_menu = new HashMap<>();
        current_item_filter = new HashMap<>();
        current_sorting_type = new HashMap<>();
        current_market_page = new HashMap<>();
        current_buying_item = new HashMap<>();
        current_buying_item_amount = new HashMap<>();
        current_buy_requests_page = new HashMap<>();
        current_buy_requests_sorting_type = new HashMap<>();
        current_buy_requests_item_filter = new HashMap<>();
        current_my_shop_page = new HashMap<>();
        current_my_buy_requests_page = new HashMap<>();
    }
}
