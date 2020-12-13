package io.github.revxrsal.minigames.menu;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface InventoryClickCallback {

    void handle(@NotNull InventoryClickEvent event);

}
