package com.darkbladedev.mechanics;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.darkbladedev.data.EventType;
import com.darkbladedev.utils.ColorText;

public class WeeklyEventManager {
    private static final long WEEK_IN_MILLIS = TimeUnit.DAYS.toMillis(7);
    private static final String DATA_FILE = "weekly_event_data.json";
    
    private final Plugin plugin;
    private final Random random = new Random();
    private final File dataFile;
    
    private BukkitTask weeklyTask;
    private long eventStartTime;
    private long eventEndTime;
    private EventType currentEventType;
    private Object currentEvent;
    private boolean isEventActive = false;
    private boolean isPaused = false;
    private long pauseStartTime = 0;
    private long totalPausedTime = 0;
    
    public WeeklyEventManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), DATA_FILE);
        
        // Crear directorio si no existe
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
    }
    
    public void initialize() {
        // Cargar datos guardados o iniciar un nuevo evento
        if (loadSavedEventData()) {
            Bukkit.getConsoleSender().sendMessage(
                ColorText.Colorize("&6Reanudando evento semanal: &e" + currentEventType.getEventName())
            );
            
            // Calcular tiempo restante
            long currentTime = System.currentTimeMillis();
            long remainingTime = eventEndTime - currentTime;
            
            if (remainingTime > 0) {
                // Reanudar el evento actual
                startEvent(currentEventType, remainingTime);
                
                // Programar el siguiente evento cuando termine este
                scheduleNextEvent(remainingTime);
            } else {
                // Si el evento ya debería haber terminado, iniciar uno nuevo
                startRandomEvent();
            }
        } else {
            // No hay datos guardados, iniciar un nuevo evento aleatorio
            startRandomEvent();
        }
    }
    
    private void startRandomEvent() {
        // Obtener lista de eventos disponibles
        List<EventType> availableEvents = new ArrayList<>();
        for (EventType type : EventType.values()) {
            // Filtrar eventos que no son adecuados para ser semanales
            if (!type.getEventName().equals("mob_rain")) { // Excluir eventos instantáneos
                availableEvents.add(type);
            }
        }
        
        // Seleccionar un evento aleatorio
        if (!availableEvents.isEmpty()) {
            EventType selectedEvent = availableEvents.get(random.nextInt(availableEvents.size()));
            
            // Iniciar el evento seleccionado por una semana
            startEvent(selectedEvent, WEEK_IN_MILLIS);
            
            // Programar el siguiente evento
            scheduleNextEvent(WEEK_IN_MILLIS);
        }
    }
    
    private void startEvent(EventType eventType, long duration) {
        // Guardar información del evento actual
        currentEventType = eventType;
        eventStartTime = System.currentTimeMillis();
        eventEndTime = eventStartTime + duration;
        isEventActive = true;
        
        // Convertir duración a ticks (para Bukkit)
        long durationTicks = TimeUnit.MILLISECONDS.toSeconds(duration);
        
        // Iniciar el evento según su tipo
        switch (eventType.getEventName()) {
            case "size_randomizer":
                SizeRandomizer sizeRandomizer = new SizeRandomizer(plugin, durationTicks, 0.5f, 2.0f);
                sizeRandomizer.start();
                currentEvent = sizeRandomizer;
                break;
                
            case "acid_week":
                AcidWeek acidWeek = new AcidWeek(plugin, durationTicks);
                acidWeek.start();
                currentEvent = acidWeek;
                break;
                
            case "toxic_fog":
                ToxicFog toxicFog = new ToxicFog(plugin, durationTicks);
                toxicFog.start();
                currentEvent = toxicFog;
                break;
                
            case "undead_week":
                UndeadWeek undeadWeek = new UndeadWeek(plugin, durationTicks);
                undeadWeek.start();
                currentEvent = undeadWeek;
                break;
                
            case "paranoia_effect":
                ParanoiaEffect paranoiaEffect = new ParanoiaEffect(plugin, durationTicks, 5);
                paranoiaEffect.start();
                currentEvent = paranoiaEffect;
                break;
                
            case "explosive_week":
                ExplosiveWeek explosiveWeek = new ExplosiveWeek(plugin, durationTicks);
                explosiveWeek.start();
                currentEvent = explosiveWeek;
                break;
                
            case "blood_and_iron_week":
                BloodAndIronWeek bloodAndIronWeek = new BloodAndIronWeek(plugin, durationTicks);
                bloodAndIronWeek.start();
                currentEvent = bloodAndIronWeek;
                break;
                
            default:
                Bukkit.getConsoleSender().sendMessage(
                    ColorText.Colorize("&cEvento no implementado para ejecución semanal: " + eventType.getEventName())
                );
                isEventActive = false;
                return;
        }
        
        // Anunciar el inicio del evento
        Bukkit.broadcastMessage(ColorText.Colorize("&6&l¡EVENTO SEMANAL INICIADO!"));
        Bukkit.broadcastMessage(ColorText.Colorize("&e" + getEventDisplayName(eventType) + " &6estará activo durante 7 días."));
        
        // Guardar datos del evento
        saveEventData();
    }
    
    private String getEventDisplayName(EventType eventType) {
        switch (eventType.getEventName()) {
            case "size_randomizer": return "Tamaños Aleatorios";
            case "acid_week": return "Semana Ácida";
            case "toxic_fog": return "Niebla Tóxica";
            case "undead_week": return "Semana de No-Muertos";
            case "paranoia_effect": return "Paranoia";
            case "explosive_week": return "Semana Explosiva";
            case "blood_and_iron_week": return "Semana de Sangre y Hierro";
            default: return eventType.getEventName();
        }
    }
    
    private void scheduleNextEvent(long delay) {
        // Cancelar tarea anterior si existe
        if (weeklyTask != null) {
            weeklyTask.cancel();
        }
        
        // Adjust delay if the event is paused
        long adjustedDelay = delay;
        if (isPaused) {
            // If paused, we need to account for the time already paused
            adjustedDelay += System.currentTimeMillis() - pauseStartTime;
        }
        
        // Programar el próximo evento
        weeklyTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Finalizar evento actual si es necesario
                stopCurrentEvent();
                
                // Iniciar nuevo evento aleatorio
                startRandomEvent();
            }
        }.runTaskLater(plugin, TimeUnit.MILLISECONDS.toSeconds(adjustedDelay) * 20); // Convertir a ticks
    }
    
    public void stopCurrentEvent() {
        if (!isEventActive || currentEvent == null) {
            return;
        }
        
        // Detener el evento según su tipo
        if (currentEvent instanceof SizeRandomizer) {
            ((SizeRandomizer) currentEvent).stop();
        } else if (currentEvent instanceof AcidWeek) {
            ((AcidWeek) currentEvent).stop();
        } else if (currentEvent instanceof ToxicFog) {
            ((ToxicFog) currentEvent).stop();
        } else if (currentEvent instanceof UndeadWeek) {
            ((UndeadWeek) currentEvent).stop();
        } else if (currentEvent instanceof ParanoiaEffect) {
            ((ParanoiaEffect) currentEvent).stop();
        } else if (currentEvent instanceof ExplosiveWeek) {
            ((ExplosiveWeek) currentEvent).stop();
        } else if (currentEvent instanceof BloodAndIronWeek) {
            ((BloodAndIronWeek) currentEvent).stop();
        }
        
        isEventActive = false;
        currentEvent = null;
        
        // Anunciar fin del evento
        Bukkit.broadcastMessage(ColorText.Colorize("&6&l¡EVENTO SEMANAL FINALIZADO!"));
        Bukkit.broadcastMessage(ColorText.Colorize("&eEl próximo evento comenzará pronto..."));
    }
    
    @SuppressWarnings("unchecked")
    private void saveEventData() {
        JSONObject data = new JSONObject();
        data.put("eventType", currentEventType.getEventName());
        data.put("startTime", eventStartTime);
        data.put("endTime", eventEndTime);
        data.put("isPaused", isPaused);
        data.put("pauseStartTime", pauseStartTime);
        data.put("totalPausedTime", totalPausedTime);
        
        try (FileWriter writer = new FileWriter(dataFile)) {
            writer.write(data.toJSONString());
            writer.flush();
        } catch (IOException e) {
            plugin.getLogger().severe("Error al guardar datos del evento semanal: " + e.getMessage());
        }
    }
    
    private boolean loadSavedEventData() {
        if (!dataFile.exists()) {
            return false;
        }
        
        try {
            JSONParser parser = new JSONParser();
            JSONObject data = (JSONObject) parser.parse(new FileReader(dataFile));
            
            String eventTypeName = (String) data.get("eventType");
            eventStartTime = (Long) data.get("startTime");
            eventEndTime = (Long) data.get("endTime");
            
            // Load pause state if available
            if (data.containsKey("isPaused")) {
                isPaused = (Boolean) data.get("isPaused");
            }
            
            if (data.containsKey("pauseStartTime")) {
                pauseStartTime = (Long) data.get("pauseStartTime");
            }
            
            if (data.containsKey("totalPausedTime")) {
                totalPausedTime = (Long) data.get("totalPausedTime");
            }
            
            currentEventType = EventType.getByName(eventTypeName);
            
            return currentEventType != null;
        } catch (IOException | ParseException e) {
            plugin.getLogger().severe("Error al cargar datos del evento semanal: " + e.getMessage());
            return false;
        }
    }
    
    public void shutdown() {
        // Guardar el estado actual antes de apagar
        if (isEventActive) {
            saveEventData();
        }
        
        // Cancelar tareas programadas
        if (weeklyTask != null) {
            weeklyTask.cancel();
        }
    }
    
    public boolean isEventActive() {
        return isEventActive;
    }
    
    public EventType getCurrentEventType() {
        return currentEventType;
    }
    
    public void pauseCurrentEvent() {
        if (!isEventActive || isPaused || currentEvent == null) {
            return;
        }
        
        isPaused = true;
        pauseStartTime = System.currentTimeMillis();
        
        // Pause the event based on its type
        if (currentEvent instanceof SizeRandomizer) {
            ((SizeRandomizer) currentEvent).pause();
        } else if (currentEvent instanceof AcidWeek) {
            ((AcidWeek) currentEvent).pause();
        } else if (currentEvent instanceof ToxicFog) {
            ((ToxicFog) currentEvent).pause();
        } else if (currentEvent instanceof UndeadWeek) {
            ((UndeadWeek) currentEvent).pause();
        } else if (currentEvent instanceof ParanoiaEffect) {
            ((ParanoiaEffect) currentEvent).pause();
        } else if (currentEvent instanceof ExplosiveWeek) {
            ((ExplosiveWeek) currentEvent).pause();
        } else if (currentEvent instanceof BloodAndIronWeek) {
            ((BloodAndIronWeek) currentEvent).pause();
        }
        
        // Announce event pause
        Bukkit.broadcastMessage(ColorText.Colorize("&6&l¡EVENTO SEMANAL PAUSADO!"));
        Bukkit.broadcastMessage(ColorText.Colorize("&eEl evento se reanudará cuando un administrador lo indique."));
    }
    
    public void resumeCurrentEvent() {
        if (!isEventActive || !isPaused || currentEvent == null) {
            return;
        }
        
        // Calculate how long the event was paused
        long currentTime = System.currentTimeMillis();
        long pauseDuration = currentTime - pauseStartTime;
        totalPausedTime += pauseDuration;
        
        // Adjust the end time to account for the pause
        eventEndTime += pauseDuration;
        
        isPaused = false;
        
        // Resume the event based on its type
        if (currentEvent instanceof SizeRandomizer) {
            ((SizeRandomizer) currentEvent).resume();
        } else if (currentEvent instanceof AcidWeek) {
            ((AcidWeek) currentEvent).resume();
        } else if (currentEvent instanceof ToxicFog) {
            ((ToxicFog) currentEvent).resume();
        } else if (currentEvent instanceof UndeadWeek) {
            ((UndeadWeek) currentEvent).resume();
        } else if (currentEvent instanceof ParanoiaEffect) {
            ((ParanoiaEffect) currentEvent).resume();
        } else if (currentEvent instanceof ExplosiveWeek) {
            ((ExplosiveWeek) currentEvent).resume();
        } else if (currentEvent instanceof BloodAndIronWeek) {
            ((BloodAndIronWeek) currentEvent).resume();
        }
        
        // Announce event resume
        Bukkit.broadcastMessage(ColorText.Colorize("&6&l¡EVENTO SEMANAL REANUDADO!"));
        Bukkit.broadcastMessage(ColorText.Colorize("&eEl evento continuará por el tiempo restante."));
    }
    
    public void forceStopCurrentEvent() {
        if (!isEventActive || currentEvent == null) {
            return;
        }
        
        // Stop the current event
        stopCurrentEvent();
        
        // Cancel the next scheduled event
        if (weeklyTask != null) {
            weeklyTask.cancel();
            weeklyTask = null;
        }
        
        // Announce forced stop
        Bukkit.broadcastMessage(ColorText.Colorize("&c&l¡EVENTO SEMANAL DETENIDO FORZOSAMENTE!"));
        Bukkit.broadcastMessage(ColorText.Colorize("&eEl próximo evento semanal se programará automáticamente."));
        
        // Schedule a new random event to start after a delay (1 hour)
        weeklyTask = new BukkitRunnable() {
            @Override
            public void run() {
                startRandomEvent();
            }
        }.runTaskLater(plugin, 20 * 60 * 60); // 1 hour delay
    }
    
    // Update getTimeRemaining to account for paused time
    public long getTimeRemaining() {
        if (!isEventActive) {
            return 0;
        }
        
        if (isPaused) {
            return eventEndTime - pauseStartTime;
        }
        
        return Math.max(0, eventEndTime - System.currentTimeMillis());
    }
    
    public boolean isPaused() {
        return isPaused;
    }
}