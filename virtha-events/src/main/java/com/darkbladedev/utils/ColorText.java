package com.darkbladedev.utils;

import org.bukkit.ChatColor;

public class ColorText {

    public static final String prefix = "&2[ &5VIRTHA &3Events &2]";

    public static String Colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', prefix + " " + text);
    }
    
}
