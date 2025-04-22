package com.darkbladedev;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.darkbladedev.commands.UnbanCommand;
import com.darkbladedev.commands.VirthaEventsMainCommand;
import com.darkbladedev.commands.ZombieInfectionCommand;
import com.darkbladedev.effects.ZombieInfection;
import com.darkbladedev.enchants.AcidResistance;
import com.darkbladedev.mechanics.BanManager;
import com.darkbladedev.mechanics.HealthSteal;
import com.darkbladedev.mechanics.HealthRewards;
import com.darkbladedev.mechanics.WeeklyEventManager;
import com.darkbladedev.placeholders.VirthaEventsExpansion;
import com.darkbladedev.storage.StorageManager;
import com.darkbladedev.tabcompleter.CommandTabcompleter;
import com.darkbladedev.utils.ColorText;
import com.darkbladedev.utils.WorldIndexManager;

public class VirthaEventsMain extends JavaPlugin{

    public static VirthaEventsMain plugin;
    public static AcidResistance acidResistance;
    private WeeklyEventManager weeklyEventManager;
    private VirthaEventsExpansion placeholderExpansion;
    private StorageManager storageManager;
    private ZombieInfection zombieInfection;
    private ZombieInfectionCommand zombieInfectionCommand;

    @Override
    public void onEnable() {
        plugin = this;

        // Initialize world index manager
        WorldIndexManager.updateWorldIndices();
        
        // Initialize zombie infection system
        zombieInfection = ZombieInfection.getInstance(this);
        zombieInfectionCommand = new ZombieInfectionCommand(zombieInfection);

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
            pluginManager.registerEvents(new BanManager(), this);
            pluginManager.registerEvents(new HealthSteal(this), this);
            // Register the new HealthRewards listener
            new HealthRewards(this);
            
            // Register zombie infection listener
            pluginManager.registerEvents(zombieInfection, this);
            
            Bukkit.getConsoleSender().sendMessage(ColorText.Colorize("&6Events registered! 📝"));
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ColorText.Colorize("&cError registering events!"));
            e.printStackTrace();
        }
    }

    private void registerCommands() {
        try {
            getCommand("virtha_events").setExecutor(new VirthaEventsMainCommand(this));
            getCommand("virtha_events").setTabCompleter(new CommandTabcompleter(this));
            
            Bukkit.getConsoleSender().sendMessage(ColorText.Colorize("&6Commands registered! 📝"));
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ColorText.Colorize("&cError registering commands!"));
            e.printStackTrace();
        }
        
        // Register the unban command
        File banDataFile = new File(getDataFolder(), "ban_data.json");
        getCommand("vunban").setExecutor(new UnbanCommand(banDataFile));
        getCommand("vunban").setTabCompleter(new UnbanCommand(banDataFile));
    }
    
    public WeeklyEventManager getWeeklyEventManager() {
        return weeklyEventManager;
    }
    
    public ZombieInfectionCommand getZombieInfectionCommand() {
        return zombieInfectionCommand;
    }
}