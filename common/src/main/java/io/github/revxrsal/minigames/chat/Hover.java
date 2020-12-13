package io.github.revxrsal.minigames.chat;

import io.github.revxrsal.minigames.util.Chat;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@ToString
@EqualsAndHashCode
public final class Hover {

    private final String value;
    private final String action;

    private Hover(String value, String action) {
        this.value = value;
        this.action = action;
    }

    public String getValue() {
        return value;
    }

    public String getAction() {
        return action;
    }

    public static Hover tooltip(@NotNull String text) {
        return new Hover(Chat.colorize(text), "show_text");
    }

    public static Hover of(@NotNull String action, @NotNull String text) {
        return new Hover(Chat.colorize(text), action);
    }

}
