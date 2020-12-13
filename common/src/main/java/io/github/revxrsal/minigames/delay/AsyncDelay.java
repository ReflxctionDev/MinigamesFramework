package io.github.revxrsal.minigames.delay;

import io.github.revxrsal.minigames.MinigamePlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * A delay handler that updates asynchronously
 *
 * @param <E> The context of delays.
 */
public final class AsyncDelay<E extends Enum<E>> {

    private static final UUID CONSOLE_UUID = new UUID(0, 0);

    private static final Map<Class<?>, Function<?, UUID>> TO_UUID = new HashMap<>();

    public static <T> void registerUUIDAdapter(Class<T> type, Function<T, UUID> convert) {
        TO_UUID.put(type, convert);
    }

    private final Map<UUID, Map<E, Long>> delays = new ConcurrentHashMap<>();

    private Map<E, Long> get(UUID uuid) {
        return delays.computeIfAbsent(uuid, c -> new ConcurrentHashMap<>());
    }

    public CompletableFuture<Void> delay(Object target, E context, long duration, TimeUnit object) {
        if (duration == 0) return CompletableFuture.completedFuture(null);
        UUID uuid = getUUID(target);
        get(uuid).put(context, System.currentTimeMillis());
        CompletableFuture<Void> future = new CompletableFuture<>();
        MinigamePlugin.SCHEDULED_SERVICE.schedule(() -> {
            get(uuid).remove(context);
            future.complete(null);
        }, duration, object);
        return future;
    }

    public long getTimeLeft(Object target, E context) {
        return System.currentTimeMillis() - get(getUUID(target)).getOrDefault(context, System.currentTimeMillis());
    }

    public boolean hasDelay(Object target, E context) {
        return getTimeLeft(target, context) > 0;
    }

    public long getTimeLeft(Object target, E context, TimeUnit unit) {
        return TimeUnit.MILLISECONDS.convert(getTimeLeft(target, context), unit);
    }

    private UUID getUUID(Object o) {
        if (o instanceof OfflinePlayer) {
            return ((OfflinePlayer) o).getUniqueId();
        }
        if (o instanceof ConsoleCommandSender) {
            return CONSOLE_UUID;
        }
        if (o instanceof Entity) {
            return ((Entity) o).getUniqueId();
        }
        if (o instanceof UUID) {
            return (UUID) o;
        }
        Function<Object, UUID> convert = (Function<Object, UUID>) TO_UUID.get(o.getClass());
        if (convert != null) {
            return convert.apply(o);
        }
        throw new IllegalArgumentException("Don't know how to get the UUID from " + o + ".");
    }
}
