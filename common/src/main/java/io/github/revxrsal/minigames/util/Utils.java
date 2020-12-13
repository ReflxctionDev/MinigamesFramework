package io.github.revxrsal.minigames.util;

import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;

public class Utils {

    public static <T> T n(T t, String er) {
        return Objects.requireNonNull(t, er);
    }

    public static <T> T n(T t) {
        return Objects.requireNonNull(t);
    }

    @SneakyThrows
    public static <K, U, V> Map<U, V> mapKeys(Map<K, V> map, Function<K, U> remap) {
        Map<U, V> newMap;
        if (map instanceof ImmutableMap) newMap = new LinkedHashMap<>();
        newMap = map.getClass().newInstance();
        for (Entry<K, V> entry : map.entrySet()) {
            newMap.put(remap.apply(entry.getKey()), entry.getValue());
        }
        if (map instanceof ImmutableMap)
            return ImmutableMap.copyOf(newMap);
        return newMap;
    }

    @SneakyThrows
    public static <K, V, U> Map<K, U> mapValues(Map<K, V> map, Function<V, U> remap) {
        Map<K, U> newMap;
        if (map instanceof ImmutableMap) newMap = new LinkedHashMap<>();
        newMap = map.getClass().newInstance();
        for (Entry<K, V> entry : map.entrySet()) {
            newMap.put(entry.getKey(), remap.apply(entry.getValue()));
        }
        if (map instanceof ImmutableMap)
            return ImmutableMap.copyOf(newMap);
        return newMap;
    }

    @NotNull
    @SafeVarargs
    public static <T> T firstNotNull(T... values) {
        for (T v : values) {
            if (v != null) return v;
        }
        throw new NullPointerException("All inputted values are null!");
    }

    public static int coerceMin(int value, int min) {
        return Math.max(value, min);
    }

    public static int coerceMax(int value, int max) {
        return Math.min(value, max);
    }

    public static int coerce(int value, int min, int max) {
        return value < min ? min : Math.min(value, max);
    }

}
