package com.darkbladedev;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.darkbladedev.utils.ColorText;

public class VirthaEventsMain extends JavaPlugin{

    @Override
    public void onEnable() {
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
    
    
}