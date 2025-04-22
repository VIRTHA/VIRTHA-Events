package com.darkbladedev.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to manage and retrieve world indices from Bukkit's world list
 */
public class WorldIndexManager {
    
    private static Map<String, Integer> worldIndices = new HashMap<>();
    
    /**
     * Updates the internal map of world indices
     * Should be called when worlds are added or removed
     */
    public static void updateWorldIndices() {
        worldIndices.clear();
        List<World> worlds = Bukkit.getWorlds();
        
        for (int i = 0; i < worlds.size(); i++) {
            World world = worlds.get(i);
            worldIndices.put(world.getName(), i);
        }
    }
    
    /**
     * Gets the index of a world in the Bukkit.getWorlds() list
     * @param worldName The name of the world
     * @return The index of the world, or -1 if not found
     */
    public static int getWorldIndex(String worldName) {
        // Update indices if the map is empty
        if (worldIndices.isEmpty()) {
            updateWorldIndices();
        }
        
        return worldIndices.getOrDefault(worldName, -1);
    }
    
    /**
     * Gets the index of a world in the Bukkit.getWorlds() list
     * @param world The world object
     * @return The index of the world, or -1 if not found
     */
    public static int getWorldIndex(World world) {
        if (world == null) return -1;
        return getWorldIndex(world.getName());
    }
    
    /**
     * Gets a world by its index in the Bukkit.getWorlds() list
     * @param index The index of the world
     * @return The world at the specified index, or null if the index is invalid
     */
    public static World getWorldByIndex(int index) {
        List<World> worlds = Bukkit.getWorlds();
        if (index >= 0 && index < worlds.size()) {
            return worlds.get(index);
        }
        return null;
    }
    
    /**
     * Gets the total number of worlds
     * @return The number of worlds
     */
    public static int getWorldCount() {
        return Bukkit.getWorlds().size();
    }
}