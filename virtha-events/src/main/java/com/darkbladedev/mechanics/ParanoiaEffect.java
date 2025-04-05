package com.darkbladedev.mechanics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class ParanoiaEffect {

    private final Random random = new Random();

    private final Plugin plugin;
    private final long duration;
    private final long interval;
    
    private boolean isActive = false;
    private BukkitTask mainTask;
    private BukkitTask soundTask;
    private final Set<UUID> affectedPlayers = new HashSet<>();

    public ParanoiaEffect(Plugin plugin, long duration, long interval) {
        this.plugin = plugin;
        this.duration = duration;
        this.interval = interval;
    }

    public void start() {
        if (isActive) return;
        
        isActive = true;
        long durationTicks = (long)(duration * 20); // Convertir a ticks
        
        // Tarea principal que aplica efectos de poción y brillo
        mainTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!affectedPlayers.contains(player.getUniqueId())) {
                        // Aplicar efectos a nuevos jugadores
                        player.setGlowing(true);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, (int)(durationTicks), 0, false, true, true));
                        affectedPlayers.add(player.getUniqueId());
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 40L); // Verificar cada 2 segundos
        
        // Tarea separada para reproducir sonidos aleatorios
        soundTask = new BukkitRunnable() {
            @Override
            public void run() {
                List<Sound> sounds = generateSoundList();
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Reproducir sonidos aleatorios en posiciones aleatorias alrededor del jugador
                    if (random.nextInt(100) < 70) { // 70% de probabilidad de sonido
                        Sound randomSound = sounds.get(random.nextInt(sounds.size()));
                        
                        // Crear ubicación aleatoria alrededor del jugador
                        Location playerLoc = player.getLocation();
                        Location soundLoc = playerLoc.clone().add(
                            (random.nextDouble() - 0.5) * 10, // X: ±5 bloques
                            (random.nextDouble() - 0.5) * 6,  // Y: ±3 bloques
                            (random.nextDouble() - 0.5) * 10  // Z: ±5 bloques
                        );
                        
                        // Volumen y tono aleatorios para mayor efecto
                        float volume = 0.5f + random.nextFloat() * 0.5f;
                        float pitch = 0.8f + random.nextFloat() * 0.4f;
                        
                        player.playSound(soundLoc, randomSound, volume, pitch);
                    }
                }
            }
        }.runTaskTimer(plugin, 5L, Math.max(1, Math.round(20.0 / Math.max(1, interval)))); // Ajustar frecuencia según el intervalo
        
        // Programar el fin del efecto
        new BukkitRunnable() {
            @Override
            public void run() {
                stop();
            }
        }.runTaskLater(plugin, durationTicks);
        
        // Anunciar inicio del efecto
        Bukkit.broadcastMessage("§c§l¡La paranoia se apodera del servidor!");
    }
    
    public void stop() {
        if (!isActive) return;
        
        // Cancelar tareas
        if (mainTask != null) {
            mainTask.cancel();
            mainTask = null;
        }
        
        if (soundTask != null) {
            soundTask.cancel();
            soundTask = null;
        }
        
        // Eliminar efectos de todos los jugadores afectados
        for (UUID uuid : affectedPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.setGlowing(false);
                player.removePotionEffect(PotionEffectType.NAUSEA);
            }
        }
        
        affectedPlayers.clear();
        isActive = false;
        
        // Anunciar fin del efecto
        Bukkit.broadcastMessage("§a§lLa paranoia ha terminado... por ahora.");
    }

    private List<Sound> generateSoundList() {
        List<Sound> sounds = new ArrayList<>();
        
        // Sonidos originales
        sounds.add(Sound.ENTITY_ENDERMAN_STARE);
        sounds.add(Sound.ENTITY_GHAST_SCREAM);
        sounds.add(Sound.ENTITY_WITHER_AMBIENT);
        sounds.add(Sound.ENTITY_CREEPER_PRIMED);
        sounds.add(Sound.ENTITY_ZOMBIE_AMBIENT);
        sounds.add(Sound.ENTITY_SKELETON_AMBIENT);
        sounds.add(Sound.ENTITY_SPIDER_AMBIENT);
        sounds.add(Sound.ENTITY_ENDERMAN_TELEPORT);
        sounds.add(Sound.ENTITY_WITCH_AMBIENT);
        sounds.add(Sound.ENTITY_PHANTOM_AMBIENT);
        sounds.add(Sound.ENTITY_WOLF_GROWL);
        sounds.add(Sound.ENTITY_SILVERFISH_AMBIENT);
        sounds.add(Sound.ENTITY_ENDER_DRAGON_GROWL);
        
        // 1.21.4 sounds
        sounds.add(Sound.ENTITY_WARDEN_NEARBY_CLOSE);
        sounds.add(Sound.ENTITY_CREAKING_ACTIVATE);
        
        // Sonidos adicionales para mayor variedad
        sounds.add(Sound.AMBIENT_CAVE);
        sounds.add(Sound.BLOCK_CHAIN_BREAK);
        sounds.add(Sound.BLOCK_CHEST_LOCKED);
        sounds.add(Sound.ENTITY_ALLAY_DEATH);
        sounds.add(Sound.ENTITY_BLAZE_DEATH);
        sounds.add(Sound.ENTITY_WARDEN_HEARTBEAT);
        sounds.add(Sound.ENTITY_WARDEN_LISTENING);
        sounds.add(Sound.ENTITY_WARDEN_LISTENING_ANGRY);
        
        // Sonidos adicionales más molestos y ruidosos
        sounds.add(Sound.ENTITY_GHAST_HURT);
        sounds.add(Sound.ENTITY_GHAST_DEATH);
        sounds.add(Sound.ENTITY_WITHER_DEATH);
        sounds.add(Sound.ENTITY_ENDER_DRAGON_DEATH);
        sounds.add(Sound.ENTITY_ELDER_GUARDIAN_CURSE);
        sounds.add(Sound.ENTITY_ELDER_GUARDIAN_DEATH);
        sounds.add(Sound.ENTITY_ENDER_DRAGON_GROWL);
        sounds.add(Sound.ENTITY_ENDERMAN_DEATH);
        sounds.add(Sound.ENTITY_ENDERMAN_HURT);
        sounds.add(Sound.ENTITY_ENDERMAN_SCREAM);
        sounds.add(Sound.ENTITY_EVOKER_AMBIENT);
        sounds.add(Sound.ENTITY_EVOKER_DEATH);
        sounds.add(Sound.ENTITY_EVOKER_PREPARE_ATTACK);
        sounds.add(Sound.ENTITY_EVOKER_PREPARE_SUMMON);
        sounds.add(Sound.ENTITY_EVOKER_PREPARE_WOLOLO);
        sounds.add(Sound.ENTITY_RAVAGER_AMBIENT);
        sounds.add(Sound.ENTITY_RAVAGER_ATTACK);
        sounds.add(Sound.ENTITY_RAVAGER_DEATH);
        sounds.add(Sound.ENTITY_RAVAGER_HURT);
        sounds.add(Sound.ENTITY_RAVAGER_STEP);
        sounds.add(Sound.ENTITY_RAVAGER_STUNNED);
        sounds.add(Sound.ENTITY_RAVAGER_ROAR);
        sounds.add(Sound.ENTITY_VEX_AMBIENT);
        sounds.add(Sound.ENTITY_VEX_CHARGE);
        sounds.add(Sound.ENTITY_VEX_DEATH);
        sounds.add(Sound.ENTITY_VEX_HURT);
        sounds.add(Sound.ENTITY_WARDEN_AGITATED);
        sounds.add(Sound.ENTITY_WARDEN_ANGRY);
        sounds.add(Sound.ENTITY_WARDEN_ATTACK_IMPACT);
        sounds.add(Sound.ENTITY_WARDEN_DEATH);
        sounds.add(Sound.ENTITY_WARDEN_DIG);
        sounds.add(Sound.ENTITY_WARDEN_EMERGE);
        sounds.add(Sound.ENTITY_WARDEN_HURT);
        sounds.add(Sound.ENTITY_WARDEN_LISTENING_ANGRY);
        sounds.add(Sound.ENTITY_WARDEN_ROAR);
        sounds.add(Sound.ENTITY_WARDEN_SNIFF);
        sounds.add(Sound.ENTITY_WARDEN_SONIC_BOOM);
        sounds.add(Sound.ENTITY_WARDEN_SONIC_CHARGE);
        sounds.add(Sound.ENTITY_WARDEN_STEP);
        sounds.add(Sound.ENTITY_WARDEN_TENDRIL_CLICKS);
        
        // Sonidos de bloques inquietantes
        sounds.add(Sound.BLOCK_ANVIL_BREAK);
        sounds.add(Sound.BLOCK_ANVIL_DESTROY);
        sounds.add(Sound.BLOCK_ANVIL_FALL);
        sounds.add(Sound.BLOCK_ANVIL_HIT);
        sounds.add(Sound.BLOCK_ANVIL_LAND);
        sounds.add(Sound.BLOCK_ANVIL_PLACE);
        sounds.add(Sound.BLOCK_ANVIL_STEP);
        sounds.add(Sound.BLOCK_ANVIL_USE);
        sounds.add(Sound.BLOCK_BELL_RESONATE);
        sounds.add(Sound.BLOCK_BELL_USE);
        sounds.add(Sound.BLOCK_END_PORTAL_FRAME_FILL);
        sounds.add(Sound.BLOCK_END_PORTAL_SPAWN);
        sounds.add(Sound.BLOCK_RESPAWN_ANCHOR_AMBIENT);
        sounds.add(Sound.BLOCK_RESPAWN_ANCHOR_CHARGE);
        sounds.add(Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE);
        sounds.add(Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN);
        sounds.add(Sound.BLOCK_SCULK_CATALYST_BLOOM);
        sounds.add(Sound.BLOCK_SCULK_CHARGE);
        sounds.add(Sound.BLOCK_SCULK_SHRIEKER_SHRIEK);
        
        return sounds;
    }
    
    public boolean isActive() {
        return isActive;
    }
}
