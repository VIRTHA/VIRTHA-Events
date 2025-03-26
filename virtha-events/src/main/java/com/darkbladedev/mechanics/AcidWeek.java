package com.darkbladedev.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AcidWeek implements Listener {
    
    private final Plugin plugin;
    private final Set<UUID> playersInWater = new HashSet<>();
    private final Set<UUID> playersInRain = new HashSet<>();
    private BukkitTask acidTask;
    private BukkitTask weatherTask;
    private boolean isActive = false;
    private final long duration;
    
    public AcidWeek(Plugin plugin, long duration) {
        this.plugin = plugin;
        this.duration = duration;
    }
    
    public void start() {
        if (isActive) return;
        
        isActive = true;
        long durationTicks = duration * 20; // Convertir segundos a ticks
        
        // Iniciar la tarea principal de daño por ácido
        startAcidDamageTask();
        
        // Iniciar la tarea de control del clima
        startWeatherControlTask();
        
        // Registrar eventos
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Programar el fin del evento
        new BukkitRunnable() {
            @Override
            public void run() {
                stop();
            }
        }.runTaskLater(plugin, durationTicks);
    }
    
    public void stop() {
        if (!isActive) return;
        
        // Cancelar tareas
        if (acidTask != null) {
            acidTask.cancel();
            acidTask = null;
        }
        
        if (weatherTask != null) {
            weatherTask.cancel();
            weatherTask = null;
        }
        
        // Limpiar conjuntos
        playersInWater.clear();
        playersInRain.clear();
        
        // Restaurar clima normal en todos los mundos
        for (World world : Bukkit.getWorlds()) {
            world.setStorm(false);
            world.setThundering(false);
        }
        
        isActive = false;
    }
    
    private boolean hasAcidResistance(Player player) {
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasEnchant(Registry.ENCHANTMENT.get(new NamespacedKey(plugin, "acid_resistance")))) {

                return true;
            }
        }
        return false;
    }
    
    private void startAcidDamageTask() {
        acidTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Daño a jugadores en agua (cada 3 segundos)
                for (UUID uuid : playersInWater) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        // Verificar si el jugador tiene el encantamiento de resistencia al ácido
                        if (!hasAcidResistance(player)) {
                            // 2 puntos = 1 corazón
                            player.damage(2.0); // Ignora armadura al usar damage() directamente
                            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0, false, true, true));
                        }
                    }
                }
                
                // Daño a jugadores bajo la lluvia (cada 5 segundos)
                if (this.getTaskId() % (5*20/60) == 0) { // Cada 5 segundos (si el task corre cada 3 ticks)
                    for (UUID uuid : playersInRain) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null && player.isOnline()) {
                            // Verificar si el jugador tiene el encantamiento de resistencia al ácido
                            if (!hasAcidResistance(player)) {
                                player.damage(2.0); // 1 corazón
                                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0, false, true, true));
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 60L); // Ejecutar cada 3 segundos (60 ticks)
    }
    
    private void startWeatherControlTask() {
        weatherTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    long time = world.getTime();
                    
                    // Día: 0-12000, Noche: 12001-24000
                    if (time >= 0 && time < 12000) {
                        // Durante el día: siempre llueve
                        world.setStorm(true);
                        world.setThundering(true);
                    } else {
                        // Durante la noche: se calma
                        world.setStorm(false);
                        world.setThundering(false);
                    }
                }
                
                // Actualizar jugadores en lluvia
                updatePlayersInRain();
            }
        }.runTaskTimer(plugin, 0L, 100L); // Verificar cada 5 segundos
    }
    
    private void updatePlayersInRain() {
        playersInRain.clear();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            World world = player.getWorld();
            
            // Verificar si está lloviendo en el mundo
            if (world.hasStorm()) {
                Location loc = player.getLocation();
                
                // Verificar si el jugador está expuesto al cielo
                if (world.getHighestBlockYAt(loc) <= loc.getBlockY() && !isPlayerUnderRoof(player)) {
                    playersInRain.add(player.getUniqueId());
                }
            }
        }
    }
    
    private boolean isPlayerUnderRoof(Player player) {
        Location loc = player.getLocation();
        
        // Verificar bloques por encima del jugador hasta el límite del mundo
        for (int y = loc.getBlockY() + 2; y < player.getWorld().getMaxHeight(); y++) {
            Block block = player.getWorld().getBlockAt(loc.getBlockX(), y, loc.getBlockZ());
            if (block.getType().isSolid()) {
                return true; // Hay un bloque sólido encima
            }
        }
        
        return false; // No hay techo
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isActive) return;
        
        Player player = event.getPlayer();
        Block block = player.getLocation().getBlock();
        
        // Verificar si el jugador está en agua
        if (block.getType() == Material.WATER) {
            playersInWater.add(player.getUniqueId());
        } else {
            playersInWater.remove(player.getUniqueId());
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isActive) return;
        
        // Verificar si es un jugador dañado por una poción arrojadiza
        if (event.getEntity() instanceof Player && event.getDamager() instanceof ThrownPotion) {
            Player player = (Player) event.getEntity();
            ThrownPotion potion = (ThrownPotion) event.getDamager();
            
            // Verificar si es una botella de agua (sin efectos)
            if (potion.getItem().getType() == Material.SPLASH_POTION && 
                potion.getEffects().isEmpty()) {
                
                // Verificar si el jugador tiene el encantamiento de resistencia al ácido
                if (!hasAcidResistance(player)) {
                    // 3 puntos = 1.5 corazones
                    player.damage(3.0); // Ignora armadura
                    event.setCancelled(true); // Cancelar el evento original
                } else {
                    event.setCancelled(true); // Cancelar el evento si tiene resistencia
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        if (!isActive) return;
        
        ItemStack item = event.getItem();
        Material type = item.getType();
        
        // Verificar si es una herramienta o armadura
        if (isToolOrArmor(type)) {
            // Duplicar el daño (2x durabilidad)
            event.setDamage(event.getDamage() * 2);
        }
    }
    
    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        if (!isActive) return;
        
        Block block = event.getBlock();
        
        // Verificar si es un cultivo
        if (block.getBlockData() instanceof Ageable) {
            // Verificar si tiene techo
            if (!hasRoof(block)) {
                event.setCancelled(true); // Cancelar crecimiento
            }
        }
    }
    
    private boolean hasRoof(Block block) {
        // Verificar si hay bloques sólidos encima
        for (int y = block.getY() + 1; y < block.getWorld().getMaxHeight(); y++) {
            Block blockAbove = block.getWorld().getBlockAt(block.getX(), y, block.getZ());
            if (blockAbove.getType().isSolid()) {
                return true; // Hay un bloque sólido encima
            }
        }
        
        return false; // No hay techo
    }
    
    private boolean isToolOrArmor(Material material) {
        String name = material.name();
        return name.endsWith("_PICKAXE") || 
               name.endsWith("_AXE") || 
               name.endsWith("_SHOVEL") || 
               name.endsWith("_HOE") || 
               name.endsWith("_SWORD") || 
               name.endsWith("_HELMET") || 
               name.endsWith("_CHESTPLATE") || 
               name.endsWith("_LEGGINGS") || 
               name.endsWith("_BOOTS");
    }
    
    public boolean isActive() {
        return isActive;
    }
}