package com.darkbladedev.mechanics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class ParanoiaEffect {

    private final Random random = new Random();
    private BukkitRunnable task;

    private final Plugin plugin;
    private final float duration;
    private final int interval;

    public ParanoiaEffect(Plugin plugin, float duration, int interval) {
        this.plugin = plugin;
        this.duration = duration;
        this.interval = interval;
    }

    public void start() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        float durationSeconds = duration * 20; // 1 second in ticks

        if (players.isEmpty()) {
            return; // No players online
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : players) {
                    player.setGlowing(true);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, (int) (duration), 0));
                    for (int i = 0; i < interval; i++) {
                        List<Sound> sounds = generateSoundList();
                        player.playSound(player.getLocation(), sounds.get(random.nextInt(sounds.size())), 1, 1);
                    }
                }
            }
        };

        task.runTaskTimer(plugin, 0L, 3L);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (task != null) {
                    task.cancel();
                    task = null;
                }
            }
        }.runTaskLater(plugin, (long) durationSeconds);
    }

    private List<Sound> generateSoundList() {
        List<Sound> sounds = new ArrayList<>();
        
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
        //sounds.add(Sound.ENTITY_WARDEN_NEARBY_CLOSE);
        //sounds.add(Sound.ENTITY_CREAKING_ACTIVATE);
        
        return sounds;
    }
}
