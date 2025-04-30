package com.darkbladedev.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.darkbladedev.effects.ZombieInfection;
import com.darkbladedev.utils.ColorText;
import com.darkbladedev.utils.ConfigManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Comando para gestionar las curas de infección zombie
 */
public class CureManagementCommand implements CommandExecutor, TabCompleter {
    
    private final Plugin plugin;
    private final ZombieInfection zombieInfection;
    
    public CureManagementCommand(Plugin plugin, ZombieInfection zombieInfection) {
        this.plugin = plugin;
        this.zombieInfection = zombieInfection;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verificar si el comando debe estar disponible solo en modo debug
        if (command.getName().equalsIgnoreCase("vcuredebug") && !ConfigManager.isDebugEnabled()) {
            sender.sendMessage(ColorText.Colorize("&cEste comando solo está disponible en modo debug."));
            return true;
        }
        
        // Verificar permisos
        if (!sender.hasPermission("virthaevents.command.curemanagement")) {
            sender.sendMessage(ColorText.Colorize("&cNo tienes permiso para usar este comando."));
            return true;
        }
        
        if (args.length < 1) {
            sendHelpMessage(sender);
            return true;
        }
        
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "set":
            case "establecer":
                if (args.length < 3) {
                    sender.sendMessage(ColorText.Colorize("&cUso: /vcure set <jugador> <cantidad>"));
                    return true;
                }
                
                Player targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer == null || !targetPlayer.isOnline()) {
                    sender.sendMessage(ColorText.Colorize("&cJugador no encontrado o no está en línea."));
                    return true;
                }
                
                int newCount;
                try {
                    newCount = Integer.parseInt(args[2]);
                    if (newCount < 0) {
                        sender.sendMessage(ColorText.Colorize("&cLa cantidad debe ser un número positivo."));
                        return true;
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorText.Colorize("&cLa cantidad debe ser un número válido."));
                    return true;
                }
                
                zombieInfection.setCuredCount(targetPlayer.getUniqueId(), newCount);
                sender.sendMessage(ColorText.Colorize("&aHas establecido el contador de curas de " + targetPlayer.getName() + " a " + newCount));
                break;
                
            case "add":
            case "agregar":
                if (args.length < 3) {
                    sender.sendMessage(ColorText.Colorize("&cUso: /vcure add <jugador> <cantidad>"));
                    return true;
                }
                
                Player targetPlayerAdd = Bukkit.getPlayer(args[1]);
                if (targetPlayerAdd == null || !targetPlayerAdd.isOnline()) {
                    sender.sendMessage(ColorText.Colorize("&cJugador no encontrado o no está en línea."));
                    return true;
                }
                
                int addCount;
                try {
                    addCount = Integer.parseInt(args[2]);
                    if (addCount <= 0) {
                        sender.sendMessage(ColorText.Colorize("&cLa cantidad debe ser un número positivo."));
                        return true;
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorText.Colorize("&cLa cantidad debe ser un número válido."));
                    return true;
                }
                
                UUID playerUUID = targetPlayerAdd.getUniqueId();
                int currentCount = zombieInfection.getCuredCount(playerUUID);
                zombieInfection.setCuredCount(playerUUID, currentCount + addCount);
                sender.sendMessage(ColorText.Colorize("&aHas agregado " + addCount + " curas a " + targetPlayerAdd.getName() + 
                                                    " (Total: " + (currentCount + addCount) + ")"));
                break;
                
            case "remove":
            case "quitar":
                if (args.length < 3) {
                    sender.sendMessage(ColorText.Colorize("&cUso: /vcure remove <jugador> <cantidad>"));
                    return true;
                }
                
                Player targetPlayerRemove = Bukkit.getPlayer(args[1]);
                if (targetPlayerRemove == null || !targetPlayerRemove.isOnline()) {
                    sender.sendMessage(ColorText.Colorize("&cJugador no encontrado o no está en línea."));
                    return true;
                }
                
                int removeCount;
                try {
                    removeCount = Integer.parseInt(args[2]);
                    if (removeCount <= 0) {
                        sender.sendMessage(ColorText.Colorize("&cLa cantidad debe ser un número positivo."));
                        return true;
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorText.Colorize("&cLa cantidad debe ser un número válido."));
                    return true;
                }
                
                UUID playerUUIDRemove = targetPlayerRemove.getUniqueId();
                int currentCountRemove = zombieInfection.getCuredCount(playerUUIDRemove);
                int newCountRemove = Math.max(0, currentCountRemove - removeCount);
                zombieInfection.setCuredCount(playerUUIDRemove, newCountRemove);
                sender.sendMessage(ColorText.Colorize("&aHas quitado " + (currentCountRemove - newCountRemove) + " curas a " + 
                                                    targetPlayerRemove.getName() + " (Total: " + newCountRemove + ")"));
                break;
                
            case "check":
            case "verificar":
                if (args.length < 2) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ColorText.Colorize("&cDebes especificar un jugador."));
                        return true;
                    }
                    
                    Player senderPlayer = (Player) sender;
                    int senderCount = zombieInfection.getCuredCount(senderPlayer.getUniqueId());
                    sender.sendMessage(ColorText.Colorize("&aTu contador de curas: &e" + senderCount));
                    return true;
                }
                
                Player targetPlayerCheck = Bukkit.getPlayer(args[1]);
                if (targetPlayerCheck == null || !targetPlayerCheck.isOnline()) {
                    sender.sendMessage(ColorText.Colorize("&cJugador no encontrado o no está en línea."));
                    return true;
                }
                
                int targetCount = zombieInfection.getCuredCount(targetPlayerCheck.getUniqueId());
                sender.sendMessage(ColorText.Colorize("&aContador de curas de " + targetPlayerCheck.getName() + ": &e" + targetCount));
                break;
                
            default:
                sendHelpMessage(sender);
                break;
        }
        
        return true;
    }
    
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ColorText.Colorize("&6=== Comandos de Gestión de Curas ==="));
        sender.sendMessage(ColorText.Colorize("&e/vcure set <jugador> <cantidad> &7- Establece el contador de curas"));
        sender.sendMessage(ColorText.Colorize("&e/vcure add <jugador> <cantidad> &7- Agrega curas al contador"));
        sender.sendMessage(ColorText.Colorize("&e/vcure remove <jugador> <cantidad> &7- Quita curas del contador"));
        sender.sendMessage(ColorText.Colorize("&e/vcure check [jugador] &7- Verifica el contador de curas"));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("virthaevents.command.curemanagement")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return Arrays.asList("set", "add", "remove", "check").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            String action = args[0].toLowerCase();
            if (action.equals("set") || action.equals("add") || action.equals("remove") || action.equals("check")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 3) {
            String action = args[0].toLowerCase();
            if (action.equals("set")) {
                return Arrays.asList("0", "1", "5", "10");
            } else if (action.equals("add") || action.equals("remove")) {
                return Arrays.asList("1", "5", "10");
            }
        }
        
        return new ArrayList<>();
    }
}