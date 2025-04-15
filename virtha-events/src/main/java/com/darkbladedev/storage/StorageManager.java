package com.darkbladedev.storage;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class StorageManager {

    private final File file;
    private static FileConfiguration config;
        
        public StorageManager(File dataFolder) {
            
            this.file = new File(dataFolder, "config.yml");
    
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            config = YamlConfiguration.loadConfiguration(file);
    }

    public static String getPrefix() {
        return config.getString("prefix");
    }
}
