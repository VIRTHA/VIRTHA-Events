package com.darkbladedev.mechanics;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SizeRandomizer {

    private final Plugin plugin;
    private final float min;
    private final float max;
    private final float duration;

    
    public SizeRandomizer(Plugin plugin, float duration, float min, float max) {
        this.plugin = plugin;
        this.duration = duration;
        this.min = min;
        this.max = max;
    }

    public static int getRandomSize(float min, float max) {
        return (int) (Math.random() * (max - min + 1) + min);
    }

    public void start() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        float durationTicks = duration * 20; // 1 second in ticks
        
        if (players.isEmpty()) {
            return; // No players online
        }
        
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                randomizePlayerSizes(players);
            }
        };
        
        // Inicia la tarea que cambia los tamaños
        task.runTaskTimer(plugin, 0L, 20L); // Ejecuta cada 1 segundos (20 ticks)
        
        // Programa la tarea para detener el evento después de la duración especificada
        new BukkitRunnable() {
            @Override
            public void run() {
                task.cancel(); // Detiene la tarea de cambio de tamaño
                resetPlayerSizes(players); // Restablece los tamaños
            }
        }.runTaskLater(plugin, (long) durationTicks);
    }
    private void randomizePlayerSizes(List<Player> players) {
        for (Player player : players) {
            double size = getRandomSize(min, max); // Adjust the range as needed
            player.getAttribute(Attribute.SCALE).setBaseValue(size);
        }
    }

    private void resetPlayerSizes(List<Player> players) {
        for (Player player : players) {
            player.getAttribute(Attribute.SCALE).setBaseValue(1.0); // Reset to default size
        }
    }
}
