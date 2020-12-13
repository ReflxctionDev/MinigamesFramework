package io.github.revxrsal.minigames.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Primitives;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.revxrsal.minigames.gson.CaseInsensitiveEnumTypeAdapterFactory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

import static io.github.revxrsal.minigames.config.MappedConfiguration.Builder.n;
import static io.github.revxrsal.minigames.config.MappedConfiguration.SectionTypeAdapterFactory.mapType;
import static java.util.Objects.requireNonNull;

/**
 * A configuration designed to simplify accessing YML content as objects.
 * <p>
 * Instances should be constructed using either {@link #fromFile(File)} or {@link #fromEmbeddedFile(JavaPlugin, String)}.
 */
public class MappedConfiguration {

    private final Gson gson;
    private YamlConfiguration config;
    private final File file;

    private MappedConfiguration(Gson gson, YamlConfiguration config, File file) {
        this.gson = n(gson, "gson is null!");
        this.config = n(config, "config is null!");
        this.file = n(file, "file is null!");
    }

    /**
     * Reloads this configuration.
     *
     * @return This {@link MappedConfiguration} instance
     */
    public MappedConfiguration reload() {
        config = YamlConfiguration.loadConfiguration(file);
        return this;
    }

    /**
     * Saves this configuration file
     *
     * @throws IOException Thrown when the given file cannot be written to for
     *                     any reason.
     */
    public void save() throws IOException {
        config.save(file);
    }

    /**
     * Reads the object in the specified path and converts it to the assigned type.
     *
     * @param path Path of the data value
     * @param type Type to deserialize to
     * @param <T>  The required type
     * @return The read value, or null if nothing is found.
     */
    public <T> T get(String path, Type type) {
        return gson.fromJson(gson.toJsonTree(config.get(path)), type);
    }

    /**
     * Reads the whole configuration and converts it to the specified type
     *
     * @param type Type to convert to
     * @param <T>  The required type
     * @return The read value
     */
    public <T> T getContent(Type type) {
        return gson.fromJson(gson.toJsonTree(getMap(config)), type);
    }

    /**
     * Sets the content of this configuration as the specified object.
     */
    public void setContent(Object value) {
        setMap(config, gson.fromJson(gson.toJsonTree(value), mapType.getType()));
    }

    /**
     * Sets the data at the specified path to the desired object.
     *
     * @param path  Path to set in
     * @param value Value to set. Can be null.
     */
    public void set(String path, Object value) {
        config.set(path, convert(value));
    }

    /**
     * Saves this configuration and propagates any exceptions.
     */
    public void saveSilently() {
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the inner configuration instance.
     *
     * @return The configuration instance.
     */
    public YamlConfiguration getConfig() {
        return config;
    }

    /**
     * Creates a new {@link Builder} that is based on a file that is embedded
     * inside the plugin's JAR.
     *
     * @param plugin   The plugin instance
     * @param fileName The name of the file (with extension)
     * @return A new builder
     */
    public static Builder fromEmbeddedFile(JavaPlugin plugin, String fileName) {
        n(plugin, "plugin is null!");
        fileName = n(fileName, "fileName is null!").replace('/', File.separatorChar);
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists())
            plugin.saveResource(fileName, false);
        return new Builder(file);
    }

    /**
     * Creates a new builder from a pre-defined {@link File}.
     *
     * @param file File of the configuration
     * @return A new builder
     */
    public static Builder fromFile(File file) {
        return new Builder(file);
    }

    public static class Builder {

        private final GsonBuilder gson = new GsonBuilder()
                .registerTypeAdapterFactory(SectionTypeAdapterFactory.INSTANCE)
                .registerTypeAdapterFactory(StandardSerializableTypeAdapterFactory.INSTANCE);

        private File file;

        private Builder(File file) {
            this.file = n(file, "file is null!");
        }

        /**
         * Sets the file of the configuration
         *
         * @param file File to set
         * @return This builder instance.
         */
        public Builder file(File file) {
            this.file = n(file, "file is null!");
            return this;
        }

        /**
         * Applies actions to the internal GsonBuilder instance. Useful to apply certain settings
         * to the backing {@link GsonBuilder}.
         *
         * @param consumer Actions to apply on the GsonBuilder
         * @return This builder instance.
         */
        public Builder gsonBuilder(Consumer<GsonBuilder> consumer) {
            n(consumer, "consumer is null!").accept(gson);
            return this;
        }

        /**
         * Whether or not should {@link Enum} types be case-insensitive. For example,
         * <br><code>
         * material: sToNe
         * </code><br>
         * will get parsed as Material.STONE regardless of whether it was fully uppercase
         * or not.
         *
         * @return This builder instance.
         */
        public Builder enableCaseInsensitiveEnumSerialization() {
            gson.registerTypeAdapterFactory(CaseInsensitiveEnumTypeAdapterFactory.INSTANCE);
            return this;
        }

        /**
         * Creates a new {@link MappedConfiguration} from the specified settings
         *
         * @return The newly created configuration
         */
        public MappedConfiguration build() {
            return new MappedConfiguration(gson.create(), YamlConfiguration.loadConfiguration(file), file);
        }

        static <T> T n(T t, String err) {
            return requireNonNull(t, err);
        }

    }

    // type adapter factories

    static class SectionTypeAdapterFactory implements TypeAdapterFactory {

        static final SectionTypeAdapterFactory INSTANCE = new SectionTypeAdapterFactory();
        static final TypeToken<Map<String, Object>> mapType = new TypeToken<Map<String, Object>>() {
        };

        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            Class<?> rawType = typeToken.getRawType();
            if (!ConfigurationSection.class.isAssignableFrom(rawType)) return null;
            TypeAdapter<Map<String, Object>> delegate = gson.getDelegateAdapter(this, mapType);
            return new TypeAdapter<T>() {
                @Override public void write(JsonWriter out, T t) throws IOException {
                    ConfigurationSection section = (ConfigurationSection) t;
                    delegate.write(out, toMap(section));
                }

                @Override public T read(JsonReader in) throws IOException {
                    throw new UnsupportedEncodingException(); // we never read these.
                }
            };
        }

        private static Map<String, Object> toMap(ConfigurationSection section) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (Entry<String, Object> e : section.getValues(true).entrySet()) {
                if (e.getKey().contains(".")) continue;
                if (e.getValue() instanceof ConfigurationSection)
                    map.put(e.getKey(), toMap((ConfigurationSection) e.getValue()));
                else
                    map.put(e.getKey(), e.getValue());
            }
            return map;
        }

    }

    static class StandardSerializableTypeAdapterFactory implements TypeAdapterFactory {

        static final StandardSerializableTypeAdapterFactory INSTANCE = new StandardSerializableTypeAdapterFactory();

        @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            Class<?> rawType = typeToken.getRawType();
            if (!ConfigurationSerializable.class.isAssignableFrom(rawType)) return null;
            return new TypeAdapter<T>() {
                @Override public void write(JsonWriter out, T t) throws IOException {
                    if (t == null)
                        out.nullValue();
                    else
                        gson.toJson(((ConfigurationSerializable) t).serialize(), mapType.getType(), out);
                }

                @Override public T read(JsonReader in) throws IOException {
                    Map<String, Object> map = gson.fromJson(in, mapType.getType());
                    return (T) ConfigurationSerialization.deserializeObject(map, (Class<? extends ConfigurationSerializable>) rawType);
                }
            };
        }
    }

    private static Field mapField;

    static {
        try {
            mapField = MemorySection.class.getDeclaredField("map");
            mapField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Object> getMap(MemorySection section) {
        try {
            return (Map<String, Object>) mapField.get(section);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setMap(MemorySection section, Map<String, Object> newMap) {
        Map<String, Object> map = getMap(section);
        map.clear();
        map.putAll(newMap);
    }

    private Object convert(Object value) {
        if (value == null) return null;
        Class<?> type = value.getClass();
        if (type == String.class || Primitives.isPrimitive(type) || Primitives.isWrapperType(type))
            return value;
        if (type.isArray()) {
            List<Object> values = new ArrayList<>();
            for (Object o : ((Object[]) value)) {
                values.add(convert(o));
            }
            return values;
        }
        if (Collection.class.isAssignableFrom(type)) {
            List<Object> values = new ArrayList<>();
            for (Object o : ((Iterable<Object>) value)) {
                values.add(convert(o));
            }
            return values;
        }
        return gson.fromJson(gson.toJsonTree(value), mapType.getType());
    }

}