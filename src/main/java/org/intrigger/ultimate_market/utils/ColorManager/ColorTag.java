package org.intrigger.ultimate_market.utils.ColorManager;

public class ColorTag {
    public String type; //"OPEN" or "CLOSE"
    public String val; //e.g. ffffff
    public int index;
    public int left;
    public int right;

    public ColorTag(String _type, String _val, int _index, int _left, int _right){
        type = _type;
        val = _val;
        index = _index;
        left = _left;
        right = _right;
    }

    @Override
    public String toString() {
        return "Type: " + type + " Val: " + val + " Index: " + index + " [" + left + ";" + right + "]";
    }
}
