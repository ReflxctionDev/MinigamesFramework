package io.github.revxrsal.minigames;

public interface PluginRegistry {

    void registerCommands(MinigamePlugin plugin);

    void registerListeners(MinigamePlugin plugin);

}
