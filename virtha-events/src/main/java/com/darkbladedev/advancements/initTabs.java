package com.darkbladedev.advancements;

import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import org.bukkit.Bukkit;

import com.darkbladedev.advancements.advs.AdvancementTabNamespaces;
import com.darkbladedev.advancements.advs.init.First_death;
import com.darkbladedev.advancements.advs.init.First_kill;
import com.darkbladedev.advancements.advs.undead_week_tab.First_zombification;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.database.impl.SQLite;
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingCompletedEvent;
import com.fren_gor.ultimateAdvancementAPI.exceptions.APINotInstantiatedException;

public class initTabs {
    private AdvancementMain main;
    private final Plugin plugin;
    public static UltimateAdvancementAPI api;
    public AdvancementTab init;
    public AdvancementTab undead_week_tab;
    private boolean apiInitialized = false;

    public initTabs(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Inicializa las pestañas de avances si UltimateAdvancementAPI está disponible
     * @param plugin El plugin principal
     * @return true si la inicialización fue exitosa, false en caso contrario
     */
    public boolean initializeTabs() {
        try {
            // Verificar si el plugin UltimateAdvancementAPI está presente y habilitado
            PluginManager pluginManager = Bukkit.getPluginManager();
            Plugin uaPlugin = pluginManager.getPlugin("UltimateAdvancementAPI");
            
            if (uaPlugin == null || !uaPlugin.isEnabled()) {
                plugin.getLogger().severe("UltimateAdvancementAPI no está instalado o habilitado. Los avances no estarán disponibles.");
                return false;
            }
            
            main = new AdvancementMain(plugin);
            main.load();
            main.enable(() -> new SQLite(main, new File(plugin.getDataFolder(), "advancements.db")));

            // After the initialisation of AdvancementMain you can get an instance of the UltimateAdvancementAPI class
            api = UltimateAdvancementAPI.getInstance(plugin);
            
            if (api == null) {
                plugin.getLogger().severe("No se pudo obtener la instancia de UltimateAdvancementAPI.");
                return false;
            }

            init = api.createAdvancementTab(AdvancementTabNamespaces.init_NAMESPACE);
            undead_week_tab = api.createAdvancementTab(AdvancementTabNamespaces.undead_week_tab_NAMESPACE);

            RootAdvancement welcome = new RootAdvancement(init, "welcome", new AdvancementDisplay(Material.GRASS_BLOCK, "Bienvenido a §cHeartless", AdvancementFrameType.TASK, true, true, 0f, 0f , "Inicia tu travesía en §cHeartless", ""),"textures/block/stripped_spruce_log.png",1);
            First_death first_death = new First_death(welcome);
            First_kill first_kill = new First_kill(welcome);

            init.registerAdvancements(welcome ,first_death ,first_kill );
            
            RootAdvancement discovery = new RootAdvancement(undead_week_tab, "discovery", new AdvancementDisplay(Material.ZOMBIE_HEAD, "§6Semana de los No-muertos", AdvancementFrameType.CHALLENGE, true, true, 0f, 0f , "§7Descubre la §asemana de los", "§aNo-muertos §7por primera vez."),"textures/block/stone.png",1);
            First_zombification first_zombification = new First_zombification(discovery);

            undead_week_tab.registerAdvancements(discovery ,first_zombification );

            init.getEventManager().register(init, PlayerLoadingCompletedEvent.class, event -> {
                init.showTab(event.getPlayer());
                init.grantRootAdvancement(event.getPlayer());
            });

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