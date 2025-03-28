package com.darkbladedev.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.darkbladedev.data.EventType;
import com.darkbladedev.mechanics.AcidWeek;
import com.darkbladedev.mechanics.MobRain;
import com.darkbladedev.mechanics.SizeRandomizer;
import com.darkbladedev.mechanics.ToxicFog;
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
            case "size_randomizer":
                SizeRandomizer sizeRandomizer = new SizeRandomizer(plugin, Float.parseFloat(args[2]), Float.parseFloat(args[3]), Float.parseFloat(args[4]));
                sizeRandomizer.start();
            case "acid_week":
                AcidWeek acidWeek = new AcidWeek(plugin, Long.parseLong(args[2]));
                acidWeek.start();
            case "toxic_fog":
                ToxicFog toxicFog = new ToxicFog(plugin, Long.parseLong(args[2]));
                toxicFog.start();
            default:
                break;
        }
        
        return true;
    }
}