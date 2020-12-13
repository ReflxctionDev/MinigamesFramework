package io.github.revxrsal.minigames.menu;

import io.github.revxrsal.minigames.util.Chat;
import io.github.revxrsal.minigames.util.Metadata;
import io.github.revxrsal.minigames.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class InventoryUI {

    private static final Metadata<InventoryUI> OPENED_UI = Metadata.of("opened_menu");

    private final Map<Integer, Button> buttons = new HashMap<>();
    private final List<InventoryClickCallback> globalActions = new ArrayList<>();
    private final List<InventoryCloseCallback> closeActions = new ArrayList<>();
    private final String title;
    private final int size;
    protected boolean cancelAllClicks = false;

    public InventoryUI(String title, int size) {
        this.title = Chat.colorize(title);
        this.size = Utils.coerce(size, 1, 6) * 9;
    }

    protected void register(int slot, @NotNull Button button) {
        buttons.put(slot, button);
    }

    protected void always(@NotNull InventoryClickCallback callback) {
        globalActions.add(callback);
    }

    protected void whenClosed(@NotNull InventoryCloseCallback callback) {
        closeActions.add(callback);
    }

    protected Inventory createInventory() {
        Inventory inventory = Bukkit.createInventory(null, size, title);
        for (Entry<Integer, Button> entry : buttons.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue().getItem().createItem());
        }
        return inventory;
    }

    public void display(@NotNull HumanEntity entity) {
        entity.openInventory(createInventory());
        OPENED_UI.set(entity, this);
    }

    protected void onClick(InventoryClickEvent event) {
        if (event.getAction().name().contains("MOVE_"))
            event.setCancelled(true);
        if (event.getRawSlot() > event.getInventory().getSize() || event.getSlotType() == InventoryType.SlotType.OUTSIDE)
            return;
        if (event.getView().getBottomInventory() == event.getInventory()) {
            event.setCancelled(true);
            return;
        }
        if (!event.getView().getTitle().equals(title)) return;
        if (event.getCurrentItem() == null) return;
        if (cancelAllClicks) event.setCancelled(true);
        for (InventoryClickCallback e : globalActions) {
            e.handle(event);
        }
        Button button = buttons.get(event.getRawSlot());
        if (button != null && button.getActions() != null) {
            for (InventoryClickCallback task : button.getActions()) {
                task.handle(event);
            }
        }
    }

    public static class MenuListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void onInventoryClick(InventoryClickEvent event) {
            InventoryUI menu = OPENED_UI.get(event.getWhoClicked());
            if (menu == null) return;
            menu.onClick(event);
        }

        @EventHandler(ignoreCancelled = true)
        public void onInventoryClose(InventoryCloseEvent event) {
            InventoryUI menu = OPENED_UI.get(event.getPlayer());
            if (menu == null) return;
            menu.closeActions.forEach(a -> a.handle(event));
            OPENED_UI.remove(event.getPlayer());
        }
    }

}
