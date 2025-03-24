package com.darkbladedev.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ToxicFog {
    
    private final Plugin plugin;
    private final Set<UUID> affectedPlayers;
    private BukkitTask toxicFogTask;
    private boolean isActive = false;

    private final long duration;
    
    public ToxicFog(Plugin plugin, long duration) {
        this.plugin = plugin;
        this.duration = duration;
        this.affectedPlayers = new HashSet<>();
    }
    
    
    public void start() {
        if (isActive) return;
        
        isActive = true;
        Long durationSeconds = duration * 20;
        
        toxicFogTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Verificar si el jugador está en agua o bajo techo
                    if (!isPlayerSafe(player)) {
                        // Aplicar efecto de veneno
                        player.addPotionEffect(new PotionEffect(
                                PotionEffectType.POISON, 
                                100, // Duración de 5 segundos (100 ticks)
                                0,   // Nivel 1 de veneno
                                false, 
                                true, 
                                true));
                        
                        affectedPlayers.add(player.getUniqueId());
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 100L); // Verificar cada 5 segundos (100 ticks)

        new BukkitRunnable() {
            @Override
            public void run() {
                if (toxicFogTask != null) {
                    toxicFogTask.cancel();
                    toxicFogTask = null;
                    
                    // Eliminar efectos de veneno de todos los jugadores afectados
                    for (UUID uuid : affectedPlayers) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null && player.isOnline()) {
                            player.removePotionEffect(PotionEffectType.POISON);
                        }
                    }
                    
                    affectedPlayers.clear();
                    isActive = false;
                }
            }
        }.runTaskLater(plugin, durationSeconds);
    }

    
    private boolean isPlayerSafe(Player player) {
        // Verificar si el jugador está en agua
        Material blockType = player.getLocation().getBlock().getType();
        if (blockType == Material.WATER) {
            return true;
        }
        
        // Verificar si el jugador está bajo techo (no tiene acceso directo al cielo)
        if (player.getLocation().getBlock().getLightFromSky() > 0) {
            return false;
        }
        return true;
    }
    
    public boolean isActive() {
        return isActive;
    }
}
