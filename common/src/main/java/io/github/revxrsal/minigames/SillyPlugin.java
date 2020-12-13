///*
// * * Copyright 2019-2020 github.com/ReflxctionDev
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package io.github.sillygames;
//
//import org.jetbrains.annotations.NotNull;
//
//public class SillyPlugin extends MinigamePlugin {
//
//    private final PluginRegistry pluginRegistry;
//
//    private static SillyPlugin plugin;
//
//    @InvokeEnable(1)
//    private void handle() {
//        pluginRegistry.registerListeners(this);
//        messageManager.load(false);
//    }
//
//    @NotNull
//    public static SillyPlugin getPlugin() {
//        return plugin;
//    }
//
//    @Override protected boolean requiresWorldEdit() {
//        return false;
//    }
//
//    {
//        plugin = this;
//        PluginRegistry reg;
//        try {
//            reg = (PluginRegistry) Class.forName("io.github.revxrsal.minigames._GeneratedPluginRegistry").newInstance();
//        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
//            e.printStackTrace();
//            throw new IllegalStateException();
//        }
//        pluginRegistry = reg;
//    }
//
//}
