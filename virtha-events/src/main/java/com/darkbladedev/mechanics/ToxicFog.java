package com.darkbladedev.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        
        toxicFogTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Verificar si el jugador está en agua o bajo techo
                    if (!isPlayerSafe(player)) {
                        // Aplicar los efectos
                        player.addPotionEffects(getPotionEffects());
                        
                        affectedPlayers.add(player.getUniqueId());
                    } else {
                        // Remover los efectos si el jugador está en un entorno seguro
                        player.removePotionEffect(PotionEffectType.POISON);
                        player.removePotionEffect(PotionEffectType.DARKNESS);

                        affectedPlayers.remove(player.getUniqueId());
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Verificar cada 1 segundo (20 ticks)

        new BukkitRunnable() {
            @Override
            public void run() {
                stop();
            }
        }.runTaskLater(plugin, duration * 20L);
    }

    public void stop() {
        if (!isActive) return;

        Long durationSeconds = duration * 20;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (toxicFogTask != null) {
                    toxicFogTask.cancel();
                    toxicFogTask = null;
                    
                    // Eliminar todos los efectos de los jugadores afectados
                    for (UUID uuid : affectedPlayers) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null && player.isOnline()) {
                            player.removePotionEffect(PotionEffectType.POISON);
                            player.removePotionEffect(PotionEffectType.DARKNESS);
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

    private List<PotionEffect> getPotionEffects() {
        List<PotionEffect> potionEffects = new ArrayList<>();
        // Usar una duración muy larga (30 minutos = 36000 ticks) para que sea efectivamente permanente
        potionEffects.add(new PotionEffect(PotionEffectType.POISON, 36000, 2, false, true, true));
        // Hacer el efecto de oscuridad permanente durante el evento
        potionEffects.add(new PotionEffect(PotionEffectType.DARKNESS, 36000, 2, true, false, true));
        return potionEffects;
    }
}
