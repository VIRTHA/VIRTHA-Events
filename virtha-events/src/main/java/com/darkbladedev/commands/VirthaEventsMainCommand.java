package com.darkbladedev.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.darkbladedev.VirthaEventsMain;
import com.darkbladedev.utils.ColorText;

public class VirthaEventsMainCommand implements CommandExecutor{

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
            default:
                sender.sendMessage(ColorText.Colorize("&cInvalid subcommand. Use /virthaevents <run_event|health|event_control>"));
                return false;
        
        }
    }
}
