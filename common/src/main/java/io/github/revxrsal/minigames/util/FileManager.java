package io.github.revxrsal.minigames.util;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class FileManager {

    private final JavaPlugin plugin;

    public FileManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public File embedded(@NotNull String name) {
        File file = new File(plugin.getDataFolder(), name.replace('/', File.separatorChar));
        if (!file.exists())
            plugin.saveResource(name, true);
        return file;
    }

    public File directory(@NotNull String name) {
        File directory = new File(plugin.getDataFolder(), name.replace('/', File.separatorChar));
        directory.mkdir();
        return directory;
    }

}
