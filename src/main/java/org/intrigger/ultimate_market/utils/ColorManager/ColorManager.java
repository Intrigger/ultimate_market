package org.intrigger.ultimate_market.utils.ColorManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ColorManager {
    public ColorManager(){}
    public String format(String input){
        int size = input.length();

        Deque<ColorTag> stack = new ArrayDeque<>();

        ArrayList<ColoredSymbol> str = new ArrayList<>();

        for (int i = 0; i < size; i++){
            boolean matches = false;
            int to_add = 0;
            if (i < size - 9){
                if (input.substring(i, i + 10).matches("</#([0-9A-Fa-f]{6})>")) {
                    stack.removeLast();
                    to_add = 9;
                    matches = true;
                }
            }
            if (i < size - 8){ // start match case
                if (input.substring(i, i + 9).matches("<#([0-9A-Fa-f]{6})>")){
                    stack.add(new ColorTag("OPEN", input.substring(i + 2, i + 8), i + 9, i, i + 8));
                    to_add = 8;
                    matches = true;
                }
            }
            if (!matches){
                if (stack.isEmpty()) str.add(new ColoredSymbol(i, input.charAt(i)));
                else str.add(new ColoredSymbol(i, input.charAt(i), stack.getLast().val));
            }

            i += to_add;
        }

        StringBuilder result = new StringBuilder();

        for (ColoredSymbol c: str){
            result.append(c.getFormatted());
        }

        return result.toString();
    }
    public List<String> format(List<String> input){
        return input.stream().map(this::format).collect(Collectors.toList());
    }

    public Component parseColoredText(String input){
        Pattern tagPattern = Pattern.compile("<#([0-9a-fA-F]{6})>|</#([0-9a-fA-F]{6})>");
        Matcher matcher = tagPattern.matcher(input);

        Stack<TextColor> colorStack = new Stack<>();
        TextComponent.Builder root = Component.text();
        int lastIndex = 0;

        while (matcher.find()) {
            // Добавим текст между тегами
            if (matcher.start() > lastIndex) {
                String between = input.substring(lastIndex, matcher.start());
                TextColor currentColor = colorStack.isEmpty() ? null : colorStack.peek();
                Component segment = Component.text(between)
                        .color(currentColor)
                        .decoration(TextDecoration.ITALIC, false);
                root.append(segment);
            }

            // Обработка тега
            if (matcher.group(1) != null) {
                // Открывающий тег <#abcdef>
                String hex = matcher.group(1);
                colorStack.push(TextColor.fromHexString("#" + hex));
            } else if (matcher.group(2) != null) {
                // Закрывающий тег </#abcdef>
                if (!colorStack.isEmpty()) {
                    colorStack.pop();
                }
            }

            lastIndex = matcher.end();
        }

        // Добавим остаток текста
        if (lastIndex < input.length()) {
            String tail = input.substring(lastIndex);
            TextColor currentColor = colorStack.isEmpty() ? null : colorStack.peek();
            Component segment = Component.text(tail)
                    .color(currentColor)
                    .decoration(TextDecoration.ITALIC, false);
            root.append(segment);
        }

        return root.build();
    }

    public List<Component> parseColoredText(List<String> input){
        return input.stream().map(this::parseColoredText).collect(Collectors.toList());
    }
}
