package org.intrigger.ultimate_market.utils;

public class ItemStackNotation {
    public String key;
    public String owner;
    public long price;
    public long time;
    public byte[] bytes;
    public int full;

    public ItemStackNotation(String _key, String _owner, long _price, long _time, byte[] _bytes, int _full){
        key = _key;
        owner = _owner;
        price = _price;
        time = _time;
        bytes = _bytes;
        full = _full;
    }
}
