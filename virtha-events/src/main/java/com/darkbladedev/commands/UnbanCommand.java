package com.darkbladedev.commands;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.darkbladedev.utils.ColorText;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class UnbanCommand implements CommandExecutor, TabCompleter {

    private final File banDataFile;

    public UnbanCommand(File banDataFile) {
        this.banDataFile = banDataFile;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!sender.hasPermission("virtha.admin.unban")) {
            sender.sendMessage(ColorText.Colorize("&cNo tienes permiso para usar este comando."));
            return true;
        }

        // Check if a player name was provided
        if (args.length < 1) {
            sender.sendMessage(ColorText.Colorize("&cUso: /vunban <jugador>"));
            return true;
        }

        String playerName = args[0];
        
        // Try to find the player (online or offline)
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(playerName);
        if (targetPlayer == null) {
            // Try to find by exact name
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(playerName)) {
                    targetPlayer = offlinePlayer;
                    break;
                }
            }
            
            if (targetPlayer == null) {
                sender.sendMessage(ColorText.Colorize("&cNo se encontró ningún jugador con ese nombre."));
                return true;
            }
        }

        // Check if the player is banned
        if (!Bukkit.getBanList(BanList.Type.NAME).isBanned(targetPlayer.getName())) {
            sender.sendMessage(ColorText.Colorize("&cEse jugador no está baneado."));
            return true;
        }

        // Unban the player by name
        Bukkit.getBanList(BanList.Type.NAME).pardon(targetPlayer.getName());
        
        // Try to unban by IP if we have a record of their last IP
        // Note: This is limited as we can't easily get a player's IP if they're offline
        // We would need to store this information separately
        
        // Remove or reset the ban count in the JSON file
        resetBanCount(targetPlayer.getUniqueId());

        // Notify the sender
        sender.sendMessage(ColorText.Colorize("&a¡Jugador " + targetPlayer.getName() + " desbaneado con éxito!"));
        
        // Log the unban
        Bukkit.getConsoleSender().sendMessage(ColorText.Colorize(
            sender.getName() + " ha desbaneado a " + targetPlayer.getName()
        ));

        return true;
    }

    private void resetBanCount(UUID playerUUID) {
        if (!banDataFile.exists()) {
            return;
        }

        try {
            // Read the current ban data
            JSONParser parser = new JSONParser();
            JSONObject banData;
            
            try (FileReader reader = new FileReader(banDataFile)) {
                banData = (JSONObject) parser.parse(reader);
            }

            // Remove the player's ban count or set it to 0
            banData.remove(playerUUID.toString());
            
            // Write the updated data back to the file
            try (FileWriter writer = new FileWriter(banDataFile)) {
                writer.write(banData.toJSONString());
                writer.flush();
            }
        } catch (IOException | ParseException e) {
            Bukkit.getLogger().severe("Error updating ban data: " + e.getMessage());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Return a list of banned players for tab completion
            List<String> bannedPlayers = new ArrayList<>();
            
            // Add all banned players to the list
            Bukkit.getBanList(BanList.Type.NAME).getBanEntries().forEach(entry -> 
                bannedPlayers.add(entry.getTarget())
            );
            
            // Filter the list based on what the player has typed so far
            String partialName = args[0].toLowerCase();
            return bannedPlayers.stream()
                .filter(name -> name.toLowerCase().startsWith(partialName))
                .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
}