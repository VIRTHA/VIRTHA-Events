package com.darkbladedev.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.darkbladedev.utils.ColorText;

import xyz.oribuin.eternaltags.EternalAPI;
import xyz.oribuin.eternaltags.obj.Tag;

import java.util.*;

// Add this import at the top with other imports
import org.bukkit.event.entity.EntitySpawnEvent;

public class UndeadWeek implements Listener {

    // EternalTags API
    private final EternalAPI eternalAPI = EternalAPI.getInstance();
    
    private final Plugin plugin;
    private final long duration;
    private boolean isActive = false;
    private boolean isRedMoonActive = false;
    private int nightCounter = 0;
    private BukkitTask mainTask;
    
    // Tracking infected players and challenges
    private final Map<UUID, Boolean> infectedPlayers = new HashMap<>();
    private final Map<UUID, Integer> curedInfectionsCount = new HashMap<>();
    private final Map<UUID, Integer> redMoonKillsCount = new HashMap<>();
    private final Set<UUID> curedVillagers = new HashSet<>();
    private final Set<UUID> witherKilledInRedMoon = new HashSet<>();
    
    // Lista de entidades no-muertas
    private final List<EntityType> undeadEntities = Arrays.asList(
        EntityType.ZOMBIE, EntityType.SKELETON, EntityType.WITHER_SKELETON,
        EntityType.ZOMBIFIED_PIGLIN, EntityType.WITHER, EntityType.ZOMBIE_VILLAGER,
        EntityType.DROWNED, EntityType.HUSK, EntityType.STRAY, EntityType.PHANTOM,
        EntityType.ZOGLIN
    );
    
    public UndeadWeek(Plugin plugin, long duration) {
        this.plugin = plugin;
        this.duration = duration;
        
        // Register events
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    public void stop() {
        if (!isActive) return;
        
        if (mainTask != null) {
            mainTask.cancel();
            mainTask = null;
        }
        
        // Restaurar estados normales
        isRedMoonActive = false;
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == World.Environment.NORMAL) {
                world.setTime(0); // Día
            }
        }
        
        // Curar a todos los jugadores infectados
        for (UUID playerId : infectedPlayers.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.removePotionEffect(PotionEffectType.POISON);
            }
        }
        
        // Otorgar recompensas finales
        giveRewards();
        
        // Anunciar fin del evento
        Bukkit.broadcastMessage(ColorText.Colorize("&4[&cSemana de los No Muertos&4] &aLas hordas de no-muertos se han retirado... por ahora."));
        
        isActive = false;
    }
    
    private void checkTime() {
        // Solo verificar en el mundo normal
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == World.Environment.NORMAL) {
                long time = world.getTime();
                
                // Verificar si es de noche (13000-23000)
                if (time >= 13000 && time <= 23000) {
                    // Verificar si es una nueva noche
                    if (time >= 13000 && time <= 13100 && !isRedMoonActive) {
                        nightCounter++;
                        
                        // Cada 3 noches, activar la luna roja
                        if (nightCounter % 3 == 0) {
                            activateRedMoon();
                        }
                    }
                } else {
                    // Si es de día, desactivar la luna roja
                    if (isRedMoonActive) {
                        deactivateRedMoon();
                    }
                }
            }
        }
    }
    
    private void activateRedMoon() {
        isRedMoonActive = true;
        Bukkit.broadcastMessage(ColorText.Colorize("&4[&cSemana de los No Muertos&4] &c¡La Noche Roja ha comenzado! Los no-muertos son más rápidos y las camas explotan."));
        
        // Aumentar velocidad de los no-muertos y asegurar que tengan fuerza
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (isUndead(entity)) {
                    // Apply movement speed boost
                    if (entity.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                        entity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(
                            entity.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue() * 2
                        );
                    }
                    
                    // Ensure they have Strength II
                    if (!entity.hasPotionEffect(PotionEffectType.STRENGTH)) {
                        entity.addPotionEffect(new PotionEffect(
                            PotionEffectType.STRENGTH,
                            Integer.MAX_VALUE,
                            1,
                            false,
                            false,
                            true
                        ));
                    }
                }
            }
        }
    }
    
    private void deactivateRedMoon() {
        isRedMoonActive = false;
        Bukkit.broadcastMessage(ColorText.Colorize("&4[&cSemana de los No Muertos&4] &aLa Noche Roja ha terminado."));
        
        // Restaurar velocidad normal de los no-muertos
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (isUndead(entity)) {
                    entity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(
                        entity.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue() / 2
                    );
                }
            }
        }
    }
    
    private void checkInfectedPlayers() {
        // Verificar jugadores infectados
        for (Map.Entry<UUID, Boolean> entry : new HashMap<>(infectedPlayers).entrySet()) {
            if (entry.getValue()) { // Si está infectado
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null && player.isOnline()) {
                    // Aplicar efecto de veneno que no mata
                    if (!player.hasPotionEffect(PotionEffectType.POISON)) {
                        player.addPotionEffect(new PotionEffect(
                            PotionEffectType.POISON, 
                            Integer.MAX_VALUE, 
                            0, // Nivel 1
                            false, 
                            true, 
                            true
                        ));
                    }
                    
                    // Asegurarse de que el veneno no mate al jugador
                    if (player.getHealth() <= 1.0) {
                        player.removePotionEffect(PotionEffectType.POISON);
                    }
                }
            }
        }
    }
    
    private void checkNetheriteArmor() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            int netheriteCount = 0;
            
            // Contar piezas de netherite
            for (ItemStack item : player.getInventory().getArmorContents()) {
                if (item != null && item.getType().toString().contains("NETHERITE")) {
                    netheriteCount++;
                }
            }
            
            // Aplicar efecto wither modificado según cantidad de piezas
            if (netheriteCount > 0) {
                player.addPotionEffect(new PotionEffect(
                    PotionEffectType.WITHER, 
                    40, // 2 segundos
                    netheriteCount - 1, // Nivel según cantidad de piezas
                    false, 
                    true, 
                    true
                ));
            }
        }
    }
    

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!isActive) return;
        
        Entity entity = event.getEntity();
        
        // Check if the entity is undead and is a LivingEntity (to apply potion effects)
        if (isUndead(entity) && entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            
            // Apply Strength II effect (infinite duration)
            livingEntity.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH,
                Integer.MAX_VALUE,  // Infinite duration
                1,                  // Level II (0-based, so 1 = level II)
                false,              // No ambient particles
                false,              // No particles
                true                // Show icon
            ));
            
            // If it's red moon night, also apply speed boost as before
            if (isRedMoonActive) {
                if (livingEntity.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                    livingEntity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(
                        livingEntity.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue() * 2
                    );
                }
            }
        }
    }


    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isActive) return;
        
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();
        
        // Manejar proyectiles
        if (damager instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) damager).getShooter();
            if (shooter instanceof Entity) {
                damager = (Entity) shooter;
            }
        }
        
        // Removed the damage doubling code since we're now using Strength effect
        
        // Infectar jugadores si son golpeados por zombies
        if (isUndead(damager) && damager.getType() == EntityType.ZOMBIE && victim instanceof Player) {
            Player player = (Player) victim;
            infectedPlayers.put(player.getUniqueId(), true);
            player.sendMessage(ColorText.Colorize("&c¡Has sido infectado! Come una zanahoria dorada para curarte."));
        }
        
        // Registrar muertes en noche roja
        if (isRedMoonActive && damager instanceof Player && victim instanceof Player) {
            Player killer = (Player) damager;
            redMoonKillsCount.put(killer.getUniqueId(), 
                redMoonKillsCount.getOrDefault(killer.getUniqueId(), 0) + 1);
            
            // Verificar desafío
            if (redMoonKillsCount.get(killer.getUniqueId()) == 3) {
                killer.sendMessage(ColorText.Colorize("&a¡Desafío completado! Has matado a 3 jugadores en la Noche Roja."));
                killer.sendMessage(ColorText.Colorize("&6Recompensa: Tag 'Necroestallido'"));
                // Aquí se aplicaría el tag

                eternalAPI.setTag(killer, new Tag("necroestallido", "necroestallido", "&x&1&7&c&e&2&9N&x&1&6&c&9&3&7e&x&1&4&c&4&4&5c&x&1&3&c&0&5&3r&x&1&2&b&b&6&0o&x&1&0&b&6&6&ee&x&0&f&b&1&7&cs&x&0&e&a&d&8&at&x&0&d&a&8&9&8a&x&0&b&a&3&a&6l&x&0&a&9&e&b&3l&x&0&9&9&a&c&1i&x&0&7&9&5&c&fd&x&0&6&9&0&d&do")); //&#49bf40N&#46bd49e&#43bc53c&#41ba5cr&#3eb865o&#3bb76fe&#38b578s&#36b381t&#33b18aa&#30b094l&#2dae9dl&#2baca6i&#28abb0d&#25a9b9o
            }
        }
        
        // Registrar muerte del Wither en noche roja
        if (isRedMoonActive && victim.getType() == EntityType.WITHER && damager instanceof Player) {
            Player killer = (Player) damager;
            World world = killer.getWorld();
            
            if (world.getEnvironment() == World.Environment.NORMAL) {
                witherKilledInRedMoon.add(killer.getUniqueId());
                killer.sendMessage(ColorText.Colorize("&a¡Desafío legendario completado! Has derrotado al Wither en la Noche Roja."));
                
                // Aumentar corazón máximo
                double currentMaxHealth = killer.getAttribute(Attribute.MAX_HEALTH).getValue();
                killer.getAttribute(Attribute.MAX_HEALTH).setBaseValue(currentMaxHealth + 2.0);
                killer.sendMessage(ColorText.Colorize("&6Recompensa: +1 corazón máximo"));
            }
        }
    }
    
    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        if (!isActive) return;
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // Curar infección con zanahoria dorada
        if (item.getType() == Material.GOLDEN_CARROT && infectedPlayers.getOrDefault(player.getUniqueId(), false)) {
            infectedPlayers.put(player.getUniqueId(), false);
            player.removePotionEffect(PotionEffectType.POISON);
            player.sendMessage(ColorText.Colorize("&a¡Te has curado de la infección!"));
            
            // Registrar para el desafío
            int curedCount = curedInfectionsCount.getOrDefault(player.getUniqueId(), 0) + 1;
            curedInfectionsCount.put(player.getUniqueId(), curedCount);
            
            if (curedCount == 10) {
                player.sendMessage(ColorText.Colorize("&a¡Desafío completado! Has curado 10 infecciones."));
                
                // Aumentar corazón máximo
                double currentMaxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(currentMaxHealth + 2.0);
                player.sendMessage(ColorText.Colorize("&6Recompensa: +1 corazón máximo"));
            }
        }
    }
    
    @EventHandler
    public void onEntityTransform(EntityTransformEvent event) {
        if (!isActive) return;
        
        // Detectar curación de aldeanos zombificados
        if (event.getEntity().getType() == EntityType.ZOMBIE_VILLAGER && 
            event.getTransformedEntity().getType() == EntityType.VILLAGER) {
            
            // Buscar al jugador responsable (esto es aproximado, podría mejorarse)
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getLocation().distance(event.getEntity().getLocation()) <= 10) {
                    curedVillagers.add(player.getUniqueId());
                    player.sendMessage(ColorText.Colorize("&a¡Desafío completado! Has curado a un aldeano zombificado."));
                    player.sendMessage(ColorText.Colorize("&6Recompensa: Encantamiento First Strike"));
                    // Aquí se aplicaría el encantamiento
                    break;
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (!isActive || !isRedMoonActive) return;
        
        // Explotar camas en noche roja
        event.setCancelled(true);
        Player player = event.getPlayer();
        player.sendMessage(ColorText.Colorize("&c¡No puedes dormir durante la Noche Roja!"));
        
        // Crear explosión
        player.getWorld().createExplosion(event.getBed().getLocation(), 2.0f, false, true);
    }
    
    private boolean isUndead(Entity entity) {
        return undeadEntities.contains(entity.getType());
    }
    
    private void giveRewards() {
        // Implementar lógica para dar recompensas finales
        // Esto podría incluir persistencia de recompensas, etc.
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public boolean isRedMoonActive() {
        return isRedMoonActive;
    }

    private boolean isPaused = false;

    public void pause() {
        if (!isActive || isPaused) return;
        
        isPaused = true;
        
        // Cancel the main task
        if (mainTask != null) {
            mainTask.cancel();
            mainTask = null;
        }
        
        // Temporarily restore normal day/night cycle
        isRedMoonActive = false;
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == World.Environment.NORMAL) {
                world.setTime(0); // Day
            }
        }
        
        // Temporarily remove effects from infected players
        for (UUID playerId : infectedPlayers.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.removePotionEffect(PotionEffectType.POISON);
            }
        }
    }

    public void resume() {
        if (!isActive || !isPaused) return;
        
        isPaused = false;
        
        // Restart the main task
        startMainTask();
        
        // Reapply effects to infected players
        for (UUID playerId : infectedPlayers.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 60, 0, false, true, true));
            }
        }
    }

    private void startMainTask() {
        // Cancel existing task if any
        if (mainTask != null) {
            mainTask.cancel();
        }
        
        // Start the main task
        mainTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Toggle red moon every night
                long time = Bukkit.getWorlds().get(0).getTime();
                if (time >= 13000 && time <= 23000) { // Night time
                    if (!isRedMoonActive) {
                        activateRedMoon();
                    }
                } else {
                    if (isRedMoonActive) {
                        deactivateRedMoon();
                    }
                }
                
                // Check infected players
                checkInfectedPlayers();
            }
        }.runTaskTimer(plugin, 0L, 20L * 30); // Check every 30 seconds
    }

    // Update start method to use the new task method
    public void start() {
        if (isActive) return;
        
        isActive = true;
        isPaused = false;
        
        // Start the main task
        startMainTask();
        
        // Anunciar el inicio del evento
        Bukkit.broadcastMessage(ColorText.Colorize("&4[&cSemana de los No Muertos&4] &cLas hordas de no-muertos dominan el mundo..."));
        
        // Tarea principal que se ejecuta cada tick para verificar condiciones
        mainTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkTime();
                checkInfectedPlayers();
                checkNetheriteArmor();
            }
        }.runTaskTimer(plugin, 0L, 20L); // Cada segundo
        
        // Programar el fin del evento
        new BukkitRunnable() {
            @Override
            public void run() {
                stop();
            }
        }.runTaskLater(plugin, duration * 20L);
    }

    /**
     * Checks if a player has completed a specific challenge
     * @param playerId The UUID of the player
     * @param challengeId The ID of the challenge
     * @return true if the challenge is completed, false otherwise
     */
    public boolean hasChallengeCompleted(UUID playerId, String challengeId) {
        switch (challengeId) {
            case "cure_infection":
                // Player has cured their infection
                return curedInfectionsCount.getOrDefault(playerId, 0) > 0;
            case "cure_villager":
                // Player has cured a zombie villager
                return curedVillagers.contains(playerId);
            case "red_moon_kills":
                // Player has killed a certain number of mobs during red moon
                return redMoonKillsCount.getOrDefault(playerId, 0) >= 10;
            case "wither_red_moon":
                // Player has killed a wither during red moon
                return witherKilledInRedMoon.contains(playerId);
            default:
                return false;
        }
    }
}