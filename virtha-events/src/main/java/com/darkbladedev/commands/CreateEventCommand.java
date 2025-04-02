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
import com.darkbladedev.mechanics.UndeadWeek;
import com.darkbladedev.utils.ColorText;
import com.darkbladedev.utils.TimeConverter;

public class CreateEventCommand implements CommandExecutor {

    private final Plugin plugin;

    public CreateEventCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length <= 2) {
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
                break;
            case "size_randomizer":
                long sizeDuration = TimeConverter.parseTimeToTicks(args[2]);
                if (sizeDuration <= 0) {
                    sender.sendMessage(ColorText.Colorize("&cDuración inválida. Ejemplo: 1h, 30m, 2d"));
                    return false;
                }
                SizeRandomizer sizeRandomizer = new SizeRandomizer(plugin, sizeDuration / 20f, Float.parseFloat(args[3]), Float.parseFloat(args[4]));
                sizeRandomizer.start();
                break;
            case "acid_week":
                long acidDuration = TimeConverter.parseTimeToTicks(args[2]);
                if (acidDuration <= 0) {
                    sender.sendMessage(ColorText.Colorize("&cDuración inválida. Ejemplo: 1h, 30m, 2d"));
                    return false;
                }
                AcidWeek acidWeek = new AcidWeek(plugin, acidDuration / 20);
                acidWeek.start();
                break;
            case "toxic_fog":
                long fogDuration = TimeConverter.parseTimeToTicks(args[2]);
                if (fogDuration <= 0) {
                    sender.sendMessage(ColorText.Colorize("&cDuración inválida. Ejemplo: 1h, 30m, 2d"));
                    return false;
                }
                ToxicFog toxicFog = new ToxicFog(plugin, fogDuration / 20);
                toxicFog.start();
                break;
            case "undead_week":
                long undeadDuration = TimeConverter.parseTimeToTicks(args[2]);
                if (undeadDuration <= 0) {
                    sender.sendMessage(ColorText.Colorize("&cDuración inválida. Ejemplo: 1h, 30m, 2d"));
                    return false;
                }
                UndeadWeek undeadWeek = new UndeadWeek(plugin, undeadDuration / 20);
                undeadWeek.start();
                break;
            default:
                break;
        }
        
        return true;
    }
}