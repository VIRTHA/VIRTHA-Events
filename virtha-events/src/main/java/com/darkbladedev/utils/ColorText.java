package com.darkbladedev.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import com.darkbladedev.storage.StorageManager;

public class ColorText {

    private static String prefix = null;
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    /**
     * Gets the prefix, initializing it if necessary
     * @return The current prefix
     */
    private static String getPrefix() {
        if (prefix == null) {
            try {
                prefix = StorageManager.getPrefix();
            } catch (Exception e) {
                // Fallback to a default prefix if StorageManager isn't ready
                prefix = "&7[ &5VIRTHA &3Events &7]";
            }
        }
        return prefix;
    }

    /**
     * Colorizes text with the default prefix
     * @param text The text to colorize
     * @return Colorized text with default prefix
     */
    public static String Colorize(String text) {
        return ColorizeWithPrefix(text, getPrefix());
    }
    
    /**
     * Colorizes text without adding any prefix
     * @param text The text to colorize
     * @return Colorized text without prefix
     */
    public static String ColorizeNoPrefix(String text) {
        return processColors(text);
    }
    
    /**
     * Colorizes text with a custom prefix
     * @param text The text to colorize
     * @param customPrefix The custom prefix to use
     * @return Colorized text with custom prefix
     */
    public static String ColorizeWithPrefix(String text, String customPrefix) {
        return processColors(customPrefix + " " + text);
    }
    
    /**
     * Process both hex and standard color codes in a text
     * @param text The text to process
     * @return Text with processed color codes
     */
    private static String processColors(String text) {
        // Process hex colors first (format: &#RRGGBB)
        if (text.contains("&#")) {
            Matcher matcher = HEX_PATTERN.matcher(text);
            StringBuffer buffer = new StringBuffer();
            
            while (matcher.find()) {
                String hexColor = matcher.group(1);
                matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + hexColor).toString());
            }
            
            matcher.appendTail(buffer);
            text = buffer.toString();
        }
        
        // Then process standard color codes
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
