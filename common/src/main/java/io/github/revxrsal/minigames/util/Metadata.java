package io.github.revxrsal.minigames.util;

import io.github.revxrsal.minigames.MinigamePlugin;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Metadata<V> {

    private final String key;

    private Metadata(String key) {
        this.key = key;
    }

    public @Nullable V get(@NotNull Metadatable metadatable) {
        try {
            return (V) metadatable.getMetadata(key).get(0).value();
        } catch (Throwable t) {
            return null;
        }
    }

    public @NotNull V require(@NotNull Metadatable metadatable) {
        try {
            return (V) metadatable.getMetadata(key).get(0).value();
        } catch (Throwable t) {
            throw new IllegalStateException("Entity " + metadatable + " does not have metadata key '" + key + "'");
        }
    }

    public @Nullable V remove(@NotNull Metadatable metadatable) {
        V v = get(metadatable);
        if (v != null)
            metadatable.removeMetadata(key, MinigamePlugin.getPlugin());
        return v;
    }

    @Contract("_, null -> null; _, !null -> !null")
    public V set(@NotNull Metadatable metadatable, V value) {
        metadatable.setMetadata(key, new FixedMetadataValue(MinigamePlugin.getPlugin(), value));
        return value;
    }

    public static <V> Metadata<V> of(@NotNull String key) {
        return new Metadata<>(MinigamePlugin.getPlugin().getName().toLowerCase() + "." + key);
    }

}
