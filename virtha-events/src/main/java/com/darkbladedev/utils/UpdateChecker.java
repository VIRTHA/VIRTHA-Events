package com.darkbladedev.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Sistema de verificación y notificación de actualizaciones para el plugin
 * utilizando GitHub Releases
 */
public class UpdateChecker {

    private static final String DEFAULT_OWNER = "VIRTHA"; // Propietario del repositorio por defecto
    private static final String DEFAULT_REPO = "VIRTHA-Events"; // Nombre del repositorio por defecto
    private static final String API_BASE_URL = "https://api.github.com/repos/";
    
    private final Plugin plugin;
    private String latestVersion;
    private boolean updateAvailable = false;
    private String owner;
    private String repo;
    private String apiToken;
    private String releaseUrl;
    
    /**
     * Constructor del verificador de actualizaciones
     * @param plugin El plugin principal
     */
    public UpdateChecker(Plugin plugin) {
        this.plugin = plugin;
        this.owner = plugin.getConfig().getString("github.owner", DEFAULT_OWNER);
        this.repo = plugin.getConfig().getString("github.repo", DEFAULT_REPO);
        this.apiToken = plugin.getConfig().getString("github.token", "");
        this.releaseUrl = "https://github.com/" + owner + "/" + repo + "/releases";
    }
    
    /**
     * Inicia el verificador de actualizaciones
     */
    public void start() {
        if (!ConfigManager.isUpdateCheckerEnabled()) {
            return;
        }
        
        // Verificar actualizaciones al iniciar y luego cada 6 horas
        checkForUpdates();
        
        // Obtener el intervalo de verificación desde la configuración
        int checkInterval = plugin.getConfig().getInt("update-check-interval", 6);
        
        // Programar verificaciones periódicas
        new BukkitRunnable() {
            @Override
            public void run() {
                checkForUpdates();
            }
        }.runTaskTimerAsynchronously(plugin, 20L * 60 * 60 * checkInterval, 20L * 60 * 60 * checkInterval);
    }
    
    /**
     * Verifica si hay actualizaciones disponibles usando la API de GitHub
     */
    public void checkForUpdates() {
        CompletableFuture.runAsync(() -> {
            try {
                String apiUrl = API_BASE_URL + owner + "/" + repo + "/releases/latest";
                HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                // Añadir token de autenticación si está disponible para evitar límites de tasa
                if (apiToken != null && !apiToken.isEmpty()) {
                    connection.setRequestProperty("Authorization", "token " + apiToken);
                }
                
                // Añadir User-Agent para cumplir con los requisitos de la API de GitHub
                connection.setRequestProperty("User-Agent", "VIRTHA-Events-UpdateChecker");
                
                // Procesar la respuesta JSON
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    JSONParser parser = new JSONParser();
                    JSONObject releaseInfo = (JSONObject) parser.parse(reader);
                    
                    // Extraer la versión del tag_name (por ejemplo, "v1.0.0" -> "1.0.0")
                    String tagName = (String) releaseInfo.get("tag_name");
                    latestVersion = tagName.startsWith("v") ? tagName.substring(1) : tagName;
                    
                    String currentVersion = plugin.getDescription().getVersion();
                    updateAvailable = !currentVersion.equals(latestVersion);
                    
                    if (updateAvailable && ConfigManager.isDebugEnabled()) {
                        Bukkit.getConsoleSender().sendMessage(
                            ColorText.Colorize("&6[VIRTHA Events] &aActualización disponible: &e" + latestVersion + 
                                              " &a(Versión actual: &e" + currentVersion + "&a)")
                        );
                    }
                }
            } catch (IOException e) {
                if (ConfigManager.isDebugEnabled()) {
                    Bukkit.getConsoleSender().sendMessage(
                        ColorText.Colorize("&6[VIRTHA Events] &cError al verificar actualizaciones: " + e.getMessage())
                    );
                }
            } catch (ParseException e) {
                if (ConfigManager.isDebugEnabled()) {
                    Bukkit.getConsoleSender().sendMessage(
                        ColorText.Colorize("&6[VIRTHA Events] &cError al procesar la respuesta JSON: " + e.getMessage())
                    );
                }
            }
        });
    }
    
    /**
     * Notifica a un jugador si hay una actualización disponible
     * @param player El jugador a notificar
     */
    public void notifyPlayer(Player player) {
        if (!ConfigManager.isUpdateNotifierEnabled() || !updateAvailable || !player.hasPermission("virthaevents.admin.update")) {
            return;
        }
        
        String message = ConfigManager.getUpdateNotifierMessage()
                .replace("{latest}", latestVersion)
                .replace("{link}", releaseUrl);
        
        player.sendMessage(ColorText.Colorize(message));
    }
    
    /**
     * Verifica si hay una actualización disponible
     * @return true si hay una actualización disponible, false en caso contrario
     */
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }
    
    /**
     * Obtiene la última versión disponible
     * @return La última versión disponible
     */
    public String getLatestVersion() {
        return latestVersion;
    }
}