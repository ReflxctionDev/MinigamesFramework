package io.github.revxrsal.minigames.message.message;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Prefixable {

    Prefixable NONE = () -> "";

    @NotNull
    String getPrefix();

}
