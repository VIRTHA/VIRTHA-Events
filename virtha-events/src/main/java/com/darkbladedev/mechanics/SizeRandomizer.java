package com.darkbladedev.mechanics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SizeRandomizer implements Listener {

    private final Plugin plugin;
    private final float min;
    private final float max;
    private final float duration;
    private BukkitRunnable endTask;
    private boolean isActive = false;
    private final Map<UUID, Double> playerSizes = new HashMap<>();
    
    public SizeRandomizer(Plugin plugin, float duration, float min, float max) {
        this.plugin = plugin;
        this.duration = duration;
        this.min = min;
        this.max = max;
    }

    public static float getRandomSize(float min, float max) {
        return (float) (Math.random() * (max - min) + min);
    }

    public void start() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        float durationTicks = duration * 20; // Convert seconds to ticks
        
        isActive = true;
        
        // Register events to handle new players joining during the event
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Randomize sizes once at the start
        randomizePlayerSizes(players);
        
        // Schedule the task to end the event after the specified duration
        endTask = new BukkitRunnable() {
            @Override
            public void run() {
                stop();
            }
        };
        endTask.runTaskLater(plugin, (long) durationTicks);
    }
    
    public void stop() {
        if (!isActive) return;
        
        isActive = false;
        
        // Reset all player sizes
        for (Player player : Bukkit.getOnlinePlayers()) {
            resetPlayerSize(player);
        }
        
        // Clear the stored sizes
        playerSizes.clear();
        
        // Cancel the end task if it's still running
        if (endTask != null && !endTask.isCancelled()) {
            endTask.cancel();
        }
    }
    
    private void randomizePlayerSizes(List<Player> players) {
        for (Player player : players) {
            float size = getRandomSize(min, max);
            playerSizes.put(player.getUniqueId(), (double) size);
            player.getAttribute(Attribute.SCALE).setBaseValue(size);
        }
    }

    private void resetPlayerSize(Player player) {
        player.getAttribute(Attribute.SCALE).setBaseValue(1.0); // Reset to default size
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!isActive) return;
        
        Player player = event.getPlayer();
        
        // If this player already has a size assigned (reconnected), use that
        if (playerSizes.containsKey(player.getUniqueId())) {
            player.getAttribute(Attribute.SCALE).setBaseValue(playerSizes.get(player.getUniqueId()));
        } else {
            // Otherwise assign a new random size
            float size = getRandomSize(min, max);
            playerSizes.put(player.getUniqueId(), (double) size);
            player.getAttribute(Attribute.SCALE).setBaseValue(size);
        }
    }
}
