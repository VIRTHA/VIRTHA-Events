package com.darkbladedev;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.darkbladedev.commands.VirthaEventsMainCommand;
import com.darkbladedev.enchants.AcidResistance;
import com.darkbladedev.mechanics.HealthSteal;
import com.darkbladedev.tabcompleter.CommandTabcompleter;
import com.darkbladedev.utils.ColorText;

public class VirthaEventsMain extends JavaPlugin{

    public static VirthaEventsMain plugin;

    public static AcidResistance acidResistance;

    @Override
    public void onEnable() {

        plugin = this;

        acidResistance = new AcidResistance(new NamespacedKey(this, "acid_resistance"));

        registerCommands();
        registerEvents();
        registerEnchantment(acidResistance);

        Bukkit.getConsoleSender().sendMessage(
            ColorText.Colorize("&aThe plugin has been activated! ✅")
        );
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(
            ColorText.Colorize("&aThe plugin has been disabled! ✅")
        );
    }
    
    public void registerEvents() {
        PluginManager pluginManager = getServer().getPluginManager();

        try {
            pluginManager.registerEvents(new HealthSteal(), this);
            
            Bukkit.getConsoleSender().sendMessage(ColorText.Colorize("&6Events registered! 📝"));
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ColorText.Colorize("&cError registering events!"));
            e.printStackTrace();
        }
    }

    public void registerCommands() {
        try {
            if (getCommand("virtha_events") != null) {
                getCommand("virtha_events").setExecutor(new VirthaEventsMainCommand(this));
                getCommand("virtha_events").setTabCompleter(new CommandTabcompleter());
            }
            Bukkit.getConsoleSender().sendMessage(ColorText.Colorize("&6Commands registered! 📝"));
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ColorText.Colorize("&cError registering commands!"));
            e.printStackTrace();
        }
    }

    public static void registerEnchantment(Enchantment enchantment) {
        boolean registered = true;
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
            Enchantment.registerEnchantment(enchantment);
            
        } catch (Exception e) {
            registered = false;
            e.printStackTrace();
        }
        if(registered){
            // It's been registered!
        }
    }
}