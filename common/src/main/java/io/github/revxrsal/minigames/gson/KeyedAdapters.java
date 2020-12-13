package io.github.revxrsal.minigames.gson;

import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.revxrsal.minigames.gson.Keyed.ValueOf;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public final class KeyedAdapters {

    public static final ToStringAdapter STRING = new ToStringAdapter();
    public static final ToStringCollection COLLECTION = new ToStringCollection();
    public static final ToStringKeyMap MAP_KEY = new ToStringKeyMap();
    public static final ToStringValueMap MAP_VALUE = new ToStringValueMap();

    private static final Map<Class<?>, Method> valueOfMethods = new HashMap<>();

    public static class ToStringAdapter implements TypeAdapterFactory {

        @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!Keyed.class.isAssignableFrom(type.getRawType())) return null;
            Class<?> keyedType = type.getRawType();
            return new TypeAdapter<T>() {
                @Override public void write(JsonWriter out, T value) throws IOException {
                    out.value(((Keyed) value).getKey());
                }

                @Override public T read(JsonReader in) throws IOException {
                    return get(keyedType, in.nextString());
                }
            };
        }
    }

    public static class ToStringCollection implements TypeAdapterFactory {

        @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            Type type = typeToken.getType();
            Type[] params = getParameters(type);
            Class<?> raw = typeToken.getRawType();
            if (Collection.class.isAssignableFrom(raw)) {
                Class<?> keyType = TypeToken.get(params[0]).getRawType();
                if (Keyed.class.isAssignableFrom(TypeToken.get(params[0]).getRawType())) {
                    return new TypeAdapter<T>() {
                        @Override public void write(JsonWriter out, T t) throws IOException {
                            Collection<Keyed> coll = (Collection<Keyed>) t;
                            out.beginArray();
                            for (Keyed keyed : coll) {
                                out.value(keyed.getKey());
                            }
                            out.endArray();
                        }

                        @Override public T read(JsonReader in) throws IOException {
                            Collection<? extends Keyed> coll = (Collection<? extends Keyed>) getConstructor(type, raw).construct();
                            in.beginArray();
                            while (in.hasNext()) {
                                String key = in.nextString();
                                coll.add(get(keyType, key));
                            }
                            in.endArray();
                            return (T) coll;
                        }
                    };
                }
            }
            return null;
        }
    }

    public static class ToStringKeyMap implements TypeAdapterFactory {

        @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            Type type = typeToken.getType();
            Type[] params = getParameters(type);
            Class<?> raw = typeToken.getRawType();
            if (Map.class.isAssignableFrom(raw)) {
                TypeToken<?> keyType = TypeToken.get(params[0]);
                TypeToken<?> valueType = TypeToken.get(params[1]);
                TypeAdapter delegate = gson.getAdapter(valueType);
                if (Keyed.class.isAssignableFrom(keyType.getRawType())) {
                    return new TypeAdapter<T>() {
                        @Override public void write(JsonWriter out, T t) throws IOException {
                            Map<Keyed, Object> coll = (Map<Keyed, Object>) t;
                            out.beginObject();
                            for (Entry<Keyed, Object> entry : coll.entrySet()) {
                                out.name(entry.getKey().getKey());
                                delegate.write(out, entry.getValue());
                            }
                            out.endObject();
                        }

                        @Override public T read(JsonReader in) throws IOException {
                            Map<Keyed, Object> map = (Map<Keyed, Object>) getConstructor(type, raw).construct();
                            in.beginObject();
                            while (in.hasNext()) {
                                Keyed key = get(keyType.getRawType(), in.nextName());
                                Object value = stream(valueType.getRawType(), in, delegate);
                                Object replaced = map.put(key, value);
                                if (replaced != null) {
                                    throw new JsonSyntaxException("duplicate key: " + key);
                                }
                            }
                            in.endObject();
                            return (T) map;
                        }
                    };
                }
            }
            return null;
        }

        private static Object stream(Type type, JsonReader in, TypeAdapter<?> delegate) throws IOException {
            if (type == String.class)
                return in.nextString();
            if (type == int.class || type == Integer.class)
                return in.nextInt();
            if (type == boolean.class || type == Boolean.class)
                return in.nextBoolean();
            if (type == double.class || type == Double.class)
                return in.nextDouble();
            if (type == long.class || type == Long.class)
                return in.nextLong();
            return delegate.read(in);
        }
    }

    public static class ToStringValueMap implements TypeAdapterFactory {

        @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            Type type = typeToken.getType();
            Type[] params = getParameters(type);
            Class<?> raw = typeToken.getRawType();
            if (Map.class.isAssignableFrom(raw)) {
                TypeToken<?> valueType = TypeToken.get(params[1]);
                if (Keyed.class.isAssignableFrom(valueType.getRawType())) {
                    return new TypeAdapter<T>() {
                        @Override public void write(JsonWriter out, T t) throws IOException {
                            Map<String, Keyed> coll = (Map<String, Keyed>) t;
                            out.beginObject();
                            for (Entry<String, Keyed> entry : coll.entrySet()) {
                                out.name(entry.getKey()).value(entry.getValue().getKey());
                            }
                            out.endObject();
                        }

                        @Override public T read(JsonReader in) throws IOException {
                            Map<String, Keyed> map = (Map<String, Keyed>) getConstructor(type, raw).construct();
                            in.beginObject();
                            while (in.hasNext()) {
                                String key = in.nextName();
                                Keyed value = get(valueType.getRawType(), in.nextString());
                                Keyed replaced = map.put(key, value);
                                if (replaced != null) {
                                    throw new JsonSyntaxException("duplicate key: " + key);
                                }
                            }
                            in.endObject();
                            return (T) map;
                        }
                    };
                }
            }
            return null;
        }
    }

    private static final Type[] DEF = new Type[]{Object.class, Object.class, Object.class};

    private static Type[] getParameters(Type type) {
        if (!(type instanceof ParameterizedType)) return DEF;
        return ((ParameterizedType) type).getActualTypeArguments();
    }

    /**
     * Constructors for common interface types like Map and List and their
     * subtypes.
     */
    private static <T> ObjectConstructor<T> getConstructor(final Type type, Class<?> rawType) {
        if (Collection.class.isAssignableFrom(rawType)) {
            if (SortedSet.class.isAssignableFrom(rawType)) {
                return () -> (T) new TreeSet<>();
            } else if (EnumSet.class.isAssignableFrom(rawType)) {
                return () -> {
                    if (type instanceof ParameterizedType) {
                        Type elementType = ((ParameterizedType) type).getActualTypeArguments()[0];
                        if (elementType instanceof Class) {
                            return (T) EnumSet.noneOf((Class) elementType);
                        } else {
                            throw new JsonIOException("Invalid EnumSet type: " + type.toString());
                        }
                    } else {
                        throw new JsonIOException("Invalid EnumSet type: " + type.toString());
                    }
                };
            } else if (Set.class.isAssignableFrom(rawType)) {
                return () -> (T) new LinkedHashSet<>();
            } else if (Queue.class.isAssignableFrom(rawType)) {
                return () -> (T) new ArrayDeque<>();
            } else {
                return () -> (T) new ArrayList<>();
            }
        }

        if (Map.class.isAssignableFrom(rawType)) {
            if (ConcurrentNavigableMap.class.isAssignableFrom(rawType)) {
                return () -> (T) new ConcurrentSkipListMap<>();
            } else if (ConcurrentMap.class.isAssignableFrom(rawType)) {
                return () -> (T) new ConcurrentHashMap<>();
            } else if (SortedMap.class.isAssignableFrom(rawType)) {
                return () -> (T) new TreeMap<>();
            } else if (type instanceof ParameterizedType && !(String.class.isAssignableFrom(
                    TypeToken.get(((ParameterizedType) type).getActualTypeArguments()[0]).getRawType()))) {
                return () -> (T) new LinkedHashMap<>();
            } else {
                return () -> (T) new LinkedTreeMap<String, Object>();
            }
        }

        return null;
    }

    public static <K extends Keyed> K get(Class<?> type, String key) {
        try {
            return (K) valueOfMethods.computeIfAbsent(type, (token) -> {
                Method valueOf = Arrays.stream(token.getDeclaredMethods())
                        .filter(m -> Modifier.isStatic(m.getModifiers()))
                        .filter(m -> m.isAnnotationPresent(ValueOf.class))
                        .findFirst().orElseThrow(() -> new IllegalStateException("Class " + type.getName() + " does not declare a static method with " + ValueOf.class + " annotation!"));
                if (valueOf.getParameterCount() != 1)
                    throw new IllegalStateException("Invalid parameter count");
                if (valueOf.getParameterTypes()[0] != String.class)
                    throw new IllegalStateException("First parameter must be " + String.class + "!");
                if (!Keyed.class.isAssignableFrom(valueOf.getReturnType()))
                    throw new IllegalStateException("Return type must be of " + Keyed.class + " or a subclass!");
                if (!valueOf.isAccessible())
                    valueOf.setAccessible(true);
                return valueOf;
            }).invoke(null, key);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e.getCause());
        }
    }

}
