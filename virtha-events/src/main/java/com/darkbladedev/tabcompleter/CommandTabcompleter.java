package com.darkbladedev.tabcompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.darkbladedev.utils.TimeConverter;


public class CommandTabcompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("virtha_events")) {
            if (args.length == 1) {
                // Primer argumento: comandos principales
                completions.addAll(
                    Arrays.asList(
                        "run_event",
                            "health"
                    )
                );
            } else if (args[0].equalsIgnoreCase("run_event")) {
                if (args.length == 2) {
                    // Segundo argumento: tipos de eventos
                    completions.addAll(
                        Arrays.asList(
                            "size_randomizer",
                            "acid_week",
                            "toxic_fog",
                            "paranoia_effect",
                            "mob_rain",
                            "undead_week",
                            "explosive_week",
                            "blood_and_iron_week"
                        )
                    );
                } else if (args.length == 3) {
                    // Tercer argumento: duración para todos los eventos
                    completions.addAll(Arrays.asList(TimeConverter.getTimeCompletions()));
                } else if (args.length >= 4) {
                    // Argumentos adicionales específicos para cada tipo de evento
                    switch (args[1].toLowerCase()) {
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
                            }
                            break;
                        case "acid_week":
                            if (args.length == 4) {
                                completions.addAll(Arrays.asList(TimeConverter.getTimeCompletions()));
                            }
                            break;
                        case "paranoia_effect":
                            if (args.length == 4) {
                                completions.addAll(Arrays.asList(TimeConverter.getTimeCompletions()));
                            }
                            break;
                        case "lunar_gravity":
                            if (args.length == 4) {
                                completions.addAll(Arrays.asList(TimeConverter.getTimeCompletions()));
                            }
                            break;
                        case "toxic_fog":
                            if (args.length == 4) {
                                completions.addAll(Arrays.asList(TimeConverter.getTimeCompletions()));
                            }
                            break;
                        case "undead_week":
                            if (args.length == 4) {
                                completions.addAll(Arrays.asList(TimeConverter.getTimeCompletions()));
                            }
                            break;
                    }
                }
            } else if (args[0].equalsIgnoreCase("health")) {
                if (args.length == 2) {
                    completions.addAll(
                        Arrays.asList(
                            "add",
                                "remove",
                                "set",
                                "add-max",
                                "remove-max",
                                "set-max"
                        )
                    );
                } else if (args.length == 3) {
                    completions.add("amount"); 
                } else if (args.length == 4) {
                    sender.getServer().getOnlinePlayers().forEach(player -> completions.add(player.getName())); 
                }
            }
        }
        return completions;
    }
}
