package com.darkbladedev.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.darkbladedev.enchants.AcidResistance;
import com.darkbladedev.utils.ColorText;

public class EnchantCommand implements CommandExecutor{

    private AcidResistance AcidResistance;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (args.length < 3) {
            sender.sendMessage(ColorText.Colorize("&cUsage: /virtha-events enchant <enchantment> <level>"));
            return false; 
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        String enchantment = args[1];
        String level = args[2];

        if (enchantment.equalsIgnoreCase("AcidResistance")) {
            try {
                item.addEnchantment(AcidResistance, Integer.parseInt(level));
                sender.sendMessage(ColorText.Colorize("&aEnchantment applied!"));
            } catch (Exception e) {
                Bukkit.getLogger().severe("Error applying enchantment: " + e.getMessage());
                sender.sendMessage(ColorText.Colorize("&cError applying enchantment!"));
            }
        }

        return false;
    }
}
