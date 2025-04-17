package com.darkbladedev.mechanics;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import com.darkbladedev.utils.ColorText;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class BanManager implements Listener {

    /**
     * Handles player login attempts and provides ban time information
     */
    @SuppressWarnings("rawtypes")
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        // Check if the player is banned
        if (event.getResult() == Result.KICK_BANNED) {
            // Get ban entry from the name ban list
            BanList banList = Bukkit.getBanList(BanList.Type.NAME);
            BanList ipBanList = Bukkit.getBanList(BanList.Type.IP);
            
            String playerName = event.getPlayer().getName();
            String playerIP = event.getAddress().getHostAddress();
            
            // Check if player is banned by name
            if (banList.isBanned(playerName)) {
                Date expiration = banList.getBanEntry(playerName).getExpiration();
                
                // If the ban has an expiration date
                if (expiration != null) {
                    // Calculate remaining time
                    long remainingMillis = expiration.getTime() - System.currentTimeMillis();
                    
                    // Only process if there's time remaining
                    if (remainingMillis > 0) {
                        String formattedTime = formatRemainingTime(remainingMillis);
                        String reason = banList.getBanEntry(playerName).getReason();
                        
                        // Create a custom ban message with remaining time
                        String banMessage = ColorText.ColorizeNoPrefix(
                            "&c&l¡ESTÁS BANEADO!\n\n" +
                            "&fRazón: &e" + (reason != null ? reason : "No especificada") + "\n" +
                            "&fTiempo restante: &e" + formattedTime + "\n\n" +
                            "&7Si crees que esto es un error, contacta a un administrador."
                        );
                        
                        // Set the kick message
                        event.setKickMessage(banMessage);
                    }
                }
            }
            // Check if player is banned by IP
            else if (ipBanList.isBanned(playerIP)) {
                Date expiration = ipBanList.getBanEntry(playerIP).getExpiration();
                
                // If the ban has an expiration date
                if (expiration != null) {
                    // Calculate remaining time
                    long remainingMillis = expiration.getTime() - System.currentTimeMillis();
                    
                    // Only process if there's time remaining
                    if (remainingMillis > 0) {
                        String formattedTime = formatRemainingTime(remainingMillis);
                        String reason = ipBanList.getBanEntry(playerIP).getReason();
                        
                        // Create a custom ban message with remaining time
                        String banMessage = ColorText.ColorizeNoPrefix(
                            "&c&l¡ESTÁS BANEADO!\n\n" +
                            "&fRazón: &e" + (reason != null ? reason : "No especificada") + "\n" +
                            "&fTiempo restante: &e" + formattedTime + "\n\n" +
                            "&7Si crees que esto es un error, contacta a un administrador."
                        );
                        
                        // Set the kick message
                        event.setKickMessage(banMessage);
                    }
                }
            }
        }
    }
    
    /**
     * Formats milliseconds into a human-readable time format
     * @param millis Time in milliseconds
     * @return Formatted time string (e.g., "2 días, 5 horas, 30 minutos")
     */
    private String formatRemainingTime(long millis) {
        if (millis <= 0) {
            return "0 minutos";
        }
        
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        
        StringBuilder sb = new StringBuilder();
        
        if (days > 0) {
            sb.append(days).append(days == 1 ? " día" : " días");
        }
        
        if (hours > 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(hours).append(hours == 1 ? " hora" : " horas");
        }
        
        if (minutes > 0 || (days == 0 && hours == 0)) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(minutes).append(minutes == 1 ? " minuto" : " minutos");
        }
        
        return sb.toString();
    }
}