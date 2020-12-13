package io.github.revxrsal.minigames.util;

import org.bukkit.ChatColor;

public class Chat {

    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

}
