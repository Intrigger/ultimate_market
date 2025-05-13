package org.intrigger.ultimate_market.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StringDeserializer {
    public static TextComponent deserialize(String str){
        LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacy(LegacyComponentSerializer.SECTION_CHAR);
        return LEGACY.deserialize(str).decoration(TextDecoration.ITALIC, false);
    }
    public static List<Component> deserialize(List<String> lore){
        LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacy(LegacyComponentSerializer.SECTION_CHAR);
        List<Component> res = lore.stream().map(LEGACY::deserialize).collect(Collectors.toList());
        ArrayList<Component> result = new ArrayList<>();
        for (Component c: res){
            result.add(c.decoration(TextDecoration.ITALIC, false));
        }
        return result;
    }
}
