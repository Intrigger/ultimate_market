package org.intrigger.ultimate_market.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;

public class MarketTabComplete implements TabCompleter {

    public static List<List<String>> list = Arrays.asList(Arrays.asList("sell", "buy"),
            Collections.emptyList(),
            Collections.singletonList("full"));
    public static Map<String, List<List<String>>> tab_complete;


    public MarketTabComplete(){
        tab_complete = new HashMap<>();

        tab_complete.put("ah", Arrays.asList(Arrays.asList("buy", "sell")));
        tab_complete.put("buy", Arrays.asList(
                Arrays.asList("количество"),
                Arrays.asList("цена"),
                Arrays.asList("")
        ));
        tab_complete.put("sell", Arrays.asList(
                                                Arrays.asList("цена"),
                                                Arrays.asList("full"),
                                                Arrays.asList("")
                ));

    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {


//        int argsN = strings.length;
//
//        String lastString = strings[argsN - 1];
//
//        List<String> completion = new ArrayList<>();
//
//        if (argsN > list.size()) return Collections.emptyList();
//        for (String z : list.get(argsN - 1)){
//            if (z.toLowerCase().startsWith(lastString.toLowerCase())){
//                completion.add(z);
//            }
//        }

        String key = "ah";

        int index = 0;

        for (int i = 0; i < strings.length; i++){
            if (!strings[i].equalsIgnoreCase("")) {
                if (tab_complete.containsKey(strings[i])) {
                    key = strings[i];
                    index = i + 1;
                }
            }
        }

        int args_counter = 0;

        for (int i = index; i < strings.length; i++){
            if (!strings[i].equalsIgnoreCase("")) {
                args_counter++;
            }
        }

        List<String> completion = new ArrayList<>(Collections.emptyList());

        //System.out.println("key: " + key + "\targs: " + args_counter);

        if (strings[strings.length - 1].equalsIgnoreCase("")){
            completion = tab_complete.getOrDefault(key, Collections.emptyList()).get(Math.min(tab_complete.getOrDefault(key, Collections.emptyList()).size() - 1, args_counter));
        }
        else{
            for (String i: tab_complete.getOrDefault(key, Collections.emptyList()).get(Math.min(tab_complete.getOrDefault(key, Collections.emptyList()).size() - 1, args_counter))){
                if (i.startsWith(strings[strings.length - 1])){
                    completion.add(i);
                }
            }
        }

        return completion;
        //return tab_complete.getOrDefault(key, Collections.emptyList()).get(Math.min(tab_complete.getOrDefault(key, Collections.emptyList()).size() - 1, args_counter));
    }
}
