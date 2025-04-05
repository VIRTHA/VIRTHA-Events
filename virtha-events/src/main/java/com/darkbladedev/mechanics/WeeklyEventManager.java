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
            default: return eventType.getEventName();
        }
    }
    
    private void scheduleNextEvent(long delay) {
        // Cancelar tarea anterior si existe
        if (weeklyTask != null) {
            weeklyTask.cancel();
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
        }.runTaskLater(plugin, TimeUnit.MILLISECONDS.toSeconds(delay) * 20); // Convertir a ticks
    }
    
    public void stopCurrentEvent() {
        if (!isEventActive || currentEvent == null) {
            return;
        }
        
        // Detener el evento según su tipo
        if (currentEvent instanceof SizeRandomizer) {
            // No es necesario detenerlo explícitamente, ya tiene su propio temporizador
        } else if (currentEvent instanceof AcidWeek) {
            ((AcidWeek) currentEvent).stop();
        } else if (currentEvent instanceof ToxicFog) {
            ((ToxicFog) currentEvent).stop();
        } else if (currentEvent instanceof UndeadWeek) {
            ((UndeadWeek) currentEvent).stop();
        } else if (currentEvent instanceof ParanoiaEffect) {
            ((ParanoiaEffect) currentEvent).stop();
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
    
    public long getTimeRemaining() {
        if (!isEventActive) {
            return 0;
        }
        return Math.max(0, eventEndTime - System.currentTimeMillis());
    }
}