/*
 * * Copyright 2020 github.com/moltenjson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.revxrsal.minigames.gson;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * A utility to create and build JSON text flexibly
 */
public class JsonBuilder {

    private final Map<String, Object> jsonMap;

    public JsonBuilder(boolean order) {
        jsonMap = order ? new LinkedHashMap<>() : new HashMap<>();
    }

    public JsonBuilder() {
        this(true);
    }

    public JsonBuilder(Map<String, Object> jsonMap) {
        this.jsonMap = jsonMap;
    }

    public JsonBuilder map(String key, Object value) {
        jsonMap.put(key, value == null ? JsonNull.INSTANCE : value);
        return this;
    }

    public JsonBuilder map(Object key, Object value) {
        return map(key.toString(), value);
    }

    public JsonBuilder mapIf(boolean expression, String key, Object value) {
        if (expression)
            return map(key, value);
        return this;
    }

    public <T> JsonBuilder mapIf(Predicate<T> predicate, String key, T value) {
        return mapIf(predicate == null || predicate.test(value), key, value);
    }

    public JsonBuilder mapIfNotNull(String key, Object value) {
        return mapIf(Objects::nonNull, key, value);
    }

    public JsonBuilder mapIfAbsent(String key, Object value) {
        return mapIf(o -> jsonMap.containsKey(key), key, value);
    }

    public JsonBuilder removeKey(String key) {
        jsonMap.remove(key);
        return this;
    }

    public Map<String, Object> getJsonMap() {
        return jsonMap;
    }

    public String build() {
        return build(Gsons.DEFAULT);
    }

    public String buildPretty() {
        return build(Gsons.PRETTY_PRINTING);
    }

    public String build(@NotNull Gson profile) {
        return profile.toJson(jsonMap);
    }

    public JsonObject buildJsonObject() {
        return buildJsonElement().getAsJsonObject();
    }

    public JsonElement buildJsonElement() {
        return JsonUtils.getElementFromString(build());
    }

}
