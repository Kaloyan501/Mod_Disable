//ModDisable
//A Minecraft Mod to disable other Mods
//Copyright (C) 2024-2025 Kaloyan Ivanov Donev

//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.
package com.kaloyandonev.moddisable.helpers;

import com.google.gson.*;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.references.Blocks;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.intellij.lang.annotations.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class RecipeDisabler {

    private static Path FindPlayerFolder(String playerUUID) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            throw new IllegalStateException("Server not available yet.");
        }

        // This returns an absolute path, no matter the working directory:
        Path worldRoot = server.getWorldPath(LevelResource.ROOT).toAbsolutePath();
        System.out.println("Resolved world root: " + worldRoot);

        Path playerFile = worldRoot
                .resolve("Mod_Disable_Data")
                .resolve(playerUUID + ".json");

        try {
            Files.createDirectories(playerFile.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return playerFile;
    }


    /**
     * Function to read the disabled items JSON list of the player or create it if not available,
     * and remove the specified item from the array (i.e., enable it).
     *
     * @param playerUUID - UUID of the player, used to get the disabled items JSON file.
     * @param itemToEnable - The item that is going to be enabled (removed from the disabled list).
     * @throws IOException
     */
    public static void EnableItem(String playerUUID, String itemToEnable) throws IOException {
        Path path = FindPlayerFolder(playerUUID);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject jObj;

        // Load or initialize JSON file
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                jObj = JsonParser.parseReader(reader).getAsJsonObject();
            }
        } else {
            jObj = new JsonObject();
            jObj.add("disabled_items", new JsonArray());
        }

        // Ensure the array exists
        JsonArray arr = jObj.getAsJsonArray("disabled_items");
        if (arr == null) {
            arr = new JsonArray();
            jObj.add("disabled_items", arr);
        }

        // Remove item safely (iterate backward)
        for (int i = arr.size() - 1; i >= 0; i--) {
            if (arr.get(i).getAsString().equals(itemToEnable)) {
                arr.remove(i);
            }
        }

        // Save changes
        try (Writer writer = Files.newBufferedWriter(path)) {
            gson.toJson(jObj, writer);
        }
    }


    /**
     * Function to read the disabled items JSON list of the player or create it if not available and add the string to the array.
     * @param playerUUID - UUID of the player, used to get the disabled items list JSON file.
     * @param ItemToDisable - The item that is going to get disabled.
     * @throws IOException
     */


    public static void DisableItem(String playerUUID, String ItemToDisable) throws IOException{
        Path path = FindPlayerFolder(playerUUID);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonObject jObj;
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                jObj = JsonParser.parseReader(reader).getAsJsonObject();
            }
        } else {
            jObj = new JsonObject();
            jObj.add("disabled_items", new JsonArray());
        }

        JsonArray arr = jObj.getAsJsonArray("disabled_items");
        String newItem = ItemToDisable;
        boolean found = false;
        for (JsonElement e : arr) {
            if (newItem.equals(e.getAsString())) {
                found = true;
                break;
            }
        }
        if (!found) {
            arr.add(newItem);
        }

        try (Writer writer = Files.newBufferedWriter(path)) {
            gson.toJson(jObj, writer);
        }

    }

    public static void DisableNamespace(String playerUUID, String namespace) throws IOException{
        Path path = FindPlayerFolder(playerUUID);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonObject jObj = null;
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                jObj = JsonParser.parseReader(reader).getAsJsonObject();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            jObj = new JsonObject();
            jObj.add("disabled_items", new JsonArray());
        }

        JsonArray arr;
        arr = jObj.getAsJsonArray("disabled_items");


        BuiltInRegistries.ITEM.forEach(item -> {
            boolean found = false;
            String id = BuiltInRegistries.ITEM.getKey(item).toString();
            if (id.split(":")[0].contains(namespace)) {
                for (JsonElement e: arr) {
                    if (id.equals(e.getAsString())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    arr.add(id);
                }
            }
            found = false;
        });

        try (Writer writer = Files.newBufferedWriter(path)) {
            gson.toJson(jObj, writer);
        }

    }

    public static void EnableNamespace(String playerUUID, String namespace) throws IOException {
        Path path = FindPlayerFolder(playerUUID);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject jObj;

        // 1. Load or initialize the JSON object
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                jObj = JsonParser.parseReader(reader).getAsJsonObject();
            }
        } else {
            jObj = new JsonObject();
            jObj.add("disabled_items", new JsonArray());
        }

        // 2. Ensure we have a JsonArray to work with
        JsonArray arr = jObj.getAsJsonArray("disabled_items");
        if (arr == null) {
            arr = new JsonArray();
            jObj.add("disabled_items", arr);
        }

        // 3. Gather all IDs in that namespace
        Set<String> toRemove = new HashSet<>();
        BuiltInRegistries.ITEM.forEach(item -> {
            String fullId = BuiltInRegistries.ITEM.getKey(item).toString();        // "modid:item_name"
            String ns = fullId.substring(0, fullId.indexOf(':'));                  // "modid"
            if (ns.equals(namespace)) {
                toRemove.add(fullId);
            }
        });

        // 4. Remove matching entries by iterating backwards
        //    JsonArray.remove(int index) is safe when counting down
        for (int i = arr.size() - 1; i >= 0; i--) {
            JsonElement elem = arr.get(i);
            // JsonElement.getAsString() is available if you import com.google.gson.JsonElement
            if (toRemove.contains(elem.getAsString())) {
                arr.remove(i);
            }
        }

        // 5. Persist changes back to disk
        try (Writer writer = Files.newBufferedWriter(path)) {
            gson.toJson(jObj, writer);
        }
    }



}
