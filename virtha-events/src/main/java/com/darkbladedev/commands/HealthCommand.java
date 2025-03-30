package com.darkbladedev.commands;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.darkbladedev.utils.ColorText;

public class HealthCommand implements CommandExecutor{

    public HealthCommand() {}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length < 4) {
            sender.sendMessage(ColorText.Colorize("&cUsage: /ve health <add/remove> <amount> <player>"));
            return false;
        }
    
        if (!sender.hasPermission("virthaevents.command.health")) {
            sender.sendMessage(ColorText.Colorize("&cYou do not have permission to use this command"));
            return false;
        }
        
        
        String cmd = args[0];
        String actionType = args[1];
        Double health = Double.parseDouble(args[2]);
        Player targetPlayer = Bukkit.getPlayer(args[3]);
        if (cmd.equalsIgnoreCase("health")) {
           switch (actionType) {

            // Health
            case "add":
                try {
                    targetPlayer.setHealth(targetPlayer.getHealth() + health);
                } catch (Exception e) {
                    sender.sendMessage(ColorText.Colorize("&cError: &6" + e.getMessage()));
                }
                break;
            case "remove":
                try { 
                        targetPlayer.setHealth(targetPlayer.getHealth() - health);
                    } catch (Exception e) {
                        sender.sendMessage(ColorText.Colorize("&cError: &6" + e.getMessage()));
                    }
                break;
            case "set":
                try {
                    targetPlayer.setHealth(health);
                } catch (Exception e) {
                    sender.sendMessage(ColorText.Colorize("&cError: &6" + e.getMessage()));
                }
                break;

            // Max Health
            case "add-max":
                try {
                    targetPlayer.getAttribute(Attribute.MAX_HEALTH).setBaseValue(targetPlayer.getHealth() + health);
                } catch (Exception e) {
                    sender.sendMessage(ColorText.Colorize("&cError: &6" + e.getMessage()));
                }
                break;
            case "remove-max":
                try { 
                    targetPlayer.getAttribute(Attribute.MAX_HEALTH).setBaseValue(targetPlayer.getHealth() - health);
                    } catch (Exception e) {
                        sender.sendMessage(ColorText.Colorize("&cError: &6" + e.getMessage()));
                    }
                break;
            case "set-max":
                try {
                    targetPlayer.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
                } catch (Exception e) {
                    sender.sendMessage(ColorText.Colorize("&cError: &6" + e.getMessage()));
                }
                break;
           
            default:
                break;
           }
            return true;
        }
        return false;
    }

}
