package org.intrigger.ultimate_market.utils;

public class ItemStackNotation {
    public String key;
    public String owner;
    public double price;
    public long time;
    public byte[] bytes;
    public int full;
    public int amount;

    public ItemStackNotation(String _key, String _owner, double _price, long _time, byte[] _bytes, int _amount, int _full){
        key = _key;
        owner = _owner;
        price = _price;
        time = _time;
        bytes = _bytes;
        amount = _amount;
        full = _full;
    }

    @Override
    public String toString() {
        return "Key: " + key + "\tOwner: " + owner + "\tPrice:" + price + "\tTime: " + time + "\tFull: " + full + "\tAmount: " + amount;
    }
}
