package com.darkbladedev.tabcompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;


public class CommandTabcompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("virtha_events")) {
            if (args.length == 1) {
                completions.addAll(
                    Arrays.asList(
                        "run_event"
                    )
                );
            } else if (args[0].equalsIgnoreCase("run_event")) {
                if (args.length == 2) {
                    completions.addAll(
                        Arrays.asList(
                            "mob_rain"
                        )
                    );
                }
            }
        }
        return completions;
    }
}
