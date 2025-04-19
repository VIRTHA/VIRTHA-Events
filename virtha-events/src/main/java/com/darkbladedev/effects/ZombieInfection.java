package com.darkbladedev.effects;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.darkbladedev.utils.ColorText;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ZombieInfection implements Listener {
    
    private static ZombieInfection instance;
    private final Plugin plugin;
    
    // Key para almacenar la infección en los datos persistentes del jugador
    private final NamespacedKey zombieInfectionKey;
    private final NamespacedKey InfectionCureCountKey;
    
    // Mapa para rastrear jugadores infectados y sus tareas
    private final Map<UUID, BukkitTask> infectionTasks = new HashMap<>();
    private final Set<UUID> infectedPlayers = new HashSet<>();
    
    // Contador de infecciones curadas por jugador
    private final Map<UUID, Integer> curedInfectionsCount = new HashMap<>();
    
    private BukkitTask timeCheckTask;
    private boolean isEnabled = true;
    
    private ZombieInfection(Plugin plugin) {
        this.plugin = plugin;
        this.zombieInfectionKey = new NamespacedKey(plugin, "is_infected");
        this.InfectionCureCountKey = new NamespacedKey(plugin, "infection_cure_count");
        
        // Registrar eventos
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Iniciar tarea de verificación de tiempo
        startTimeCheckTask();
    }
    
    public static ZombieInfection getInstance(Plugin plugin) {
        if (instance == null) {
            instance = new ZombieInfection(plugin);
        }
        return instance;
    }
    
    /**
     * Inicia la tarea que verifica el tiempo del día para aplicar los efectos correspondientes
     */
    private void startTimeCheckTask() {
        if (timeCheckTask != null) {
            timeCheckTask.cancel();
        }
        
        timeCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isEnabled) return;
                
                // Obtener el tiempo del mundo principal
                World world = Bukkit.getWorlds().get(0);
                long time = world.getTime();
                
                // Aplicar efectos según la hora del día a todos los jugadores infectados
                for (UUID playerId : infectedPlayers) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        applyTimeBasedEffects(player, time);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 100L); // Verificar cada 5 segundos (100 ticks)
    }
    
    // Add these fields at the top of the class with other fields
    private final Map<UUID, Long> lastActionBarMessageTime = new HashMap<>();
    private final long ACTION_BAR_COOLDOWN = 3000; // 3 seconds cooldown in milliseconds
    
    /**
     * Aplica efectos basados en la hora del día
     * @param player El jugador infectado
     * @param time El tiempo actual del mundo (0-24000)
     */
    private void applyTimeBasedEffects(Player player, long time) {
        // Mañana (0-3000) y atardecer (12000-14000): Debilidad
        if ((time >= 0 && time <= 3000) || (time >= 12000 && time <= 14000)) {
            if (!player.hasPotionEffect(PotionEffectType.WEAKNESS)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 140, 0, false, true, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 0, false, true, true));
                sendActionBar(player, "&7La infección zombie te debilita durante este momento del día...");
            }
        }
        // Mediodía y parte de la tarde (5000-11000): Fuego
        else if (time >= 5000 && time <= 11000) {
            // Aplicar efecto de wither y fuego sin importar si ya tiene el efecto
            player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 0, false, true, true));
            player.setFireTicks(Math.max(player.getFireTicks(), 100)); // Asegurar al menos 5 segundos de fuego
            sendActionBar(player, "&c¡La luz del sol quema tu piel infectada!");
            
            // Efectos visuales adicionales para enfatizar el fuego
            player.getWorld().spawnParticle(Particle.FLAME, 
                player.getLocation().add(0, 1, 0), 
                10, 0.3, 0.5, 0.3, 0.01);
        }
        // Noche (14000-24000): Fuerza
        else if (time >= 14000 && time <= 24000) {
            if (!player.hasPotionEffect(PotionEffectType.STRENGTH)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 300, 0, false, true, true));
                sendActionBar(player, "&8La oscuridad de la noche &4fortalece &8tu infección...");
            }
        }
        
        // Efectos visuales constantes para mostrar que está infectado
        player.getWorld().spawnParticle(Particle.SMOKE, 
            player.getLocation().add(0, 1, 0), 
            5, 0.3, 0.5, 0.3, 0.01);
    }
    
    /**
     * Envía un mensaje al action bar del jugador con control de cooldown
     * @param player El jugador al que enviar el mensaje
     * @param message El mensaje a enviar
     */
    private void sendActionBar(Player player, String message) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // Verificar si ha pasado suficiente tiempo desde el último mensaje
        if (!lastActionBarMessageTime.containsKey(playerId) || 
            currentTime - lastActionBarMessageTime.get(playerId) >= ACTION_BAR_COOLDOWN) {
            
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                TextComponent.fromLegacyText(ColorText.ColorizeNoPrefix(message)));
            
            // Actualizar el tiempo del último mensaje
            lastActionBarMessageTime.put(playerId, currentTime);
        }
    }
    
    /**
     * Infecta a un jugador con el virus zombie
     * @param player El jugador a infectar
     */
    public void infectPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Si ya está infectado, no hacer nada
        if (isInfected(player)) return;
        
        // Marcar al jugador como infectado
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(zombieInfectionKey, PersistentDataType.BOOLEAN, true);
        infectedPlayers.add(playerId);
        
        // Notificar al jugador
        player.sendMessage(ColorText.Colorize("&2¡Has sido infectado con el virus zombie! &aCome una manzana dorada para curarte."));
        player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 1.0f, 0.5f);
        
        // Efectos visuales iniciales
        player.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, 
            player.getLocation().add(0, 1, 0), 
            15, 0.5, 0.5, 0.5, 0.1);
        
        // Aplicar efectos iniciales
        World world = player.getWorld();
        applyTimeBasedEffects(player, world.getTime());
    }
    
    /**
     * Cura a un jugador de la infección zombie
     * @param player El jugador a curar
     */
    public void curePlayer(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Si no está infectado, no hacer nada
        if (!isInfected(player)) return;
        
        // Eliminar la marca de infección
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.remove(zombieInfectionKey);
        infectedPlayers.remove(playerId);

        pdc.set(InfectionCureCountKey, PersistentDataType.INTEGER, pdc.getOrDefault(InfectionCureCountKey, PersistentDataType.INTEGER, 0) + 1);
        
        // Cancelar tareas asociadas
        if (infectionTasks.containsKey(playerId)) {
            infectionTasks.get(playerId).cancel();
            infectionTasks.remove(playerId);
        }
        
        // Eliminar efectos negativos
        player.removePotionEffect(PotionEffectType.HUNGER);
        player.removePotionEffect(PotionEffectType.NAUSEA);
        player.removePotionEffect(PotionEffectType.WITHER);
        player.setFireTicks(0);
        
        // Notificar al jugador
        sendActionBar(player, "&a¡Te has curado de la infección zombie!");
        player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 0.7f, 1.0f);
        
        // Efectos visuales de curación
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, 
            player.getLocation().add(0, 1, 0), 
            20, 0.5, 0.5, 0.5, 0.1);
        
        // Registrar curación para recompensas
        int curedCount = curedInfectionsCount.getOrDefault(playerId, 0) + 1;
        curedInfectionsCount.put(playerId, curedCount);
        
        // Recompensa por curarse múltiples veces
        if (curedCount == 5) {
            player.sendMessage(ColorText.Colorize("&a¡Has sobrevivido a 5 infecciones zombie!"));
            player.sendMessage(ColorText.Colorize("&6Recompensa: Resistencia parcial a futuras infecciones"));
        }
    }
    
    /**
     * Verifica si un jugador está infectado
     * @param player El jugador a verificar
     * @return true si está infectado, false en caso contrario
     */
    public boolean isInfected(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        return pdc.has(zombieInfectionKey, PersistentDataType.BOOLEAN);
    }
    
    /**
     * Habilita o deshabilita el sistema de infección zombie
     * @param enabled true para habilitar, false para deshabilitar
     */
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        
        if (!enabled) {
            // Curar a todos los jugadores si se deshabilita
            for (UUID playerId : new HashSet<>(infectedPlayers)) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    curePlayer(player);
                }
            }
        }
    }
    
    /**
     * Verifica si el jugador tiene resistencia a la infección (por haber sido curado 5+ veces)
     * @param player El jugador a verificar
     * @return true si tiene resistencia parcial
     */
    private boolean hasInfectionResistance(Player player) {
        int curedCount = curedInfectionsCount.getOrDefault(player.getUniqueId(), 0);
        return curedCount >= 5;
    }
    
    /**
     * Verifica si el jugador tiene un arma en la mano
     * @param player El jugador a verificar
     * @return true si tiene un arma
     */
    private boolean hasWeaponInHand(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        Material type = mainHand.getType();
        
        // Verificar si es un arma
        return type.toString().contains("SWORD") || 
               type.toString().contains("AXE") || 
               type.toString().contains("TRIDENT") ||
               type == Material.BOW || 
               type == Material.CROSSBOW;
    }
    
    // Eventos
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!isEnabled) return;
        
        Player player = event.getPlayer();
        
        // Verificar si el jugador estaba infectado al desconectarse
        if (isInfected(player)) {
            infectedPlayers.add(player.getUniqueId());
            player.sendMessage(ColorText.Colorize("&2Sigues infectado con el virus zombie. &aCome una manzana dorada para curarte."));
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled) return;
        
        // Verificar si un jugador infectado golpea a otro jugador
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            
            // Si el atacante está infectado y no tiene arma en la mano
            if (isInfected(attacker) && !hasWeaponInHand(attacker)) {
                // Probabilidad de infección (50% base, reducida si tiene resistencia)
                double infectionChance = hasInfectionResistance(victim) ? 0.25 : 0.5;
                
                if (Math.random() < infectionChance) {
                    infectPlayer(victim);
                    attacker.sendMessage(ColorText.Colorize("&2¡Has infectado a " + victim.getName() + "!"));
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        if (!isEnabled) return;
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // Curar infección con manzana dorada
        if ((item.getType() == Material.GOLDEN_APPLE || item.getType() == Material.ENCHANTED_GOLDEN_APPLE) 
                && isInfected(player)) {
            curePlayer(player);
            
            // Bonus de salud temporal por usar manzana encantada
            if (item.getType() == Material.ENCHANTED_GOLDEN_APPLE) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 1, false, true, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 6000, 0, false, true, true));
                player.sendMessage(ColorText.Colorize("&6La manzana encantada te ha dado protección adicional contra futuras infecciones."));
            }
        }
    }
    
    /**
     * Obtiene el número de veces que un jugador se ha curado de la infección
     * @param playerId UUID del jugador
     * @return Número de curaciones
     */
    public int getCuredCount(UUID playerId) {
        return curedInfectionsCount.getOrDefault(playerId, 0);
    }
}