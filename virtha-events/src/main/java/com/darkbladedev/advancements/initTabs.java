package com.darkbladedev.advancements;

import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.Bukkit;

import com.darkbladedev.advancements.advs.AdvancementTabNamespaces;
import com.darkbladedev.advancements.advs.heartless_init.Week_survive;
import com.darkbladedev.advancements.advs.heartless_undead_week.Survive_10_week;
import com.darkbladedev.advancements.advs.heartless_undead_week.Survive_1_night;
import com.darkbladedev.advancements.advs.heartless_undead_week.Survive_1_week;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.exceptions.APINotInstantiatedException;

public class initTabs {
    public static UltimateAdvancementAPI api;
    public AdvancementTab heartless_init;
    public AdvancementTab heartless_undead_week;
    private boolean apiInitialized = false;

    /**
     * Inicializa las pestañas de avances si UltimateAdvancementAPI está disponible
     * @param plugin El plugin principal
     * @return true si la inicialización fue exitosa, false en caso contrario
     */
    public boolean initializeTabs(Plugin plugin) {
        try {
            // Verificar si el plugin UltimateAdvancementAPI está presente y habilitado
            PluginManager pluginManager = Bukkit.getPluginManager();
            Plugin uaPlugin = pluginManager.getPlugin("UltimateAdvancementAPI");
            
            if (uaPlugin == null || !uaPlugin.isEnabled()) {
                plugin.getLogger().severe("UltimateAdvancementAPI no está instalado o habilitado. Los avances no estarán disponibles.");
                return false;
            }
            
            // Intentar obtener la instancia de la API
            api = UltimateAdvancementAPI.getInstance(plugin);
            
            if (api == null) {
                plugin.getLogger().severe("No se pudo obtener la instancia de UltimateAdvancementAPI.");
                return false;
            }
            
            // Crear las pestañas de avances
            heartless_init = api.createAdvancementTab(AdvancementTabNamespaces.heartless_init_NAMESPACE);
            heartless_undead_week = api.createAdvancementTab(AdvancementTabNamespaces.heartless_undead_week_NAMESPACE);
            
            // Registrar los avances
            RootAdvancement init = new RootAdvancement(heartless_init, "init", new AdvancementDisplay(Material.NETHER_PORTAL, "§aInicio en Heartless", AdvancementFrameType.CHALLENGE, true, false, 0f, 0f , "§fDa comienzo a tu aventura", "§fcaótica en §cHeartless§f!"),"textures/block/stripped_spruce_log.png",1);
            Week_survive week_survive = new Week_survive(init);
            heartless_init.registerAdvancements(init, week_survive);
            
            RootAdvancement discover_undead_week = new RootAdvancement(heartless_undead_week, "discover_undead_week", new AdvancementDisplay(Material.ZOMBIE_HEAD, "§d§lSemana de los no-muertos", AdvancementFrameType.CHALLENGE, true, false, 0f, 0f , "§fDescubre la semana", "§fde los no-muertos"),"textures/block/netherrack.png",1);
            Survive_1_night survive_1_night = new Survive_1_night(discover_undead_week);
            Survive_1_week survive_1_week = new Survive_1_week(discover_undead_week);
            Survive_10_week survive_10_week = new Survive_10_week(discover_undead_week);
            heartless_undead_week.registerAdvancements(discover_undead_week, survive_1_night, survive_1_week, survive_10_week);
            
            apiInitialized = true;
            plugin.getLogger().info("UltimateAdvancementAPI inicializado correctamente.");
            return true;
        } catch (APINotInstantiatedException e) {
            plugin.getLogger().severe("Error al inicializar UltimateAdvancementAPI: La API no está instanciada correctamente.");
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            plugin.getLogger().severe("Error inesperado al inicializar UltimateAdvancementAPI: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Verifica si la API fue inicializada correctamente
     * @return true si la API está inicializada, false en caso contrario
     */
    public boolean isApiInitialized() {
        return apiInitialized;
    }
}