package com.darkbladedev.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.darkbladedev.VirthaEventsMain;
import com.darkbladedev.mechanics.WeeklyEventManager;
import com.darkbladedev.utils.ColorText;

public class EventControlCommand implements CommandExecutor {

    private final VirthaEventsMain plugin;

    public EventControlCommand(VirthaEventsMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorText.Colorize("&cUsage: /virtha_events event_control <pause|resume|stop>"));
            return false;
        }
        
        String action = args[1].toLowerCase();
        WeeklyEventManager eventManager = plugin.getWeeklyEventManager();
        
        if (!eventManager.isEventActive()) {
            sender.sendMessage(ColorText.Colorize("&cNo hay ningún evento semanal activo en este momento."));
            return false;
        }
        
        switch (action) {
            case "pause":
                if (eventManager.isPaused()) {
                    sender.sendMessage(ColorText.Colorize("&cEl evento ya está pausado."));
                    return false;
                }
                
                eventManager.pauseCurrentEvent();
                sender.sendMessage(ColorText.Colorize("&aEvento pausado correctamente."));
                break;
                
            case "resume":
                if (!eventManager.isPaused()) {
                    sender.sendMessage(ColorText.Colorize("&cEl evento no está pausado."));
                    return false;
                }
                
                eventManager.resumeCurrentEvent();
                sender.sendMessage(ColorText.Colorize("&aEvento reanudado correctamente."));
                break;
                
            case "stop":
                eventManager.forceStopCurrentEvent();
                sender.sendMessage(ColorText.Colorize("&aEvento detenido forzosamente."));
                break;
                
            default:
                sender.sendMessage(ColorText.Colorize("&cAcción desconocida. Use: pause, resume o stop."));
                return false;
        }
        
        return true;
    }
}