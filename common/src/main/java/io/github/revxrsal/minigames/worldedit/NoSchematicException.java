package io.github.revxrsal.minigames.worldedit;

public class NoSchematicException extends Exception {

    public NoSchematicException(String schematic) {
        super("Schematic " + schematic + ".schem does not exist!");
    }

}
