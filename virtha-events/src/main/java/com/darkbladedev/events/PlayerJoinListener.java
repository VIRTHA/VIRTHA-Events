package com.darkbladedev.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.darkbladedev.VirthaEventsMain;
import com.darkbladedev.utils.ConfigManager;

/**
 * Listener para eventos de conexión de jugadores
 */
public class PlayerJoinListener implements Listener {
    
    private final VirthaEventsMain plugin;
    
    /**
     * Constructor del listener
     * @param plugin El plugin principal
     */
    public PlayerJoinListener(VirthaEventsMain plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Maneja el evento de conexión de un jugador
     * @param event El evento de conexión
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Notificar sobre actualizaciones disponibles a los administradores
        if (player.hasPermission("virthaevents.admin.update") && 
            ConfigManager.isUpdateNotifierEnabled() && 
            plugin.getUpdateChecker().isUpdateAvailable()) {
            
            // Programar la notificación para 2 segundos después de la conexión
            // para evitar que se mezcle con otros mensajes de conexión
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.notifyUpdateToPlayer(player);
            }, 40L); // 40 ticks = 2 segundos
        }
        
        // Si el modo debug está activado, mostrar información adicional a los administradores
        if (player.hasPermission("virthaevents.admin") && ConfigManager.isDebugEnabled()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                player.sendMessage("§6[VIRTHA Events DEBUG] §aEl modo debug está activado. Los comandos de depuración están disponibles.");
            }, 60L); // 60 ticks = 3 segundos
        }
    }
}