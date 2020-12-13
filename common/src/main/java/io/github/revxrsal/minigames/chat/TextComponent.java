package io.github.revxrsal.minigames.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.revxrsal.minigames.util.Chat;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.StringJoiner;

import static io.github.revxrsal.minigames.util.Utils.n;

@SuppressWarnings({"FieldCanBeLocal", "RedundantSuppression", "unused"})
public class TextComponent {

    private static final String COLOR_CHAR = Character.toString(ChatColor.COLOR_CHAR);

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private final String text;
    private final Hover hoverEvent;
    private final Click clickEvent;

    private transient final String asJson;

    private TextComponent(String text, Hover hoverEvent, Click clickEvent) {
        this.text = text;
        this.hoverEvent = hoverEvent;
        this.clickEvent = clickEvent;
        asJson = GSON.toJson(this);
    }

    @Override public String toString() {
        return asJson;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static String wrap(@NotNull TextComponent... components) {
        return Arrays.toString(components);
    }

    public static String fixColors(String e) {
        if (e.equals(" ")) return " ";
        StringJoiner builder = new StringJoiner(" ");
        StringJoiner mock = new StringJoiner(" ");
        for (String part : e.split("\\s")) {
            mock.add(part);
            if (part.startsWith(COLOR_CHAR)) {
                builder.add(part);
                continue;
            }
            part = ChatColor.getLastColors(mock.toString()) + part;
            builder.add(part);
        }
        String result = builder.toString();
        if (e.startsWith(" ")) result = " " + result;
        if (e.endsWith(" ")) result += " ";
        return result;
    }

    public static class Builder {

        private String message = "";
        private Hover hover = null;
        private Click click = null;

        public Builder message(@NotNull String message) {
            this.message = n(Chat.colorize(message), "text");
            return this;
        }

        public Builder hover(@NotNull Hover hover) {
            this.hover = n(hover, "hoverEvent");
            return this;
        }

        public Builder click(@NotNull Click click) {
            this.click = n(click, "clickEvent");
            return this;
        }

        public Builder fixColors() {
            this.message = TextComponent.fixColors(message);
            return this;
        }

        public TextComponent build() {
            return new TextComponent(message, hover, click);
        }

    }

}
