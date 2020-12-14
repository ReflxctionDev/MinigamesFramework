package io.github.revxrsal.minigames;

import com.google.gson.FieldNamingPolicy;
import io.github.revxrsal.minigames.config.MappedConfiguration;
import io.github.revxrsal.minigames.menu.InventoryUI;
import io.github.revxrsal.minigames.message.message.MessageManager;
import io.github.revxrsal.minigames.pluginlib.DependentJavaPlugin;
import io.github.revxrsal.minigames.pluginlib.PluginLib;
import io.github.revxrsal.minigames.pluginlib.Relocation;
import io.github.revxrsal.minigames.util.FileManager;
import io.github.revxrsal.minigames.util.Protocol;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static io.github.revxrsal.minigames.util.Utils.firstNotNull;
import static java.io.File.separator;
import static org.bukkit.Bukkit.getServer;

public abstract class MinigamePlugin {

    private static MinigamePlugin instance;
    public static final AtomicBoolean DISABLE = new AtomicBoolean(false);
    public static final AtomicBoolean MISSING_WE = new AtomicBoolean(false);
    private static final String DEF_SEPARATOR = new String(new char[]{'/'});

    public static final ExecutorService THREAD_POOL = new ForkJoinPool();
    public static final ScheduledExecutorService SCHEDULED_SERVICE = Executors.newSingleThreadScheduledExecutor();

    protected final FileManager fileManager;
    protected static MessageManager messageManager;
    protected final JavaPlugin plugin;
    protected final MappedConfiguration configFile;

    public MinigamePlugin(JavaPlugin plugin) {
        instance = this;
        this.plugin = plugin;
        fileManager = new FileManager(this);
        configFile = MappedConfiguration
                .fromEmbeddedFile(plugin, "config.yml")
                .gsonBuilder(b -> b.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE))
                .enableCaseInsensitiveEnumSerialization()
                .build();
    }

    public final void onLoad() {
        call(PreLoad.class);
        if (!DISABLE.get()) {
            for (String file : directories()) {
                fileManager.directory(file.replace(DEF_SEPARATOR, separator));
            }
            for (String file : bundledFiles()) {
                fileManager.embedded(file.replace(DEF_SEPARATOR, separator));
            }
        }
        messageManager = new MessageManager(this);
        call(InvokeLoad.class);
    }

    @SneakyThrows
    protected boolean downloadIfMissing(String plugin, String url) {
        if (Bukkit.getPluginManager().getPlugin(plugin) == null) {
            getLogger().info(StringUtils.capitalize(plugin) + " plugin not found. Downloading...");
            File pluginJAR = new File(getDataFolder(), ".." + separator + plugin + ".jar");

            URL website = new URL(url);
            URLConnection connection = website.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            connection.connect();

            ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
            FileOutputStream fos = new FileOutputStream(pluginJAR);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            Objects.requireNonNull(Bukkit.getPluginManager().loadPlugin(pluginJAR), "Plugin " + plugin + " not found.").onLoad();
            return true;
        }
        return false;
    }

    public YamlConfiguration getRelativeFile(@NotNull String name) {
        return YamlConfiguration.loadConfiguration(fileManager.embedded(name));
    }

    /**
     * Calls all methods with the specified annotation
     *
     * @param annotation Annotation to filter with
     */
    @SneakyThrows
    private void call(Class<? extends Annotation> annotation) {
        Method priorityMethod = annotation.getDeclaredMethod("value");
        List<Method> methods = new ArrayList<>();
        for (Method method : getClass().getDeclaredMethods()) {
            Annotation ann = method.getAnnotation(annotation);
            if (ann == null) continue;
            if (method.isAnnotationPresent(annotation)) {
                methods.add(method);
            }
        }
        methods.sort(Comparator.comparingInt(o -> {
            try {
                return (int) priorityMethod.invoke(o.getAnnotation(annotation));
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                return 0;
            }
        }));
        for (Method method : methods) {
            @Nullable RunAsync async = method.getAnnotation(RunAsync.class);
            if (!method.isAccessible()) method.setAccessible(true);
            if (async == null)
                try {
                    method.invoke(plugin);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    error("Failed to run callback method " + method.getName() + " in stage @" + annotation.getSimpleName());
                    e.getCause().printStackTrace();
                }
            else {
                SCHEDULED_SERVICE.schedule(() -> {
                    try {
                        method.invoke(plugin);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        warn("Failed to invoke method " + method.getName() + " asynchronously:");
                        (e.getCause() == null ? e : e.getCause()).printStackTrace();
                    }
                }, async.delay(), TimeUnit.MILLISECONDS);
            }
        }
    }

    public final void onEnable() {
        try {
            for (DownloadPlugin plugin : getClass().getAnnotationsByType(DownloadPlugin.class)) {
                try {
                    if (downloadIfMissing(plugin.name(), plugin.url()))
                        Bukkit.getPluginManager().enablePlugin(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin(plugin.name()), "Plugin " + plugin + " not found."));
                } catch (Exception e) {
                    DISABLE.set(true);
                    sneakyThrow(e);
                }
            }

            if (DISABLE.get()) {
                getLogger().severe("Unsupported server protocol: 1." + Protocol.EXACT);
                getLogger().severe("Please use one of the following: 1.8.8, 1.8.9, 1.12.2, 1.13.2, 1.14.X, 1.15.X for the plugin to function");
                Bukkit.getPluginManager().disablePlugin(plugin);
                return;
            }
            if (requiresWorldEdit() && MISSING_WE.get()) {
                String v = "6.1.9";
                String d = "https://dev.bukkit.org/projects/worldedit/files/2597538/download";
                if (Protocol.isNewerThan(13)) { // 1.13+
                    v = "7.0.0";
                    d = "https://dev.bukkit.org/projects/worldedit/files/2723275/download";
                }
                if (Protocol.EXACT >= 14.4) { // 1.14.4 or greater
                    v = "latest";
                    d = "https://dev.bukkit.org/projects/worldedit/files/latest";
                }
                getLogger().severe("No WorldEdit found. Please download WorldEdit (" + v + "), from " + d);
                getServer().getPluginManager().disablePlugin(plugin);
                DISABLE.set(true);
                return;
            }
            addListener(new InventoryUI.MenuListener());
            call(InvokeEnable.class);
            for (Object listener : listeners()) {
                addListener(listener);
            }
        } catch (Exception e) {
            try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
                e.printStackTrace(pw);
                File crashLog = new File(getDataFolder(), "crash.log");
                if (!crashLog.exists()) crashLog.createNewFile();
                FileWriter writer = new FileWriter(crashLog, false);
                writer.write(sw.toString());
                writer.close();
            } catch (IOException ioe) {
                throw new IllegalStateException(ioe);
            }
            error("Failed to enable plugin. Error has been dumped to /" + getName() + "/crash.log. Please send the file over on our Discord server for support.");
            DISABLE.set(true);
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    public final void onDisable() {
        if (DISABLE.get()) return;
        call(InvokeDisable.class);
    }

    protected YamlConfiguration loadFile(String name) {
        return YamlConfiguration.loadConfiguration(fileManager.embedded(name.replace(DEF_SEPARATOR, separator)));
    }

    protected List<Object> listeners() {
        return Collections.emptyList();
    }

    protected List<String> bundledFiles() {
        return Collections.emptyList();
    }

    protected List<String> directories() {
        return Collections.emptyList();
    }

    public void info(String... messages) {
        for (String message : messages)
            getLogger().info(message);
    }

    public void warn(String... messages) {
        for (String message : messages)
            getLogger().warning(message);
    }

    public void error(String... messages) {
        for (String message : messages)
            getLogger().severe(message);
    }

    public static void runAsync(Runnable task) {
        THREAD_POOL.submit(task);
    }

    public static <T> CompletableFuture<T> getAsync(Supplier<T> task) {
        return CompletableFuture.supplyAsync(task);
    }

    protected void addListener(Object listener) {
        if (listener instanceof Listener)
            Bukkit.getPluginManager().registerEvents((Listener) listener, plugin);
    }

    @SuppressWarnings("RedundantTypeArguments")
    public static RuntimeException sneakyThrow(@NotNull Throwable t) {
        return MinigamePlugin.<RuntimeException>sneakyThrow0(t);
    }

    private static <T extends Throwable> T sneakyThrow0(Throwable t) throws T {
        throw (T) t;
    }

    protected boolean requiresWorldEdit() {
        return true;
    }

    public static MinigamePlugin getInstance() {
        return instance;
    }

    public static void setPlugin(@NotNull MinigamePlugin plugin) {
        if (instance == null)
            instance = plugin;
    }

    /**
     * Invoked before the plugin or dependencies are loaded
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface PreLoad {

        /**
         * The priority of this method
         *
         * @return The priority
         */
        int value();

    }

    /**
     * Invoked after the dependencies have been loaded
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface InvokeLoad {

        /**
         * The priority of this method
         *
         * @return The priority
         */
        int value();

    }

    /**
     * Invoked when the plugin enables
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface InvokeEnable {

        /**
         * The priority of this method
         *
         * @return The priority
         */
        int value();

    }

    /**
     * Invoked when the plugin disables
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface InvokeDisable {

        /**
         * The priority of this method
         *
         * @return The priority
         */
        int value();

    }

    /**
     * Represents a plugin that should be downloaded if not already
     */
    @Repeatable(RuntimePlugins.class)
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DownloadPlugin {

        /**
         * Plugin name to download
         *
         * @return The plugin name
         */
        String name();

        /**
         * URL to download from
         *
         * @return The download URL
         */
        String url();
    }

    /**
     * Added to methods to indicate that they will be ran asynchronously
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RunAsync {

        /**
         * The delay to wait before running this task
         *
         * @return The delay
         */
        long delay() default 0;

    }

    /**
     * Repeatable store for {@link DownloadPlugin}
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface RuntimePlugins {

        DownloadPlugin[] value();

    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public static MessageManager getMessageManager() {
        return messageManager;
    }

    public MappedConfiguration getConfigFile() {
        return configFile;
    }

    public static void load(@NotNull Class<? extends DependentJavaPlugin> type) {
        PluginLib.builder()
                .groupId("com.google.code.gson")
                .artifactId("gson")
                .version("2.8.6")
                .relocate(new Relocation("com.google.gson", "io.github.revxrsal.minigames.libs.gson"))
                .build()
                .load(type);
        PluginLib.builder()
                .groupId("com.github.cryptomorin")
                .artifactId("XSeries")
                .version("7.6.1")
                .relocate(new Relocation("com.cryptomorin.xseries", "io.github.revxrsal.minigames.libs.xseries"))
                .build()
                .load(type);
        PluginLib.builder()
                .groupId("com.esotericsoftware")
                .artifactId("reflectasm")
                .version("1.11.9")
                .relocate(new Relocation("com.esotericsoftware", "io.github.revxrsal.minigames.libs.esoteric"))
                .build()
                .load(type);
    }

    static {
        try {
            firstNotNull(
                    Bukkit.getPluginManager().getPlugin("WorldEdit"),
                    Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit"),
                    Bukkit.getPluginManager().getPlugin("AsyncWorldEdit")
            );
        } catch (Throwable t) {
            MISSING_WE.set(true);
        }
    }

    public void saveResource(@NotNull String resourcePath, boolean replace) {
        plugin.saveResource(resourcePath, replace);
    }

    @NotNull public File getDataFolder() {
        return plugin.getDataFolder();
    }

    @NotNull public PluginDescriptionFile getDescription() {
        return plugin.getDescription();
    }

    @NotNull public Logger getLogger() {
        return plugin.getLogger();
    }

    @NotNull public String getName() {
        return plugin.getName();
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

}
