package io.github.revxrsal.minigames.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import static io.github.revxrsal.minigames.util.Placeholders.PlaceholderFiller.p;

@SuppressWarnings({"rawtypes", "CodeBlock2Expr", "unused", "RedundantSuppression"})
public class Placeholders {

    private static final boolean PAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    public static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    /**
     * Placeholder filler for offline players
     */
    private static final PlaceholderFiller<OfflinePlayer> OFFLINE_PLAYER = (player, b) -> {
        p(b, "player", player.getName() == null ? "NoName" : player.getName());
        p(b, "player_name", player.getName() == null ? "NoName" : player.getName());
        if (PAPI) {
            String created = PlaceholderAPI.setPlaceholders(player, b.toString());
            b.clear().append(created);
        }
    };

    /**
     * Placeholder filler for players
     */
    private static final PlaceholderFiller<Player> PLAYER = (player, b) -> {
        p(b, "player_displayname", player.getDisplayName());
        p(b, "player_health", (int) player.getHealth());
        if (PAPI) {
            String built = b.toString();
            String created = PlaceholderAPI.setPlaceholders(player, built);
            b.clear().append(created);
        }
        OFFLINE_PLAYER.apply(player, b);
    };

    @FunctionalInterface
    public interface PlaceholderFiller<T> {

        void apply(T value, StrBuilder builder);

        static void p(StrBuilder builder, String placeholder, Object value) {
            builder.replaceAll("%" + placeholder + "%", value.toString());
        }
    }

    @NotNull
    @SafeVarargs
    public static <T> T firstNotNull(T... values) {
        for (T v : values) {
            if (v != null) return v;
        }
        throw new NullPointerException("All inputted values are null!");
    }

    public static String on(String original, Object... formats) {
        StrBuilder builder = new StrBuilder(original);
        for (Entry<Class<?>, PlaceholderFiller> filler : fillers.entrySet())
            for (Object o : formats) {
                if (o != null)
                    if (filler.getKey().isAssignableFrom(o.getClass()))
                        filler.getValue().apply(filler.getKey().cast(o), builder);
            }
        if (PAPI) {
            OfflinePlayer player = null;
            for (Object t : formats) {
                if (t == null) continue;
                if (t instanceof OfflinePlayer) {
                    break;
                }
            }
            String created = PlaceholderAPI.setPlaceholders(player, builder.toString());
            builder.clear().append(created);
        }
        return Chat.colorize(builder.toString());
    }

    public static String formatTimeMillis(int milliseconds) {
        int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        int secondsLeft = seconds % 3600 % 60;
        int minutes = (int) Math.floor((float) seconds % 3600 / 60);
        int hours = (int) Math.floor((float) seconds / 3600);

        String hoursFormat = ((hours < 10) ? "0" : "") + hours;
        String minutesFormat = ((minutes < 10) ? "0" : "") + minutes;
        String secondsFormat = ((secondsLeft < 10) ? "0" : "") + secondsLeft;
        if (hours <= 0)
            return minutesFormat + ":" + secondsFormat;
        return hoursFormat + ":" + minutesFormat + ":" + secondsFormat;
    }

    public static String formatTime(int seconds) {
        int secondsLeft = seconds % 3600 % 60;
        int minutes = (int) Math.floor((float) seconds % 3600 / 60);
        int hours = (int) Math.floor((float) seconds / 3600);

        String hoursFormat = ((hours < 10) ? "0" : "") + hours;
        String minutesFormat = ((minutes < 10) ? "0" : "") + minutes;
        String secondsFormat = ((secondsLeft < 10) ? "0" : "") + secondsLeft;
        if (hours <= 0)
            return minutesFormat + ":" + secondsFormat;
        return hoursFormat + ":" + minutesFormat + ":" + secondsFormat;
    }

    public static String formatTimeFancy(long time) {
        Duration d = Duration.ofMillis(time);
        long hours = d.toHours();
        long minutes = d.minusHours(hours).getSeconds() / 60;
        long seconds = d.minusMinutes(minutes).minusHours(hours).getSeconds();
        List<String> words = new ArrayList<>();
        if (hours != 0)
            words.add(hours + plural(hours, " hour"));
        if (minutes != 0)
            words.add(minutes + plural(minutes, " minute"));
        if (seconds != 0)
            words.add(seconds + plural(seconds, " second"));
        return toFancyString(words);
    }

    public static String formatNumber(int number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String formatNumber(Number number) {
        return NUMBER_FORMAT.format(number);
    }

    public static <T> String toFancyString(List<T> list) {
        StringJoiner builder = new StringJoiner(", ");
        if (list.isEmpty()) return "";
        if (list.size() == 1) return list.get(0).toString();
        for (int i = 0; i < list.size(); i++) {
            T el = list.get(i);
            if (i + 1 == list.size())
                return builder.toString() + " and " + el.toString();
            else
                builder.add(el.toString());
        }
        return builder.toString();
    }

    public static String plural(Number count, String thing) {
        if (count.intValue() == 1) return thing;
        if (thing.endsWith("y"))
            return thing.substring(0, thing.length() - 1) + "ies";
        return thing + "s";
    }

    public static void register(@NotNull Class<?> cl) {
        for (Field field : Placeholders.class.getDeclaredFields()) {
            if (!PlaceholderFiller.class.isAssignableFrom(field.getType())) continue;
            Class<?> type = ((Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
            try {
                fillers.put(type, (PlaceholderFiller) field.get(null));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static final Map<Class<?>, PlaceholderFiller> fillers = new HashMap<>();

    static {
        for (Field field : Placeholders.class.getDeclaredFields()) {
            if (!PlaceholderFiller.class.isAssignableFrom(field.getType())) continue;
            Class<?> type = ((Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
            try {
                fillers.put(type, (PlaceholderFiller) field.get(null));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
