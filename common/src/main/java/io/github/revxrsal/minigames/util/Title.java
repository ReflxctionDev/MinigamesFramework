package io.github.revxrsal.minigames.util;

import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.github.revxrsal.minigames.util.Placeholders.on;

/**
 * Represents a displayed title. This class is immutable, hence is thread-safe.
 */
@Getter
@ToString
public class Title {

    private final @Nullable String title, subtitle;

    private final int fadeIn, display, fadeOut;

    private Title(@Nullable String title, @Nullable String subtitle, int fadeIn, int display, int fadeOut) {
        this.title = title;
        this.subtitle = subtitle;
        this.fadeIn = fadeIn;
        this.display = display;
        this.fadeOut = fadeOut;
    }

    public Title withTitle(@Nullable String title) {
        return new Title(title, subtitle, fadeIn, display, fadeOut);
    }

    public Title withPlaceholders(@NotNull Object... p) {
        return new Title(on(title, p), on(subtitle, p), fadeIn, display, fadeOut);
    }

//    public void display(MatchPlayer player) {
//        player.title(this);
//    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private @Nullable String title, subtitle;
        private int fadeIn, display, fadeOut;
        private boolean enabled;

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder title(@Nullable String title) {
            this.title = title;
            return this;
        }

        public Builder subtitle(@Nullable String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public Builder fadeIn(int fadeIn) {
            this.fadeIn = fadeIn;
            return this;
        }

        public Builder display(int display) {
            this.display = display;
            return this;
        }

        public Builder fadeOut(int fadeOut) {
            this.fadeOut = fadeOut;
            return this;
        }

        public Title build() {
            return new Title(title, subtitle, fadeIn, display, fadeOut);
        }

        public ToggleableTitle buildToggleable() {
            return new ToggleableTitle(enabled, title, subtitle, fadeIn, display, fadeOut);
        }
    }

    public static class ToggleableTitle extends Title {

        private final boolean enabled;

        private ToggleableTitle(boolean enabled, @Nullable String title, @Nullable String subtitle, int fadeIn, int display, int fadeOut) {
            super(title, subtitle, fadeIn, display, fadeOut);
            this.enabled = enabled;
        }

        @Override public ToggleableTitle withTitle(@Nullable String title) {
            return new ToggleableTitle(enabled, title, getSubtitle(), getFadeIn(), getDisplay(), getFadeOut());
        }

        public ToggleableTitle withPlaceholders(@NotNull Object... p) {
            return new ToggleableTitle(enabled, on(getTitle(), p), on(getSubtitle(), p), getFadeIn(), getDisplay(), getFadeOut());
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

}