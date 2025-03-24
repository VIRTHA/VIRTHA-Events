package com.darkbladedev;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.darkbladedev.commands.VirthaEventsMainCommand;
import com.darkbladedev.mechanics.HealthSteal;
import com.darkbladedev.tabcompleter.CommandTabcompleter;
import com.darkbladedev.utils.ColorText;

public class VirthaEventsMain extends JavaPlugin{

    @Override
    public void onEnable() {

        registerCommands();
        registerEvents();

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
}