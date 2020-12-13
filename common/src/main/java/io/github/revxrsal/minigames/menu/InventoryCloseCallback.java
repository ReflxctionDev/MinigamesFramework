package io.github.revxrsal.minigames.menu;

import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface InventoryCloseCallback {

    void handle(@NotNull InventoryCloseEvent event);

}
