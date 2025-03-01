package com.darkbladedev.mechanics;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.projectiles.ProjectileSource;

import com.darkbladedev.utils.ColorText;

public class HealthSteal implements Listener{


    public void onPlayerKill(PlayerDeathEvent event) {

        Player deadPlayer = event.getEntity();
        EntityDamageEvent lastDamage = deadPlayer.getLastDamageCause();

        if (!(lastDamage instanceof EntityDamageByEntityEvent)) return;

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
        double newMaxHealth = killer.getAttribute(Attribute.MAX_HEALTH).getValue() + healthToSteal;

        // Limitar salud máxima si es necesario
        if (newMaxHealth > 40.0) newMaxHealth = 40.0;

        killer.getAttribute(Attribute.MAX_HEALTH).setBaseValue(newMaxHealth);
        killer.setHealth(Math.min(killer.getHealth() + healthToSteal, newMaxHealth));

        // Mensaje al jugador
        if (killer instanceof Player) {
            ((Player) killer).sendMessage(ColorText.Colorize("&7¡&aRobaste 1 corazón de &3" + deadPlayer.getName() + "&7!"));
        }
    }
}
