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
    }
    
    public void start() {
        if (isActive) return;
        
        isActive = true;
        
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
                end();
            }
        }.runTaskLater(plugin, duration * 20L);
    }
    
    private void end() {
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
        
        // Aumentar velocidad de los no-muertos
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (isUndead(entity)) {
                    entity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(
                        entity.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue() * 2
                    );
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
        
        // Daño doble de no-muertos
        if (isUndead(damager)) {
            double damage = event.getDamage();
            event.setDamage(damage * 2);
            
            // Infectar jugadores si son golpeados por zombies
            if (damager.getType() == EntityType.ZOMBIE && victim instanceof Player) {
                Player player = (Player) victim;
                infectedPlayers.put(player.getUniqueId(), true);
                player.sendMessage(ColorText.Colorize("&c¡Has sido infectado! Come una zanahoria dorada para curarte."));
            }
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

                eternalAPI.setTag(killer, new Tag("necroestallido", "necroestallido", "&5Necroestallido"));
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
}