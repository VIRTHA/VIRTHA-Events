package com.darkbladedev;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.darkbladedev.commands.VirthaEventsMainCommand;
import com.darkbladedev.enchants.AcidResistance;
import com.darkbladedev.mechanics.HealthSteal;
import com.darkbladedev.mechanics.UndeadWeek;
import com.darkbladedev.mechanics.WeeklyEventManager;
import com.darkbladedev.tabcompleter.CommandTabcompleter;
import com.darkbladedev.utils.ColorText;

public class VirthaEventsMain extends JavaPlugin{

    public static VirthaEventsMain plugin;
    public static AcidResistance acidResistance;
    private WeeklyEventManager weeklyEventManager;

    @Override
    public void onEnable() {
        plugin = this;

        registerCommands();
        registerEvents();
        
        // Inicializar y arrancar el sistema de eventos semanales
        weeklyEventManager = new WeeklyEventManager(this);
        weeklyEventManager.initialize();

        Bukkit.getConsoleSender().sendMessage(
            ColorText.Colorize("&aThe plugin has been activated! ✅")
        );
    }

    @Override
    public void onDisable() {
        // Guardar el estado del evento semanal al apagar
        if (weeklyEventManager != null) {
            weeklyEventManager.shutdown();
        }
        
        Bukkit.getConsoleSender().sendMessage(
            ColorText.Colorize("&aThe plugin has been disabled! ✅")
        );
    }
    
    public void registerEvents() {
        PluginManager pluginManager = getServer().getPluginManager();

        try {
            pluginManager.registerEvents(new HealthSteal(), this);
            pluginManager.registerEvents(new UndeadWeek(this, 0), this); // Registrar el listener
            
            Bukkit.getConsoleSender().sendMessage(ColorText.Colorize("&6Events registered! 📝"));
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ColorText.Colorize("&cError registering events!"));
            e.printStackTrace();
        }
    }

    public void registerCommands() {
        try {
            getCommand("virtha_events").setExecutor(new VirthaEventsMainCommand(this));
            getCommand("virtha_events").setTabCompleter(new CommandTabcompleter());
            
            Bukkit.getConsoleSender().sendMessage(ColorText.Colorize("&6Commands registered! 📝"));
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ColorText.Colorize("&cError registering commands!"));
            e.printStackTrace();
        }
    }
    
    public WeeklyEventManager getWeeklyEventManager() {
        return weeklyEventManager;
    }
}