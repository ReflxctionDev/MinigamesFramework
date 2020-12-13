/*
 * * Copyright 2020 github.com/ReflxctionDev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.revxrsal.minigames.menu;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.github.revxrsal.minigames.util.Utils.n;

/**
 * Represents a button
 */
public class Button {

    public static final InventoryClickCallback CANCEL_ACTION = event -> event.setCancelled(true);
    public static final InventoryClickCallback CLOSE_INVENTORY = (event) -> event.getWhoClicked().closeInventory();

    private final Item item;
    private final ImmutableList<InventoryClickCallback> actions;

    private Button(Item item, ImmutableList<InventoryClickCallback> actions) {
        this.item = item;
        this.actions = actions;
    }

    public Item getItem() {
        return item;
    }

    public List<InventoryClickCallback> getActions() {
        return actions;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final ImmutableList.Builder<InventoryClickCallback> actions = new ImmutableList.Builder<>();
        private Item item;

        public Builder item(@NotNull Item item) {
            this.item = n(item, "item");
            return this;
        }

        public Builder handle(@NotNull InventoryClickCallback handle) {
            actions.add(handle);
            return this;
        }

        public Builder close() {
            return handle(CLOSE_INVENTORY);
        }

        public Builder cancelClick() {
            return handle(CANCEL_ACTION);
        }

        public Builder then(@NotNull InventoryClickCallback handle) {
            return handle(handle);
        }

        public Builder and(@NotNull InventoryClickCallback handle) {
            return handle(handle);
        }

        public Button build() {
            return new Button(item, actions.build());
        }

    }

}