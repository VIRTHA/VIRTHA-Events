package com.darkbladedev.commands;

import java.util.Arrays;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.darkbladedev.VirthaEventsMain;
import com.darkbladedev.utils.ColorText;

public class VirthaEventsMainCommand implements CommandExecutor {

    private final Plugin plugin;

    public VirthaEventsMainCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "run_event":
                return new CreateEventCommand(plugin).onCommand(sender, command, label, args);
            case "health":
                return new HealthCommand().onCommand(sender, command, label, args);
            case "event_control":
                return new EventControlCommand((VirthaEventsMain) plugin).onCommand(sender, command, label, args);
            // Inside the onCommand method, in the "effects" case section:
            case "effects":
                if (args.length < 2) {
                    sender.sendMessage(ColorText.Colorize("&cUso: /ve effects <tipo_efecto> [argumentos]"));
                    return true;
                }
                
                String effectType = args[1].toLowerCase();
                
                switch (effectType) {
                    case "zombie_infection":
                    case "zombieinfection":
                    case "zombie":
                        if (args.length < 3) {
                            sender.sendMessage(ColorText.Colorize("&cUso: /ve effects zombie_infection <infectar|curar|estado|toggle> [jugador]"));
                            return true;
                        }
                        
                        // Remove the first two arguments (ve, effects) and pass the rest to the zombie infection command
                        String[] zombieArgs = Arrays.copyOfRange(args, 2, args.length);
                        return ((VirthaEventsMain) plugin).getZombieInfectionCommand().execute(sender, zombieArgs);
                    
                    default:
                        sender.sendMessage(ColorText.Colorize("&cTipo de efecto desconocido. Usa: /ve effects <tipo_efecto>"));
                        return true;
                }
            default:
                sender.sendMessage(ColorText.Colorize("&cInvalid subcommand. Use /virthaevents <run_event|health|event_control>"));
                return false;
        
        }
    }
}
