package io.github.revxrsal.minigames.gson;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public class RestrictedAdapter implements TypeAdapterFactory {

    private final TypeAdapterFactory delegate;
    private final ImmutableSet<Type> allowedTypes;

    private RestrictedAdapter(TypeAdapterFactory delegate, ImmutableSet<Type> allowedTypes) {
        this.delegate = delegate;
        this.allowedTypes = allowedTypes;
    }

    @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        if (allowedTypes.contains(typeToken.getType())) return delegate.create(gson, typeToken);
        return null;
    }

    @NotNull
    public static Builder wraps(@NotNull TypeAdapterFactory factory) {
        return new Builder(factory);
    }

    public static final class Builder {

        private final TypeAdapterFactory delegate;
        private ImmutableSet.Builder<Type> types = new ImmutableSet.Builder<>();

        private Builder(TypeAdapterFactory delegate) {
            this.delegate = delegate;
        }

        public Builder restrict(@NotNull Type... types) {
            this.types.add(types);
            return this;
        }

        public Builder restrict(@NotNull TypeToken<?>... tokens) {
            for (TypeToken<?> token : tokens)
                types.add(token.getType());
            return this;
        }

        public RestrictedAdapter build() {
            return new RestrictedAdapter(delegate, types.build());
        }

    }

}
