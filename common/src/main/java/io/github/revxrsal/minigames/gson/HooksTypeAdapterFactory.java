package io.github.revxrsal.minigames.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.revxrsal.minigames.gson.GsonHook.AfterDeserialization;
import io.github.revxrsal.minigames.gson.GsonHook.AfterSerialization;
import io.github.revxrsal.minigames.gson.GsonHook.BeforeSerialization;
import io.github.revxrsal.minigames.MinigamePlugin;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class HooksTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Class<? super T> type = typeToken.getRawType();
        if (!type.isAnnotationPresent(GsonHook.class)) return null;
        TypeAdapter<T> delegate = gson.getDelegateAdapter(this, typeToken);


        List<Method> beforeSerialization = getAllMethods(type, BeforeSerialization.class);
        List<Method> postSerialization = getAllMethods(type, AfterSerialization.class);
        List<Method> postDeserialization = getAllMethods(type, AfterDeserialization.class);

        return new TypeAdapter<T>() {
            @Override public void write(JsonWriter jsonWriter, T t) throws IOException {
                invokeAll(beforeSerialization, t, "pre-serialization");
                delegate.write(jsonWriter, t);
                invokeAll(postSerialization, t, "post-serialization");
            }

            @Override public T read(JsonReader jsonReader) throws IOException {
                T value = delegate.read(jsonReader);
                invokeAll(postDeserialization, value, "post-deserialization");
                return value;
            }
        };
    }

    public static List<Method> getAllMethods(Class<?> type, Class<? extends Annotation> annotation) {
        List<Method> methods = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.isAnnotationPresent(annotation)) {
                    m.setAccessible(true);
                    methods.add(m);
                }
            }
        }
        methods.sort(Comparator.comparingInt(o -> getPriority(o, annotation)));
        return methods;
    }

    private static void invokeAll(List<Method> methods, Object instance, String label) {
        for (Method method : methods) {
            try {
                method.invoke(instance);
            } catch (InvocationTargetException | IllegalAccessException e) {
                MinigamePlugin.getPlugin().info("Failed to invoke " + label + " callback method in " + instance.getClass() + ":");
                e.getCause().printStackTrace();
            }
        }
    }

    private static final Map<Class<?>, Method> priorities = Collections.synchronizedMap(new HashMap<>());

    private static int getPriority(Method method, Class<? extends Annotation> annotation) {
        try {
            return (int) priorities.computeIfAbsent(annotation, type -> {
                try {
                    return type.getDeclaredMethod("priority");
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException("Type " + type + " does not define a 'priority' method!");
                }
            }).invoke(method.getAnnotation(annotation));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return 0;
        }
    }

}
