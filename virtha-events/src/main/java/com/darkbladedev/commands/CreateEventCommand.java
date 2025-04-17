package com.darkbladedev.commands;

import java.util.concurrent.TimeUnit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.darkbladedev.VirthaEventsMain;
import com.darkbladedev.data.EventType;
import com.darkbladedev.mechanics.AcidWeek;
import com.darkbladedev.mechanics.BloodAndIronWeek;
import com.darkbladedev.mechanics.ExplosiveWeek;
import com.darkbladedev.mechanics.MobRain;
import com.darkbladedev.mechanics.ParanoiaEffect;
import com.darkbladedev.mechanics.SizeRandomizer;
import com.darkbladedev.mechanics.ToxicFog;
import com.darkbladedev.mechanics.UndeadWeek;
import com.darkbladedev.mechanics.WeeklyEventManager;
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
            sender.sendMessage(ColorText.Colorize("&cTipo de evento desconocido: " + eventTypeName));
            return false;
        }
        
        // Get the WeeklyEventManager instance
        WeeklyEventManager eventManager = ((VirthaEventsMain) plugin).getWeeklyEventManager();
        
        // Check if this is a weekly event
        if (eventTypeName.contains("week") || eventTypeName.equals("toxic_fog")) {
            // For weekly events, use the WeeklyEventManager
            long duration = TimeUnit.DAYS.toMillis(7); // Default to 7 days
            
            // If there's a duration argument, parse it
            if (args.length > 2) {
                try {
                    int days = Integer.parseInt(args[2]);
                    duration = TimeUnit.DAYS.toMillis(days);
                } catch (NumberFormatException e) {
                    // Ignore and use default
                }
            }
            
            // Start the event using the WeeklyEventManager
            boolean started = eventManager.startEventFromCommand(eventType, duration);
            
            if (started) {
                sender.sendMessage(ColorText.Colorize("&aEvento semanal iniciado: " + eventTypeName));
            }
            
            return true;
        }
        
        // For non-weekly events, continue with the existing implementation
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
                
                float minSize = 0.1f;
                float maxSize = 2.0f;
                
                if (args.length >= 4) {
                    try {
                        minSize = Float.parseFloat(args[3]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ColorText.Colorize("&cTamaño mínimo inválido. Debe ser un número."));
                        return false;
                    }
                }
                
                if (args.length >= 5) {
                    try {
                        maxSize = Float.parseFloat(args[4]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ColorText.Colorize("&cTamaño máximo inválido. Debe ser un número."));
                        return false;
                    }
                }
                
                SizeRandomizer sizeRandomizer = new SizeRandomizer(plugin, sizeDuration, minSize, maxSize);
                sizeRandomizer.start();
                break;
            case "acid_week":
                long acidDuration = TimeConverter.parseTimeToTicks(args[2]);
                if (acidDuration <= 0) {
                    sender.sendMessage(ColorText.Colorize("&cDuración inválida. Ejemplo: 1h, 30m, 2d"));
                    return false;
                }
                
                AcidWeek acidWeek = new AcidWeek(plugin, acidDuration);
                acidWeek.start();
                break;
            case "toxic_fog":
                long fogDuration = TimeConverter.parseTimeToTicks(args[2]);
                if (fogDuration <= 0) {
                    sender.sendMessage(ColorText.Colorize("&cDuración inválida. Ejemplo: 1h, 30m, 2d"));
                    return false;
                }
                
                ToxicFog toxicFog = new ToxicFog(plugin, fogDuration);
                toxicFog.start();
                break;
            case "paranoia_effect":
                long paranoiaDuration = TimeConverter.parseTimeToTicks(args[2]);
                if (paranoiaDuration <= 0) {
                    sender.sendMessage(ColorText.Colorize("&cDuración inválida. Ejemplo: 1h, 30m, 2d"));
                    return false;
                }
                
                long interval = 1; // Default interval
                if (args.length >= 4) {
                    try {
                        interval = Long.parseLong(args[3]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ColorText.Colorize("&cIntervalo inválido. Debe ser un número."));
                        return false;
                    }
                }
                
                ParanoiaEffect paranoiaEffect = new ParanoiaEffect(plugin, paranoiaDuration, interval);
                paranoiaEffect.start();
                break;
            case "undead_week":
                long undeadDuration = TimeConverter.parseTimeToTicks(args[2]);
                if (undeadDuration <= 0) {
                    sender.sendMessage(ColorText.Colorize("&cDuración inválida. Ejemplo: 1h, 30m, 2d"));
                    return false;
                }
                
                UndeadWeek undeadWeek = new UndeadWeek(plugin, undeadDuration);
                undeadWeek.start();
                break;
            case "explosive_week":
                long explosiveDuration = TimeConverter.parseTimeToTicks(args[2]);
                if (explosiveDuration <= 0) {
                    sender.sendMessage(ColorText.Colorize("&cDuración inválida. Ejemplo: 1h, 30m, 2d"));
                    return false;
                }
                
                ExplosiveWeek explosiveWeek = new ExplosiveWeek(plugin, explosiveDuration);
                explosiveWeek.start();
                break;
            case "blood_and_iron_week":
                long bloodDuration = TimeConverter.parseTimeToTicks(args[2]);
                if (bloodDuration <= 0) {
                    sender.sendMessage(ColorText.Colorize("&cDuración inválida. Ejemplo: 1h, 30m, 2d"));
                    return false;
                }
                
                BloodAndIronWeek bloodAndIronWeek = new BloodAndIronWeek(plugin, bloodDuration);
                bloodAndIronWeek.start();
                break;
            default:
                break;
        }
        
        return true;
    }
}