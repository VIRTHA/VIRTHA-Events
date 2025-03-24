package com.darkbladedev.tabcompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.darkbladedev.data.EventType;


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
                    // Instead of using EventType.getEventNames() which returns an immutable collection
                    // Create a new ArrayList and add each event name individually
                    for (EventType type : EventType.values()) {
                        completions.add(type.getEventName());
                    }
                } else if (args.length > 3) {
                    // Add the arguments for each event type
                    switch (args[1]) {
                        case "mob_rain":
                            if (args.length == 4) {
                                completions.add("entity-count");
                            }
                            break;
                        case "size_randomizer":
                            if (args.length == 4) {
                                completions.add("min_size");
                            } else if (args.length == 5) {
                                completions.add("max_size");
                            } else if (args.length == 6) {
                                completions.add("duration");
                            }
                            break;
                        case "lunar_gravity":
                            if (args.length == 4) {
                                completions.add("duration");
                            }
                            break;
                        case "toxic_fog":
                            if (args.length == 4) {
                                completions.add("duration");
                            }
                        default:
                            break;
                    }
                }
            }
        }
        return completions;
    }
}
