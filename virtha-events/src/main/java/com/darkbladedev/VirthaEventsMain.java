package com.darkbladedev;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.darkbladedev.commands.VirthaEventsMainCommand;
import com.darkbladedev.enchants.AcidResistance;
import com.darkbladedev.mechanics.HealthSteal;
import com.darkbladedev.mechanics.WeeklyEventManager;
import com.darkbladedev.placeholders.VirthaEventsExpansion;
import com.darkbladedev.storage.StorageManager;
import com.darkbladedev.tabcompleter.CommandTabcompleter;
import com.darkbladedev.utils.ColorText;

public class VirthaEventsMain extends JavaPlugin{

    public static VirthaEventsMain plugin;
    public static AcidResistance acidResistance;
    private WeeklyEventManager weeklyEventManager;
    private VirthaEventsExpansion placeholderExpansion;
    @SuppressWarnings("unused")
    private StorageManager storageManager;

    @Override
    public void onEnable() {
        plugin = this;

        registerCommands();
        registerEvents();
        
        
        // Inicializar y arrancar el sistema de eventos semanales
        weeklyEventManager = new WeeklyEventManager(this);
        weeklyEventManager.initialize();

        storageManager = new StorageManager(this.getDataFolder());
        
        // Registrar la expansión de PlaceholderAPI si está disponible
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderExpansion = new VirthaEventsExpansion(this);
            placeholderExpansion.register();
            Bukkit.getConsoleSender().sendMessage(
                ColorText.Colorize("&6PlaceholderAPI integration enabled! 📊")
            );
        } else {
            Bukkit.getConsoleSender().sendMessage(
                ColorText.Colorize("&cPlaceholderAPI not found, placeholders will not be available.")
            );
        }

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
        
        // Desregistrar la expansión de PlaceholderAPI si está activa
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
        }
        
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