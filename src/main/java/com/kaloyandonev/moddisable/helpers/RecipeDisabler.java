package com.kaloyandonev.moddisable.helpers;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class RecipeDisabler {

    public static void RecipeRemovalFromJson(String itemId, String playerUUID){
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        Path serverDir = server.getWorldPath(LevelResource.ROOT);

        Path jsonPath = serverDir.resolve(playerUUID + ".json");

        try {
            String json = Files.readString(jsonPath, StandardCharsets.UTF_8);
            String[] disabledItems = new Gson().fromJson(json, String[].class);
            List<String> list = new ArrayList<>(Arrays.asList(disabledItems));
            list.remove(itemId);

            JsonObject root = new JsonObject();
            JsonArray arr = new JsonArray();
            for (String id : list) arr.add(id);
            root.add("disabled_items", arr);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            try (BufferedWriter writer = Files.newBufferedWriter(jsonPath, StandardCharsets.UTF_8)) {
                gson.toJson(root, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void RecipeAdditionFromJson(String itemId, String playerUUID) {
        
    }

    public static void RecipeRemoval_NameSpace_FromJson(String namespace, String playerUUID) {
        Registry<Item> itemRegistry = BuiltInRegistries.ITEM;

        for (ResourceLocation key : itemRegistry.keySet()) {
            if (key.getNamespace().startsWith(namespace)) {
                Item item = itemRegistry.get(key);

                RecipeRemovalFromJson(key.toString(), playerUUID);
            }
        }
    }

}
