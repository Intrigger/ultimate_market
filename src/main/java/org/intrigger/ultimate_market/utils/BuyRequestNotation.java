package org.intrigger.ultimate_market.utils;

public class BuyRequestNotation {
    public String key;
    public long time;
    public String owner;
    public double price;
    public String material;
    public byte[] bytes;
    public int amount_now;
    public int amount_taken;
    public int amount_total;

    public BuyRequestNotation(String _key, long _time, String _owner, double _price, String _material, byte[] _bytes, int _amount_now, int _amount_taken, int _amount_total){
        key = _key;
        time = _time;
        owner = _owner;
        price = _price;
        material = _material;
        bytes = _bytes;
        amount_now = _amount_now;
        amount_taken = _amount_taken;
        amount_total = _amount_total;
    }
}
