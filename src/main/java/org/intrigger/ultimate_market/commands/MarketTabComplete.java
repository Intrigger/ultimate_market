package org.intrigger.ultimate_market.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MarketTabComplete implements TabCompleter {

    public static List<List<String>> list = Arrays.asList(Arrays.asList("sell", "buy", "bid"),
            Collections.emptyList(),
            Arrays.asList("full", "solo"));

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {


        int argsN = strings.length;

        String lastString = strings[argsN - 1];

        List<String> completion = new ArrayList<>();

        if (argsN > list.size()) return Collections.emptyList();
        for (String z : list.get(argsN - 1)){
            if (z.toLowerCase().startsWith(lastString.toLowerCase())){
                completion.add(z);
            }
        }

        return completion;
    }

    public List<List<String>> getTabCompleteList(){
        return list;
    }
}
