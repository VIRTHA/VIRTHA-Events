package com.darkbladedev.placeholders;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.darkbladedev.VirthaEventsMain;
import com.darkbladedev.mechanics.AcidWeek;
import com.darkbladedev.mechanics.BloodAndIronWeek;
import com.darkbladedev.mechanics.ExplosiveWeek;
import com.darkbladedev.mechanics.UndeadWeek;
import com.darkbladedev.mechanics.WeeklyEventManager;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class VirthaEventsExpansion extends PlaceholderExpansion {

    private final VirthaEventsMain plugin;

    public VirthaEventsExpansion(VirthaEventsMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getAuthor() {
        return "DarkBladeDev";
    }

    @Override
    public String getIdentifier() {
        return "virtha";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    /**
     * We want the placeholders to update frequently to reflect event changes
     */
    public long getLastUpdate() {
        // Return current time in milliseconds to prevent caching
        return System.currentTimeMillis();
    }

    /**
     * Refreshes all placeholder values to ensure they reflect current event state
     * This can be called when events change or when manual refresh is needed
     */
    public void refreshPlaceholders() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            try {                
                // Force update for all online players
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Get all virtha placeholders and re-process them
                    String[] placeholders = {
                        "%virtha_event_active%",
                        "%virtha_event_paused%",
                        "%virtha_event_name%",
                        "%virtha_event_time_remaining%",
                        "%virtha_event_time_remaining_short%",
                        "%virtha_event_progress_percent%"
                    };
                    
                    // Process each placeholder to force refresh
                    me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, String.join(" ", placeholders));
                }
                
                // Log the refresh for debugging purposes
                plugin.getLogger().info("VirthaEvents placeholders have been refreshed");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to refresh placeholders: " + e.getMessage());
            }
        }
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        refreshPlaceholders();

        WeeklyEventManager eventManager = plugin.getWeeklyEventManager();
        
        // Basic event information
        if (identifier.equals("event_active")) {
            return eventManager.isEventActive() ? "Sí" : "No";
        }
        
        if (identifier.equals("event_paused")) {
            return eventManager.isPaused() ? "Sí" : "No";
        }

        if (identifier.equals("event_name")) {
            if (!eventManager.isEventActive() || eventManager.getCurrentEventType() == null) {
                return "Ninguno";
            }
            return getDisplayName(eventManager.getCurrentEventType().getEventName());
        }
        
        // Time remaining
        if (identifier.equals("event_time_remaining")) {
            if (!eventManager.isEventActive()) {
                return "No hay evento activo";
            }
            
            long timeRemaining = eventManager.getTimeRemaining();
            return formatTimeRemaining(timeRemaining);
        }
        
        if (identifier.equals("event_time_remaining_short")) {
            if (!eventManager.isEventActive()) {
                return "N/A";
            }
            
            long timeRemaining = eventManager.getTimeRemaining();
            return formatTimeRemainingShort(timeRemaining);
        }
        
        if (identifier.equals("event_progress_percent")) {
            if (!eventManager.isEventActive()) {
                return "0%";
            }
            
            long totalDuration = TimeUnit.DAYS.toMillis(7);
            long timeElapsed = totalDuration - eventManager.getTimeRemaining();
            int progressPercent = (int) ((timeElapsed * 100) / totalDuration);
            
            return progressPercent + "%";
        }
        
        // Event-specific placeholders
        if (identifier.startsWith("event_specific_")) {
            if (!eventManager.isEventActive() || eventManager.getCurrentEventType() == null) {
                return "N/A";
            }
            
            String eventName = eventManager.getCurrentEventType().getEventName();
            String specificIdentifier = identifier.substring("event_specific_".length());
            
            return getEventSpecificPlaceholder(player, eventName, specificIdentifier);
        }
        
        // Challenge completion placeholders
        if (identifier.startsWith("challenge_completed_")) {
            if (!eventManager.isEventActive() || eventManager.getCurrentEventType() == null) {
                return "false";
            }
            
            String eventName = eventManager.getCurrentEventType().getEventName();
            String challengeId = identifier.substring("challenge_completed_".length());
            
            return isChallengeCompleted(player, eventName, challengeId) ? "true" : "false";
        }
        
        return null; // Placeholder not found
    }
    
    private String getDisplayName(String eventName) {
        switch (eventName) {
            case "size_randomizer": return "Tamaños Aleatorios";
            case "acid_week": return "Semana Ácida";
            case "toxic_fog": return "Niebla Tóxica";
            case "undead_week": return "Semana de No-Muertos";
            case "paranoia_effect": return "Paranoia";
            case "explosive_week": return "Semana Explosiva";
            case "blood_and_iron_week": return "Semana de Sangre y Hierro";
            default: return eventName;
        }
    }
    
    private String formatTimeRemaining(long timeMillis) {
        if (timeMillis <= 0) {
            return "Finalizado";
        }
        
        long days = TimeUnit.MILLISECONDS.toDays(timeMillis);
        timeMillis -= TimeUnit.DAYS.toMillis(days);
        
        long hours = TimeUnit.MILLISECONDS.toHours(timeMillis);
        timeMillis -= TimeUnit.HOURS.toMillis(hours);
        
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeMillis);
        timeMillis -= TimeUnit.MINUTES.toMillis(minutes);
        
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis);
        
        StringBuilder result = new StringBuilder();
        
        if (days > 0) {
            result.append(days).append(days == 1 ? " día" : " días");
            if (hours > 0 || minutes > 0) {
                result.append(", ");
            }
        }
        
        if (hours > 0) {
            result.append(hours).append(hours == 1 ? " hora" : " horas");
            if (minutes > 0) {
                result.append(", ");
            }
        }
        
        if (minutes > 0 || (days == 0 && hours == 0)) {
            result.append(minutes).append(minutes == 1 ? " minuto" : " minutos");
            if (seconds > 0 && days == 0 && hours == 0) {
                result.append(", ");
                result.append(seconds).append(seconds == 1 ? " segundo" : " segundos");
            }
        }
        
        return result.toString();
    }
    
    private String formatTimeRemainingShort(long timeMillis) {
        if (timeMillis <= 0) {
            return "0d 0h";
        }
        
        long days = TimeUnit.MILLISECONDS.toDays(timeMillis);
        timeMillis -= TimeUnit.DAYS.toMillis(days);
        
        long hours = TimeUnit.MILLISECONDS.toHours(timeMillis);
        timeMillis -= TimeUnit.HOURS.toMillis(hours);
        
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeMillis);
        
        if (days > 0) {
            return days + "d " + hours + "h";
        } else if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }
    
    private String getEventSpecificPlaceholder(Player player, String eventName, String identifier) {
        // Implementar placeholders específicos para cada tipo de evento
        switch (eventName) {
            case "undead_week":
                return getUndeadWeekPlaceholder(player, identifier);
            case "acid_week":
                return getAcidWeekPlaceholder(player, identifier);
            case "explosive_week":
                return getExplosiveWeekPlaceholder(player, identifier);
            case "blood_and_iron_week":
                return getBloodAndIronWeekPlaceholder(player, identifier);
            // Añadir más casos según sea necesario
            default:
                return "N/A";
        }
    }
    
    private String getUndeadWeekPlaceholder(Player player, String identifier) {
        // Ejemplo: podríamos obtener datos del UndeadWeek para este jugador
        // Esto requeriría modificar UndeadWeek para exponer estos datos
        return "N/A";
    }
    
    private String getAcidWeekPlaceholder(Player player, String identifier) {
        // Ejemplo: podríamos obtener datos del AcidWeek para este jugador
        return "N/A";
    }
    
    private String getExplosiveWeekPlaceholder(Player player, String identifier) {
        // Placeholders específicos para Explosive Week
        if (identifier.equals("challenges_completed")) {
            // Aquí podríamos obtener cuántos desafíos ha completado el jugador
            // Esto requeriría modificar ExplosiveWeek para exponer estos datos
            return "N/A";
        }
        return "N/A";
    }
    
    private String getBloodAndIronWeekPlaceholder(Player player, String identifier) {
        // Placeholders específicos para Blood and Iron Week
        if (identifier.equals("player_kills")) {
            // Aquí podríamos obtener cuántos jugadores ha matado
            // Esto requeriría modificar BloodAndIronWeek para exponer estos datos
            return "N/A";
        } else if (identifier.equals("consecutive_kills")) {
            // Aquí podríamos obtener cuántas kills consecutivas tiene
            return "N/A";
        }
        return "N/A";
    }

    /**
     * Checks if a player has completed a specific challenge in the current event
     * @param player The player to check
     * @param eventName The name of the event
     * @param challengeId The ID of the challenge
     * @return true if the challenge is completed, false otherwise
     */
    private boolean isChallengeCompleted(Player player, String eventName, String challengeId) {
        Object currentEvent = plugin.getWeeklyEventManager().getCurrentEvent();
        
        switch (eventName) {
            case "blood_and_iron_week":
                if (currentEvent instanceof BloodAndIronWeek) {
                    BloodAndIronWeek bloodAndIronWeek = (BloodAndIronWeek) currentEvent;
                    return bloodAndIronWeek.hasChallengeCompleted(player.getUniqueId(), challengeId);
                }
                break;
                
            case "explosive_week":
                if (currentEvent instanceof ExplosiveWeek) {
                    ExplosiveWeek explosiveWeek = (ExplosiveWeek) currentEvent;
                    return explosiveWeek.hasChallengeCompleted(player.getUniqueId(), challengeId);
                }
                break;
                
            case "undead_week":
                if (currentEvent instanceof UndeadWeek) {
                    UndeadWeek undeadWeek = (UndeadWeek) currentEvent;
                    return undeadWeek.hasChallengeCompleted(player.getUniqueId(), challengeId);
                }
                break;
                
            case "acid_week":
                if (currentEvent instanceof AcidWeek) {
                    AcidWeek acidWeek = (AcidWeek) currentEvent;
                    return acidWeek.hasChallengeCompleted(player.getUniqueId(), challengeId);
                }
                break;
                
            // Add other events as needed
        }
        
        return false;
    }
}