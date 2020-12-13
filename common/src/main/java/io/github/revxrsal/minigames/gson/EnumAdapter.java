package io.github.revxrsal.minigames.gson;

import com.google.common.base.Enums;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.function.Function;

public class EnumAdapter<E extends Enum<E>> extends TypeAdapter<E> {

    private Function<String, E> from;
    private Function<E, String> to;
    private final Class<E> type;

    public EnumAdapter(Class<E> type) {
        this.type = type;
    }

    public EnumAdapter<E> from(Function<String, E> from) {
        if (this.from == null)
            this.from = from;
        return this;
    }

    public EnumAdapter<E> to(Function<E, String> to) {
        if (this.to == null)
            this.to = to;
        return this;
    }

    @Override public void write(JsonWriter out, E e) throws IOException {
        if (e == null)
            out.nullValue();
        else
            out.value(to == null ? e.name().toLowerCase() : to.apply(e));
    }

    @Override public E read(JsonReader in) throws IOException {
        return from != null ? from.apply(in.nextString()) : Enums.getIfPresent(type, in.nextString().toUpperCase()).or((E) null);
    }
}
