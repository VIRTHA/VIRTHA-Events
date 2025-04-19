package com.darkbladedev.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.plugin.Plugin;

import com.darkbladedev.utils.ColorText;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.BanList;

public class HealthSteal implements Listener {
    
    private static final String BAN_REASON = "Has alcanzado el mínimo de corazones permitidos";
    private final Map<UUID, Integer> banCountMap = new HashMap<>();
    private final File banDataFile;
    private final Plugin plugin;
    
    public HealthSteal(Plugin plugin) {
        this.plugin = plugin;
        this.banDataFile = new File(plugin.getDataFolder(), "ban_data.json");
        
        // Create data folder if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        // Load ban data from JSON file
        loadBanData();
    }
    
    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player deadPlayer = event.getEntity();
        EntityDamageEvent lastDamage = deadPlayer.getLastDamageCause();

        if (lastDamage == null || !(lastDamage instanceof EntityDamageByEntityEvent)) return;

        Entity damager = ((EntityDamageByEntityEvent) lastDamage).getDamager();

        // Manejar proyectiles
        if (damager instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) damager).getShooter();
            if (shooter instanceof LivingEntity) {
                damager = (Entity) shooter;
            } else {
                return;
            }
        }

        if (!(damager instanceof LivingEntity)) return;
        LivingEntity killer = (LivingEntity) damager;

        // Ajustar salud (1 corazón = 2.0 puntos)
        double healthToSteal = 2.0;
        double currentMaxHealth = killer.getAttribute(Attribute.MAX_HEALTH).getValue();
        double newMaxHealth = currentMaxHealth + healthToSteal;

        // Limitar salud máxima si es necesario
        if (newMaxHealth > 40.0) newMaxHealth = 40.0;

        // Aplicar el aumento de salud al asesino
        killer.getAttribute(Attribute.MAX_HEALTH).setBaseValue(newMaxHealth);
        killer.setHealth(Math.min(killer.getHealth() + healthToSteal, newMaxHealth));

        // Reducir la salud máxima de la víctima cuando reaparezca
        double victimCurrentMaxHealth = deadPlayer.getAttribute(Attribute.MAX_HEALTH).getValue();
        double victimNewMaxHealth = victimCurrentMaxHealth - healthToSteal;
        
        // Evitar que la salud máxima baje de 6.0 (3 corazones)
        if (victimNewMaxHealth < 6.0) victimNewMaxHealth = 6.0;
        
        // Guardar el nuevo valor de salud máxima para aplicarlo cuando el jugador reaparezca
        deadPlayer.getAttribute(Attribute.MAX_HEALTH).setBaseValue(victimNewMaxHealth);
        
        // Mensaje al jugador víctima
        deadPlayer.sendMessage(ColorText.Colorize("&7¡&c" + (killer instanceof Player ? ((Player)killer).getName() : "Un mob") + " &cha robado 1 corazón de tu salud máxima&7!"));
        if (killer instanceof Player) {
            ((Player) killer).sendMessage(ColorText.Colorize("&7¡&aRobaste 1 corazón de &3" + deadPlayer.getName() + "&7!"));
        }
    }

    
    @EventHandler
    public void onPlayerReachMinimunHealth(PlayerDeathEvent event) {
        Player player = event.getEntity();
        double currentMaxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        double minHealth = 10.0; // 5 corazones

        if (currentMaxHealth <= minHealth) {
            PenalizePlayer(player);
        }
    }

    public void PenalizePlayer(Player player) {
        // Obtener el contador de baneos para este jugador
        UUID playerUUID = player.getUniqueId();
        int banCount = banCountMap.getOrDefault(playerUUID, 0) + 1;
        banCountMap.put(playerUUID, banCount);
        
        // Save the updated ban count to JSON
        saveBanData();
        
        // Calcular la duración del baneo (6 horas * número de baneos)
        long banHours = 6L * banCount;
        Date expirationDate = new Date(System.currentTimeMillis() + (banHours * 60 * 60 * 1000));
        
        // Mensaje para el jugador
        String banMessage = ColorText.ColorizeNoPrefix(
            "&c&l¡Has alcanzado el mínimo de corazones permitidos!\n\n" +
            "&fSerás baneado por &c" + banHours + " horas&f.\n" +
            "&7Este es tu baneo número &c" + banCount
        );
        
        // Notificar al jugador antes del baneo
        player.sendMessage(ColorText.Colorize("&c¡Has alcanzado el mínimo de corazones permitidos!"));
        player.sendMessage(ColorText.Colorize("&fSerás baneado por &c" + banHours + " horas&f."));
        player.sendMessage(ColorText.Colorize("&7Este es tu baneo número &c" + banCount));
        
        // Programar el baneo para ejecutarse después de un breve retraso
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Banear al jugador por nombre
            Bukkit.getBanList(BanList.Type.NAME).addBan(
                player.getName(),
                BAN_REASON,
                expirationDate,
                "VIRTHA System"
            );

            // Banear al jugador por IP
            Bukkit.getBanList(BanList.Type.IP).addBan(
                player.getAddress().getAddress().getHostAddress(),
                BAN_REASON,
                expirationDate,
                "VIRTHA System"
            );

            // Expulsar al jugador
            player.kickPlayer(banMessage);
            
            // Notificar a los administradores
            Bukkit.getConsoleSender().sendMessage(ColorText.Colorize(
                "&c[HealthSteal] &f" + player.getName() + " ha sido baneado por " + banHours + " horas " +
                "(Baneo #" + banCount + ")"
            ));
        }, 40L); // 2 segundos de retraso (40 ticks)
    }
    
    /**
     * Loads ban data from the JSON file
     */
    private void loadBanData() {
        if (!banDataFile.exists()) {
            return;
        }
        
        try (FileReader reader = new FileReader(banDataFile)) {
            JSONParser parser = new JSONParser();
            JSONObject banData = (JSONObject) parser.parse(reader);
            
            for (Object key : banData.keySet()) {
                String uuidString = (String) key;
                UUID uuid = UUID.fromString(uuidString);
                Long banCount = (Long) banData.get(uuidString);
                
                banCountMap.put(uuid, banCount.intValue());
            }
            
            plugin.getLogger().info("Ban data loaded successfully: " + banCountMap.size() + " records");
        } catch (IOException | ParseException e) {
            plugin.getLogger().severe("Error loading ban data: " + e.getMessage());
        }
    }
    
    /**
     * Saves ban data to the JSON file
     */
    @SuppressWarnings("unchecked")
    private void saveBanData() {
        JSONObject banData = new JSONObject();
        
        for (Map.Entry<UUID, Integer> entry : banCountMap.entrySet()) {
            banData.put(entry.getKey().toString(), entry.getValue());
        }
        
        try (FileWriter writer = new FileWriter(banDataFile)) {
            writer.write(banData.toJSONString());
            writer.flush();
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving ban data: " + e.getMessage());
        }
    }
}
