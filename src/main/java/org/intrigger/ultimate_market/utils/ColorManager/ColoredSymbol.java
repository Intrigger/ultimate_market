package org.intrigger.ultimate_market.utils.ColorManager;

public class ColoredSymbol {
    int index;
    Character symbol;
    String color;
    public ColoredSymbol(int _index, Character _symbol, String _color){
        index = _index;
        symbol = _symbol;
        color = _color;
    }
    public ColoredSymbol(int _index, Character _symbol){
        index = _index;
        symbol = _symbol;
        color = "FFFFFF";
    }

    @Override
    public String toString() {
        return "Index: " + index + "\tSymbol: " + symbol + "\tColor: " + color;
    }

    public String getFormatted(){
        StringBuilder result = new StringBuilder();
        result.append("§x");
        for (int i = 0; i < 6; i++){
            result.append("§").append(color.charAt(i));
        }
        result.append(symbol);
        return result.toString();
    }
}
