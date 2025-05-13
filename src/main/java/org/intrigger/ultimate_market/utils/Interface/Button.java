package org.intrigger.ultimate_market.utils.Interface;

import java.util.List;

public class Button {
    public String name;
    public String title;
    public List<String> lore;

    public Button(String _name, String _title, List<String> _lore){
        name = _name;
        title = _title;
        lore = _lore;
    }
}
