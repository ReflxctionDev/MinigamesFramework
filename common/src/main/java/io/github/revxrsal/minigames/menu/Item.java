package io.github.revxrsal.minigames.menu;

import com.cryptomorin.xseries.SkullUtils;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Preconditions;
import io.github.revxrsal.minigames.gson.GsonHook;
import io.github.revxrsal.minigames.gson.GsonHook.AfterDeserialization;
import io.github.revxrsal.minigames.util.Chat;
import io.github.revxrsal.minigames.util.Placeholders;
import io.github.revxrsal.minigames.util.Utils;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An immutable, fast, and thread-safe wrapper for {@link ItemStack}s.
 */
@Getter
@GsonHook
public class Item {

    private final XMaterial type;
    @Nullable private final String displayName;
    private final List<String> lore;
    private final int count;
    private final Map<XEnchantment, Integer> enchantments;
    private final ItemFlag[] itemFlags;
    @Nullable private final UUID skull;
    private final boolean unbreakable;

    @Getter(AccessLevel.NONE)
    protected transient ItemStack itemStack;

    protected Item(XMaterial type,
                   @Nullable String displayName,
                   List<String> lore,
                   int count,
                   Map<XEnchantment, Integer> enchantments,
                   ItemFlag[] itemFlags,
                   @Nullable UUID skull,
                   boolean unbreakable) {
        this.type = type;
        this.displayName = colorize(displayName);
        this.lore = lore == null ? null : lore.stream().map(Item::colorize).collect(Collectors.toList());
        this.count = count;
        this.enchantments = enchantments;
        this.itemFlags = itemFlags;
        this.skull = skull;
        this.unbreakable = unbreakable;
        createItem0();
    }

    @Contract("null -> null")
    public static String colorize(@Nullable String o) {
        if (o == null) return null;
        return ChatColor.translateAlternateColorCodes('&', o);
    }

    public void give(Player... players) {
        for (Player player : players)
            giveItem(player);
    }

    public void give(int slot, Player... players) {
        for (Player player : players)
            player.getInventory().setItem(slot, itemStack);
    }

    public void give(Iterable<Player> players) {
        for (Player player : players)
            giveItem(player);
    }

    protected void giveItem(Player player) {
        player.getInventory().addItem(itemStack);
    }

    public Builder asBuilder() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    @AfterDeserialization
    protected void createItem0() {
        ItemStack item;
        if (skull != null) {
            item = SkullUtils.getSkull(skull);
        } else
            item = type.parseItem();
        int amount = Utils.coerce(count, 1, 64);
        Objects.requireNonNull(item).setAmount(amount);
        ItemMeta m = item.getItemMeta();
        if (m == null) return;
        if (displayName != null && !displayName.equals("{}")) m.setDisplayName(colorize(displayName));
        if (lore != null) m.setLore(lore.stream().map(Chat::colorize).collect(Collectors.toList()));
        if (itemFlags != null) m.addItemFlags(itemFlags);
        if (unbreakable) {
            try {
                m.setUnbreakable(true);
            } catch (Throwable t) {
                try {
                    m.spigot().setUnbreakable(true);
                } catch (Throwable ignored) {
                }
            }
        }
        if (enchantments != null)
            enchantments.forEach((ench, lvl) -> m.addEnchant(ench.parseEnchantment(), lvl, true));
        item.setItemMeta(m);
        itemStack = item;
    }

    public ItemStack withPlaceholders(Object... placeholders) {
        return setPlaceholders(new ItemStack(itemStack), placeholders);
    }

    public ItemStack createItem() {
        return new ItemStack(itemStack);
    }

    public boolean isSimilar(@Nullable ItemStack other) {
        return itemStack.isSimilar(other);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof ItemStack) return isSimilar(((ItemStack) o));
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return itemStack.isSimilar(item.itemStack);
    }

    @Override public int hashCode() {
        return Objects.hash(itemStack);
    }

    @Getter
    public static class SlotItem extends Item {

        private final int slot;

        protected SlotItem(XMaterial type, @Nullable String displayName, List<String> lore, int count, Map<XEnchantment, Integer> enchantments, ItemFlag[] itemFlags, @Nullable UUID skull, boolean unbreakable, int slot) {
            super(type, displayName, lore, count, enchantments, itemFlags, skull, unbreakable);
            this.slot = slot;
        }

        @Override protected void giveItem(Player player) {
            player.getInventory().setItem(slot, itemStack);
        }
    }

    public static Builder fromItemStack(@NotNull ItemStack item) {
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        Builder b = Item.builder().type(item.getType())
                .name(meta.getDisplayName())
                .lore(meta.getLore())
                .itemFlags(meta.getItemFlags())
                .enchant(Utils.mapKeys(meta.getEnchants(), XEnchantment::matchXEnchantment));
        if (meta instanceof SkullMeta)
            try {
                b.skull(((SkullMeta) meta).getOwningPlayer().getUniqueId());
            } catch (Throwable ignored) {
            }
        return b;
    }

    public static ItemStack setPlaceholders(ItemStack item, Object... placeholders) {
        ItemMeta meta = Builder.n(item.getItemMeta(), "meta is null!");
        if (meta.hasDisplayName())
            meta.setDisplayName(Placeholders.on(meta.getDisplayName(), placeholders));
        if (meta.hasLore())
            meta.setLore(meta.getLore().stream().map(t -> Placeholders.on(t, placeholders)).collect(Collectors.toList()));
        item.setItemMeta(meta);
        return item;
    }

    public static class Builder {

        private XMaterial type = XMaterial.STONE;
        @Nullable private String displayName = null;
        private List<String> lore = new ArrayList<>();
        private int count = 1;
        private int slot = -1;
        private Map<XEnchantment, Integer> enchantments = new HashMap<>();
        private final Set<ItemFlag> itemFlags = new HashSet<>();
        @Nullable private UUID skull;
        private boolean unbreakable = false;

        public Builder() {
        }

        public Builder(Item item) {
            type = item.type;
            displayName = item.displayName;
            lore = item.lore;
            count = item.count;
            enchantments = item.enchantments;
            skull = item.skull;
            Collections.addAll(itemFlags, item.itemFlags);
            unbreakable = item.unbreakable;
        }

        public Builder type(@NotNull Material material) {
            this.type = XMaterial.matchXMaterial(n(material, "material"));
            return this;
        }

        public Builder type(@NotNull XMaterial material) {
            this.type = n(material, "material");
            return this;
        }

        public Builder slot(int slot) {
            Preconditions.checkArgument(slot >= 0, "slot cannot be less than 0!");
            this.slot = slot;
            return this;
        }

        public Builder name(@Nullable String name) {
            if (name == null || name.equals("{}")) return this;
            displayName = name;
            return this;
        }

        public Builder amount(int amount) {
            this.count = amount;
            return this;
        }

        public Builder loreLine(@NotNull String lore) {
            this.lore.add(n(lore, "lore"));
            return this;
        }

        public Builder lore(@Nullable Collection<String> lore) {
            if (lore == null) return this;
            this.lore.addAll(n(lore, "lore"));
            return this;
        }


        public Builder lore(@NotNull String... lore) {
            Collections.addAll(this.lore, n(lore, "lore"));
            return this;
        }


        public Builder lore(@NotNull String lore, int index) {
            this.lore.set(index, n(lore, "lore"));
            return this;
        }

        public Builder removeLore(@NotNull String lore) {
            this.lore.remove(lore);
            return this;
        }

        public Builder skull(@Nullable UUID skull) {
            this.skull = skull;
            return this;
        }

        public Builder skull(@Nullable String playerName) {
            if (playerName == null) {
                this.skull = null;
                return this;
            }
            this.skull = Bukkit.getOfflinePlayer(playerName).getUniqueId();
            return this;
        }

        public Builder itemFlag(@NotNull ItemFlag itemFlag) {
            this.itemFlags.add(n(itemFlag, "itemFlag"));
            return this;
        }

        public Builder itemFlags(@NotNull Collection<ItemFlag> itemFlags) {
            this.itemFlags.addAll(n(itemFlags, "itemFlags"));
            return this;
        }

        public Builder unbreakable(boolean v) {
            unbreakable = v;
            return this;
        }

        public Builder enchant(@NotNull XEnchantment enchantment, int level) {
            this.enchantments.put(n(enchantment, "enchantment"), level);
            return this;
        }

        public Builder enchant(@NotNull Map<XEnchantment, Integer> enchantments) {
            this.enchantments.putAll(n(enchantments, "enchantments"));
            return this;
        }

        public Item build() {
            if (slot == -1)
                return new Item(type, displayName, lore, count, enchantments, itemFlags.toArray(new ItemFlag[0]), skull, unbreakable);
            else
                return new SlotItem(type, displayName, lore, count, enchantments, itemFlags.toArray(new ItemFlag[0]), skull, unbreakable, slot);
        }

        private static <T> T n(T t, String m) {
            return Objects.requireNonNull(t, m);
        }
    }

}
