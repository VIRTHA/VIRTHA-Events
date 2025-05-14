package com.darkbladedev.events;

import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.EntityTransformEvent.TransformReason;

import com.darkbladedev.VirthaEventsMain;
import com.darkbladedev.advancements.advs.init.First_death;
import com.darkbladedev.advancements.advs.init.First_kill;
import com.darkbladedev.advancements.advs.undead_week_tab.First_zombification;
import com.darkbladedev.effects.ZombieInfection;
import com.darkbladedev.utils.ColorText;
import com.darkbladedev.advancements.initTabs;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;

/**
 * Listener para manejar eventos relacionados con acciones de jugadores
 * y otorgar logros personalizados cuando se cumplen ciertas condiciones.
 */
public class PlayerActionListeners implements Listener {
    
    private final VirthaEventsMain plugin;
    private UltimateAdvancementAPI advancementAPI;
    private ZombieInfection zombieInfection;
    
    /**
     * Constructor del listener
     * @param plugin El plugin principal
     */
    public PlayerActionListeners(VirthaEventsMain plugin) {
        this.plugin = plugin;
        this.advancementAPI = initTabs.api;
        this.zombieInfection = new ZombieInfection(plugin);
        
        // Verificar si la API está disponible, si no, programar una tarea para intentar obtenerla más tarde
        if (this.advancementAPI == null) {
            plugin.getLogger().warning("La API de avances no está disponible en la inicialización. Se intentará obtener más tarde.");
            scheduleApiCheck();
        }
    }
    
    /**
     * Programa una tarea para verificar y actualizar la referencia a la API de avances
     * si inicialmente no estaba disponible
     */
    private void scheduleApiCheck() {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (initTabs.api != null && this.advancementAPI == null) {
                this.advancementAPI = initTabs.api;
                plugin.getLogger().info("Referencia a la API de avances actualizada correctamente.");
            } else if (this.advancementAPI == null) {
                plugin.getLogger().warning("La API de avances sigue sin estar disponible después del reintento.");
                // Programar otro intento en 20 ticks (1 segundo)
                scheduleApiCheck();
            }
        }, 20L); // 20 ticks = 1 segundo
    }
    
    /**
     * Maneja el evento de la primera muerte de un jugador
     * @param event El evento de muerte del jugador
     */
    @EventHandler
    public void onPlayerFirstDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // Verificar si la API de avances está disponible
        if (advancementAPI == null) {
            plugin.getLogger().warning("No se pudo otorgar el logro de primera muerte: API de avances no disponible");
            return;
        }
        
        // Otorgar el logro de primera muerte
        try {
            advancementAPI.getAdvancement(First_death.KEY).grant(player);
            plugin.getLogger().info(ColorText.Colorize("Logro de primera muerte otorgado a " + player.getName()));
        } catch (Exception e) {
            plugin.getLogger().warning("Error al otorgar el logro de primera muerte: " + e.getMessage());
        }
    }
    
    /**
     * Maneja el evento cuando un jugador mata a otro jugador
     * @param event El evento de muerte de una entidad
     */
    @EventHandler
    public void onPlayerKill(EntityDeathEvent event) {
        // Verificar si la entidad muerta es un jugador
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        // Obtener la causa de la muerte
        if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
            Entity killer = damageEvent.getDamager();
            
            // Verificar si el asesino es un jugador
            if (killer instanceof Player) {
                Player playerKiller = (Player) killer;
                
                // Verificar si la API de avances está disponible
                if (advancementAPI == null) {
                    plugin.getLogger().warning("No se pudo otorgar el logro de primera kill: API de avances no disponible");
                    return;
                }
                
                // Otorgar el logro de primera kill
                try {
                    advancementAPI.getAdvancement(First_kill.KEY).grant(playerKiller);
                    plugin.getLogger().info(ColorText.Colorize("Logro de primera kill otorgado a " + playerKiller.getName()));
                } catch (Exception e) {
                    plugin.getLogger().warning("Error al otorgar el logro de primera kill: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Maneja el evento cuando un jugador se transforma en zombie
     * @param event El evento de transformación de una entidad
     */
    @EventHandler
    public void onPlayerZombification(EntityTransformEvent event) {
        // Verificar si la transformación es de un jugador a un zombie
        if (event.getEntity() instanceof Player && event.getTransformReason() == TransformReason.INFECTION) {
            Player player = (Player) event.getEntity();

            if (player.isDead()) {
                return;
            }

            if (!zombieInfection.isInfected(player)) {
                return;
            }
            
            // Verificar si la API de avances está disponible
            if (advancementAPI == null) {
                plugin.getLogger().warning("No se pudo otorgar el logro de primera zombificación: API de avances no disponible");
                return;
            }
            
            // Otorgar el logro de primera zombificación
            try {
                advancementAPI.getAdvancement(First_zombification.KEY).grant(player);
                plugin.getLogger().info(ColorText.Colorize("Logro de primera zombificación otorgado a " + player.getName()));
            } catch (Exception e) {
                plugin.getLogger().warning("Error al otorgar el logro de primera zombificación: " + e.getMessage());
            }
        }
    }
}
