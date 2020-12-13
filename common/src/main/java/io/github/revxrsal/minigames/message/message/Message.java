package io.github.revxrsal.minigames.message.message;

import io.github.revxrsal.minigames.util.Chat;
import io.github.revxrsal.minigames.MinigamePlugin;
import io.github.revxrsal.minigames.util.Placeholders;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Represents a message. Construct with {@link MessageBuilder}.
 */
public class Message {

    private final String key;
    private final String defaultValue;
    private final String comment;
    private final String[] description;

    private String value;

    Message(String key, String defaultValue, String comment, String[] description) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.comment = comment;
        this.description = description;
        MinigamePlugin.getMessageManager().registerMessage(this);
    }

    public String build(boolean flatten, Object... formats) {
        String value = getValue();
        if (!value.contains("[noprefix]")) {
            for (Object f : formats) {
                if (f instanceof Prefixable) {
                    value = "[noprefix]" + ((Prefixable) f).getPrefix() + value;
                    break;
                }
            }
        }
        return Placeholders.on(value, flatten ? flatten(formats).toArray() : formats);
    }

    public @NotNull String create(Object... formats) {
        return build(true, formats);
    }

    /**
     * Flattens the specified array by joining all nested arrays to a single array.
     *
     * @param array The array to flatten
     * @return A stream of the flattened array.
     */
    public static Stream<Object> flatten(Object[] array) {
        return Arrays.stream(array).flatMap(o -> o instanceof Object[] ? flatten((Object[]) o) : Stream.of(o));
    }

    public String getValue() {
        return value == null ? value = defaultValue : value;
    }

    public String getKey() {
        return key;
    }

    public String getComment() {
        return comment;
    }

    public String[] getDescription() {
        return description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void reply(CommandSender cs, Object... formats) {
        reply(true, cs, formats);
    }

    public void reply(boolean flatten, CommandSender cs, Object... formats) {
        if (getValue().equals("{}")) return;
        String text = build(flatten, flatten(formats).toArray());
        if (text.contains("[noprefix]"))
            text = text.replace("[noprefix]", "");
        else
            text = prefix.getValue() + text;
        cs.sendMessage(Chat.colorize(text));
    }

    @Override public String toString() {
        return create();
    }

    public static void load() {
    }

    private static Message prefix;

    public static void setPrefix(Message prefix) {
        if (Message.prefix == null)
            Message.prefix = prefix;
    }
}
