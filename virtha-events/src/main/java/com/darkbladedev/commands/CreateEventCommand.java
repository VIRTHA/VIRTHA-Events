package com.darkbladedev.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.darkbladedev.data.EventType;
import com.darkbladedev.mechanics.MobRain;
import com.darkbladedev.utils.ColorText;

public class CreateEventCommand implements CommandExecutor {

    private final Plugin plugin;

    public CreateEventCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ColorText.Colorize("&cUsage: /virtha_events run_event <event-type> <arguments>"));
            return false;
        }
        
        String eventTypeName = args[1];
        
        EventType eventType = EventType.getByName(eventTypeName);
        
        if (eventType == null) {
            sender.sendMessage(ColorText.Colorize("&cUnknown event type: " + eventTypeName));
            //EventType.sendEventList(sender);
            return false;
        }
        

        sender.sendMessage(ColorText.Colorize("&aRunning event: " + eventType.name()));
        
        switch (eventType.getEventName()) {
            case "mob_rain":
                MobRain mobRain = new MobRain(plugin, Integer.parseInt(args[2]));
                mobRain.start();
            case "lunar_gravity":
                
            default:
                break;
        }
        
        return true;
    }
}