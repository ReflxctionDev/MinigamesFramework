package io.github.revxrsal.minigames.chat;

import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.revxrsal.minigames.packet.ChatPacket;
import io.github.revxrsal.minigames.util.Chat;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
    private final transient WrappedChatComponent component;

    private TextComponent(String text, Hover hoverEvent, Click clickEvent) {
        this.text = text;
        this.hoverEvent = hoverEvent;
        this.clickEvent = clickEvent;
        asJson = GSON.toJson(this);
        component = WrappedChatComponent.fromJson(asJson);
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

    public void send(@NotNull CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(text);
            return;
        }
        ChatPacket packet = new ChatPacket();
        packet.setMessage(component);
        packet.setChatType(ChatType.SYSTEM);
        packet.sendPacket((Player) sender);
    }

    public void actionBar(@NotNull CommandSender sender) {
        if (!(sender instanceof Player)) return;
        ChatPacket packet = new ChatPacket();
        packet.setMessage(component);
        packet.setChatType(ChatType.GAME_INFO);
        packet.sendPacket((Player) sender);
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
