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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

/**
 * Contains various helping methods for JSON
 */
public class JsonUtils {

    private static final JsonParser PARSER = new JsonParser();

    private JsonUtils() {
        throw new AssertionError(JsonUtils.class.getName() + " cannot be initiated!");
    }

    public static String setPretty(String json) {
        return Gsons.PRETTY_PRINTING.toJson(getElementFromString(json));
    }

    public static JsonElement getElementFromString(String json, Gson gson) {
        return gson.fromJson(json, ReflectiveTypes.ELEMENT_TYPE);
    }

    public static JsonElement getElementFromString(String json) {
        return PARSER.parse(json);
    }

    public static JsonObject getObjectFromString(String json, Gson gson) {
        return getElementFromString(json, gson).getAsJsonObject();
    }

    public static JsonObject getObjectFromString(String json) {
        return getObjectFromString(json, Gsons.DEFAULT);
    }

    public static JsonObject getObject(File file) throws IOException {
        return getObjectFromString(new String(Files.readAllBytes(file.toPath())));
    }

    public static Map<String, Object> toMap(String json) {
        return Gsons.DEFAULT.fromJson(json, ReflectiveTypes.MAP_TYPE);
    }

    public static Map<String, Object> toMap(JsonElement json) {
        return Gsons.DEFAULT.fromJson(json, ReflectiveTypes.MAP_TYPE);
    }

    public static List<Object> toList(String json) {
        return Gsons.DEFAULT.fromJson(json, ReflectiveTypes.LIST_TYPE);
    }

    public static List<Object> toList(JsonElement json) {
        return Gsons.DEFAULT.fromJson(json, ReflectiveTypes.LIST_TYPE);
    }
}
