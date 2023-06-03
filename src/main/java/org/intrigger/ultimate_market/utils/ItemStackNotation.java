package org.intrigger.ultimate_market.utils;

public class ItemStackNotation {
    public String owner;
    public long price;
    public long time;
    public byte[] bytes;

    public ItemStackNotation(String key, String _owner, long _price, long _time, byte[] _bytes){
        owner = _owner;
        price = _price;
        time = _time;
        bytes = _bytes;
    }
}
