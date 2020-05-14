/*
 * * Copyright 2019-2020 github.com/ReflxctionDev
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
package io.github.superkoth;

import net.moltenjson.utils.JsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SimplePlugin extends JavaPlugin {

    private ISimple simple;

    @Override
    public void onEnable() {
        String p = Bukkit.getServer().getClass().getPackage().getName().contains("12") ? "legacy" : "modern";
        System.out.println(new JsonBuilder().map("A", "B").build());
        try {
            simple = (ISimple) Class.forName("io.github.superkoth." + p + ".SimpleImpl").newInstance();
            simple.print();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        simple.print();
    }
}
