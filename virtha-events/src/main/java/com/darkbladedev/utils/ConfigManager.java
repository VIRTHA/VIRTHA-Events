package com.darkbladedev.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * Clase para gestionar la configuración del plugin
 */
public class ConfigManager {
    
    private static Plugin plugin;
    private static FileConfiguration config;
    
    /**
     * Inicializa el ConfigManager con el plugin principal
     * @param mainPlugin El plugin principal
     */
    public static void initialize(Plugin mainPlugin) {
        plugin = mainPlugin;
        reloadConfig();
    }
    
    /**
     * Recarga la configuración desde el archivo
     */
    public static void reloadConfig() {
        if (plugin != null) {
            plugin.reloadConfig();
            config = plugin.getConfig();
        }
    }
    
    /**
     * Verifica si el modo debug está activado
     * @return true si el modo debug está activado, false en caso contrario
     */
    public static boolean isDebugEnabled() {
        return config != null && config.getBoolean("debug", false);
    }
    
    /**
     * Verifica si el verificador de actualizaciones está activado
     * @return true si el verificador de actualizaciones está activado, false en caso contrario
     */
    public static boolean isUpdateCheckerEnabled() {
        return config != null && config.getBoolean("update-checker", true);
    }
    
    /**
     * Verifica si el notificador de actualizaciones está activado
     * @return true si el notificador de actualizaciones está activado, false en caso contrario
     */
    public static boolean isUpdateNotifierEnabled() {
        return config != null && config.getBoolean("update-notifier", true);
    }
    
    /**
     * Obtiene el mensaje de notificación de actualización
     * @return El mensaje de notificación de actualización
     */
    public static String getUpdateNotifierMessage() {
        return config != null ? config.getString("update-notifier-message", "&2A new update is available! &5{latest}&2. &5{link}") : "";
    }
    
    /**
     * Obtiene el prefijo del plugin
     * @return El prefijo del plugin
     */
    public static String getPrefix() {
        return config != null ? config.getString("prefix", "&2[ &5VIRTHA &3Events &2]") : "&2[ &5VIRTHA &3Events &2]";
    }
}