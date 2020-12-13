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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonResponse {

    private final String responseText;
    private final JsonObject response;
    private final Gson gson;

    public JsonResponse(JsonObject json, Gson gson) {
        response = json;
        responseText = json.toString();
        this.gson = gson;
    }

    public JsonResponse(JsonObject json) {
        this(json, Gsons.DEFAULT);
    }

    public JsonResponse(String response, Gson gson) {
        this(JsonUtils.getObjectFromString(response, gson), gson);
    }

    public JsonResponse(String response) {
        this(response, Gsons.DEFAULT);
    }

    public <T> T get(String key, Type type) {
        return gson.fromJson(response.get(key), type);
    }

    public final <T> T getAs(Type type, Gson gson) {
        return gson.fromJson(response, type);
    }

    public final <T> T getAs(Type type) {
        return getAs(type, Gsons.DEFAULT);
    }

    public final String getString(String key) {
        return response.get(key).getAsString();
    }

    public final int getInt(String key) {
        return response.get(key).getAsInt();
    }

    public final double getDouble(String key) {
        return response.get(key).getAsDouble();
    }

    public final long getLong(String key) {
        return response.get(key).getAsLong();
    }

    public final float getFloat(String key) {
        return response.get(key).getAsFloat();
    }

    public final boolean getBoolean(String key) {
        return response.get(key).getAsBoolean();
    }

    public final <E> List<E> getList(String key) {
        Type type = new TypeToken<List<E>>() {
        }.getType();
        return get(key, type);
    }

    public final <K, V> Map<K, V> getMap(String key) {
        Type type = new TypeToken<LinkedHashMap<K, V>>() {
        }.getType();
        return get(key, type);
    }

    public boolean contains(String key) {
        return response.has(key);
    }

    public String getResponseText() {
        return responseText;
    }

    public JsonObject getResponse() {
        return response;
    }

}
