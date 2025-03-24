//ModDisable
//A Minecraft Mod to disable other Mods
//Copyright (C) 2024 Kaloyan Ivanov Donev

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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kaloyandonev.moddisable.DisableModMain;
import com.kaloyandonev.moddisable.commands.Disable_Mod;
import com.kaloyandonev.moddisable.mixins.RecipeManagerAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.TickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@EventBusSubscriber(modid = DisableModMain.MODID, bus = EventBusSubscriber.Bus.MOD)
public class RecipeDisabler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<ResourceLocation> recipesToRemove = new ArrayList<>();
    private static final Map<ResourceLocation, Recipe<?>> removedRecipes = new HashMap<>();

    public static JsonArray previousJsonArray;

    public static void queueRecipeRemoval(String recipeId) {
        if (recipeId.equals("minecraft:air")) {
            LOGGER.warn("[Mod_Disable] recipeId is minecraft:air, discarding...");
        } else {
            recipesToRemove.add(recipeId);
        }

    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event){
        LOGGER.debug("Now hooking RecipeDisabler::onServerTick");
        NeoForge.EVENT_BUS.addListener(RecipeDisabler::onServerTick);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
        // Check that we're in the tick's end phase (if the event provides phases)
        MinecraftServer server = event.getServer();
        if (server != null) {
            removeQueuedRecipes(server);
        }
    }

    // V 1.1.0 Add cashing for RecipesField, so it isn't created every tick. This makes the Garbage collector happy :)
    private static Field cachedRecipesField;

    private static Field getRecipesField(RecipeManager recipeManager){
        if (cachedRecipesField == null) {
            cachedRecipesField = findRecipesField(recipeManager);
            if (cachedRecipesField != null) {
                cachedRecipesField.setAccessible(true);
            }
        }
        return cachedRecipesField;
    }
    // END

    private static void removeQueuedRecipes(MinecraftServer server) {
        if (recipesToRemove.isEmpty()) {
            return;
        }

        RecipeManager recipeManager = server.getRecipeManager();
        RecipeManagerAccessor accessor = (RecipeManagerAccessor) recipeManager;

        Map<ResourceLocation, RecipeHolder<?>> byNameMap = new HashMap<>(accessor.getByName());

        for (ResourceLocation recipeId : new ArrayList<>(byNameMap.keySet())) {
            RecipeHolder<?> recipeHolder = byNameMap.get(recipeId);
            if (recipesToRemove.contains(recipeHolder.value().toString())) {
                removedRecipes.put(recipeHolder.value(), recipeHolder.value());

                byNameMap.remove(recipeId);
                LOGGER.info("[Mod_Disable] Removed recipe: {}", recipeHolder.value());
            }
        }

        accessor.setByName(byNameMap);
        recipesToRemove.clear();
    }

    private static void removeQueuedRecipes(MinecraftServer server) {
        if (recipesToRemove.isEmpty()) {
            return;
        }

        RecipeManager recipeManager = server.getRecipeManager();
        RecipeManagerAccessor accessor = (RecipeManagerAccessor) recipeManager;

        // Retrieve the current recipe map
        Map<ResourceLocation, RecipeHolder<?>> byNameMap = new HashMap<>(accessor.getByName());

        // Iterate over the recipes to remove
        for (ResourceLocation recipeId : recipesToRemove) {
            RecipeHolder<?> recipeHolder = byNameMap.remove(recipeId);
            if (recipeHolder != null) {
                // Use the recipe's ResourceLocation as the key
                removedRecipes.put(recipeId, recipeHolder.value());
                LOGGER.info("[Mod_Disable] Removed recipe: {}", recipeId);
            }
        }

        // Update the RecipeManager with the modified map
        accessor.setByName(byNameMap);
        recipesToRemove.clear();
    }


    public static void queueRecipeRemovalFromJson(String jsonFilePath) {
        JsonArray jsonArray = JsonHelper.readJsonArrayFromFile(jsonFilePath);
        previousJsonArray = jsonArray;

        if (jsonArray != null) {
            for (JsonElement element : jsonArray) {
                String recipeId = element.getAsString();
                if (!recipesToRemove.contains(recipeId)) {
                    if (recipeId.equals("minecraft:air")){
                        LOGGER.warn("[Mod_Disable] Item ID to disable is minecraft:air, discarding...");
                    } else {
                        queueRecipeRemoval(recipeId);
                    }

                }

            }
        }
    }

    public static void enableAllRecipes(MinecraftServer server){
        List<Map.Entry<String, Recipe<?>>> toReEnable = new ArrayList<>(removedRecipes.entrySet());

        for (Map.Entry<String, Recipe<?>> entry : toReEnable) {
            enableRecipe(entry.getKey(), server);
        }
    }

    @SuppressWarnings (value="unused")
    public static void enableRecipesByNamespace(String namespace, MinecraftServer server, Player player){

        RecipeManager recipeManager = server.getRecipeManager();
        try{

            //Non-production but working way to get recipesField: Field recipesField = RecipeManager.class.getDeclaredField("recipes");

            Field recipesField = findRecipesField(recipeManager);

            recipesField.setAccessible(true);
            @SuppressWarnings (value="unchecked")
            Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes = (Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>>) recipesField.get(recipeManager);
            ResourceLocation recipeId;
            Item item;

            for (Map<ResourceLocation, Recipe<?>> recipeMap : recipes.values()) {
                for (Recipe<?> recipe : recipeMap.values()) {
                    if (recipe.getId().getNamespace().equals(namespace)) {
                        recipeId = recipe.getId();
                        item = BuiltInRegistries.ITEM.get(recipeId);

                        JsonHelper.enableItem(item, player);
                        LOGGER.info("[Mod_Disable] Enabled item {}", item);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void removeItemsByNamespace(String namespace, Player player) {
        File playerFile = JsonHelper.getPlayerFile(player);
        JsonObject data = JsonHelper.readPlayerData(playerFile);

        if (data.has("disabled_items")) {
            LOGGER.debug("[Mod_Disable] removedItemsByNamespace data.has disabled_items if was passed!");
            JsonArray disabledItems = data.getAsJsonArray("disabled_items");
            JsonArray newDisabledItems = new JsonArray();

            for (JsonElement element : disabledItems) {
                LOGGER.debug("namespace + : if passed!");
                if (!element.getAsString().startsWith(namespace + ":")) {
                    newDisabledItems.add(element);
                }
            }

            data.add("disabled_items", newDisabledItems);
            JsonHelper.writePlayerData(playerFile, data);
            LOGGER.info("[Mod_Disable] Removed items with namespace: {}", namespace);
        }
    }

    public static void disableRecipesByNamespace(String namespace, MinecraftServer server, Player player) {
        if (Disable_Mod.IsDebugEnabled) {
            LOGGER.info("[Mod_Disable] disableRecipesByNamespace called with namespace: {}", namespace);
        }

        RecipeManager recipeManager = server.getRecipeManager();
        try {
            //Non-production but working way to get recipesField: Field recipesField = RecipeManager.class.getDeclaredField("recipes");
            Field recipesField = findRecipesField(recipeManager);

            recipesField.setAccessible(true);
            LOGGER.debug("[Mod_Disable] Reflection on recipesField successful");
            Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes = (Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>>) recipesField.get(recipeManager);

            for (Map<ResourceLocation, Recipe<?>> recipeMap : recipes.values()) {
                for (Recipe<?> recipe : recipeMap.values()) {
                    ResourceLocation recipeId = recipe.getId();
                    if (recipeId.getNamespace().equals(namespace)) {
                        LOGGER.info("[Mod_Disable] Disabling recipe: {}", recipeId);
                        queueRecipeRemoval(recipeId.toString());
                    }
                }
            }

            for (Item item : BuiltInRegistries.ITEM) {
                ResourceLocation itemRegistryName = BuiltInRegistries.ITEM.getKey(item);
                if (itemRegistryName != null && itemRegistryName.getNamespace().equals(namespace)) {
                    LOGGER.info("[Mod_Disable] Disabling item: {}", itemRegistryName);
                    JsonHelper.disableItem(item, player);
                }
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings (value="unused")
    private static Field
    findRecipesField(RecipeManager recipeManager) {
        try {
            return RecipeManager.class.getDeclaredField("recipes");
        } catch (NoSuchFieldException e1) {
            try {
                return RecipeManager.class.getDeclaredField("f_44007_");
            } catch (NoSuchFieldException e2) {
                e2.printStackTrace();
            }
        }
        return null;
    }



}
