package com.darkbladedev.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.darkbladedev.effects.ZombieInfection;
import com.darkbladedev.utils.ColorText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ZombieInfectionCommand {
    
    private final ZombieInfection zombieInfection;
    
    public ZombieInfectionCommand(ZombieInfection zombieInfection) {
        this.zombieInfection = zombieInfection;
    }
    
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("virthaevents.command.effects.zombieinfection")) {
            sender.sendMessage(ColorText.Colorize("&cNo tienes permiso para usar este comando."));
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(ColorText.Colorize("&cUso: /ve effects zombie_infection <infectar|curar|estado|toggle> [jugador]"));
            return true;
        }
        
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "infectar":
            case "infect":
                if (args.length < 2 && !(sender instanceof Player)) {
                    sender.sendMessage(ColorText.Colorize("&cDebes especificar un jugador."));
                    return true;
                }
                
                Player targetToInfect = args.length >= 2 ? Bukkit.getPlayer(args[1]) : (Player) sender;
                if (targetToInfect == null || !targetToInfect.isOnline()) {
                    sender.sendMessage(ColorText.Colorize("&cJugador no encontrado o no está en línea."));
                    return true;
                }
                
                zombieInfection.infectPlayer(targetToInfect);
                sender.sendMessage(ColorText.Colorize("&aHas infectado a " + targetToInfect.getName() + " con el virus zombie."));
                break;
                
            case "curar":
            case "cure":
                if (args.length < 2 && !(sender instanceof Player)) {
                    sender.sendMessage(ColorText.Colorize("&cDebes especificar un jugador."));
                    return true;
                }
                
                Player targetToCure = args.length >= 2 ? Bukkit.getPlayer(args[1]) : (Player) sender;
                if (targetToCure == null || !targetToCure.isOnline()) {
                    sender.sendMessage(ColorText.Colorize("&cJugador no encontrado o no está en línea."));
                    return true;
                }
                
                zombieInfection.curePlayer(targetToCure);
                sender.sendMessage(ColorText.Colorize("&aHas curado a " + targetToCure.getName() + " del virus zombie."));
                break;
                
            case "estado":
            case "status":
                if (args.length < 2 && !(sender instanceof Player)) {
                    sender.sendMessage(ColorText.Colorize("&cDebes especificar un jugador."));
                    return true;
                }
                
                Player targetToCheck = args.length >= 2 ? Bukkit.getPlayer(args[1]) : (Player) sender;
                if (targetToCheck == null || !targetToCheck.isOnline()) {
                    sender.sendMessage(ColorText.Colorize("&cJugador no encontrado o no está en línea."));
                    return true;
                }
                
                boolean isInfected = zombieInfection.isInfected(targetToCheck);
                int curedCount = zombieInfection.getCuredCount(targetToCheck.getUniqueId());
                
                sender.sendMessage(ColorText.Colorize("&6Estado de " + targetToCheck.getName() + ":"));
                sender.sendMessage(ColorText.Colorize("&6- Infectado: " + (isInfected ? "&cSí" : "&aNo")));
                sender.sendMessage(ColorText.Colorize("&6- Infecciones curadas: &a" + curedCount));
                break;
                
            case "toggle":
            case "alternar":
                if (!sender.hasPermission("virthaevents.admin.zombieinfection")) {
                    sender.sendMessage(ColorText.Colorize("&cNo tienes permiso para usar este comando."));
                    return true;
                }
                
                boolean newState = !Boolean.parseBoolean(args.length >= 2 ? args[1] : "false");
                zombieInfection.setEnabled(newState);
                sender.sendMessage(ColorText.Colorize("&6Sistema de infección zombie: " + (newState ? "&aActivado" : "&cDesactivado")));
                break;

    
        // Add these cases to the switch statement in the execute method
        case "exclude_world":
        case "excludeworld":
            if (args.length < 2) {
                sender.sendMessage(ColorText.Colorize("&cDebes especificar un mundo."));
                return true;
            }
            
            String worldToExclude = args[1];
            if (Bukkit.getWorld(worldToExclude) == null) {
                sender.sendMessage(ColorText.Colorize("&cMundo no encontrado: " + worldToExclude));
                return true;
            }
            
            boolean excluded = zombieInfection.excludeWorld(worldToExclude);
            if (excluded) {
                sender.sendMessage(ColorText.Colorize("&aMundo excluido: " + worldToExclude));
            } else {
                sender.sendMessage(ColorText.Colorize("&cEl mundo ya estaba excluido: " + worldToExclude));
            }
            break;
        
        case "include_world":
        case "includeworld":
            if (args.length < 2) {
                sender.sendMessage(ColorText.Colorize("&cDebes especificar un mundo."));
                return true;
            }
            
            String worldToInclude = args[1];
            boolean included = zombieInfection.includeWorld(worldToInclude);
            if (included) {
                sender.sendMessage(ColorText.Colorize("&aMundo incluido: " + worldToInclude));
            } else {
                sender.sendMessage(ColorText.Colorize("&cEl mundo no estaba excluido: " + worldToInclude));
            }
            break;
        
        case "list_worlds":
        case "listworlds":
            Set<String> excludedWorlds = zombieInfection.getExcludedWorlds();
            if (excludedWorlds.isEmpty()) {
                sender.sendMessage(ColorText.Colorize("&aNo hay mundos excluidos."));
            } else {
                sender.sendMessage(ColorText.Colorize("&aMundos excluidos:"));
                for (String world : excludedWorlds) {
                    sender.sendMessage(ColorText.Colorize("&7- " + world));
                }
            }
            break;
        
        case "apply_mode":
        case "applymode":
            if (args.length < 2) {
                sender.sendMessage(ColorText.Colorize("&cDebes especificar un modo (all/current)."));
                return true;
            }
            
            String mode = args[1].toLowerCase();
            if (mode.equals("all")) {
                zombieInfection.setApplyToAllWorlds(true);
                sender.sendMessage(ColorText.Colorize("&aLa infección zombie ahora se aplica a todos los jugadores independientemente del mundo."));
            } else if (mode.equals("current")) {
                zombieInfection.setApplyToAllWorlds(false);
                sender.sendMessage(ColorText.Colorize("&aLa infección zombie ahora solo se aplica a jugadores en el mundo actual."));
            } else {
                sender.sendMessage(ColorText.Colorize("&cModo no válido. Usa 'all' o 'current'."));
            }
            break;
                            
            default:
                sender.sendMessage(ColorText.Colorize("&cAcción desconocida. Usa: /ve effects zombie_infection <infectar|curar|estado|toggle> [jugador]"));
                break;
        }
        
        return true;
    }
    
    // Update the tabComplete method to include the new commands
    public List<String> tabComplete(String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return Arrays.asList("infectar", "curar", "estado", "toggle", 
                            "exclude_world", "include_world", "list_worlds", "apply_mode").stream()
                .filter(s -> s.startsWith(partial))
                .collect(Collectors.toList());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("toggle")) {
                return Arrays.asList("true", "false");
            } else if (args[0].equalsIgnoreCase("exclude_world") || 
                  args[0].equalsIgnoreCase("excludeworld")) {
                // Return world names
                String partialName = args[1].toLowerCase();
                return Bukkit.getWorlds().stream()
                        .map(World::getName)
                        .filter(name -> name.toLowerCase().startsWith(partialName))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("apply_mode") || 
                  args[0].equalsIgnoreCase("applymode")) {
                return Arrays.asList("all", "current");
            } else {
                // Return online player names
                String partialName = args[1].toLowerCase();
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(partialName))
                        .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }
}