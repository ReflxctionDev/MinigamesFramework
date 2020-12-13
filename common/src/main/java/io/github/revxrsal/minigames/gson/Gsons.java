package io.github.revxrsal.minigames.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Gsons {

    /* Cannot be initiated */
    private Gsons() {
        throw new AssertionError(Gsons.class.getName() + " cannot be initiated");
    }

    /**
     * Represents an unmodified {@link Gson} profile.
     */
    public static final Gson DEFAULT = new GsonBuilder().disableHtmlEscaping().create();

    /**
     * Represents a {@link Gson} profile which does pretty printing when serializing (by fixing
     * indentation, etc.)
     */
    public static final Gson PRETTY_PRINTING = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

}
