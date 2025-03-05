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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.CheckForNull;


@Mod.EventBusSubscriber(modid = DisableModMain.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RecipeDisabler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<String> recipesToRemove = new ArrayList<>();
    private static final Map<String, Recipe<?>> removedRecipes = new HashMap<>();

    public static JsonArray previousJsonArray;

    public static void queueRecipeRemoval(String recipeId) {
        if (recipeId.equals("minecraft:air")) {
            LOGGER.warn("[Mod_Disable] recipeId is minecraft:air, discarding...");
        } else {
            recipesToRemove.add(recipeId);
        }

    }

    @SubscribeEvent
    @SuppressWarnings (value="unused")
    public static void onServerStarting(ServerStartingEvent event){
        LOGGER.debug("Now hooking RecipeDisabler::onServerTick");
        MinecraftForge.EVENT_BUS.addListener(RecipeDisabler::onServerTick);
    }

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            MinecraftServer server = event.getServer();
            //If something breaks add !recipesToRemove.isEmpty() && to the if below
            if (server != null) {
                removeQueuedRecipes(server);
            }
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
            //recipesToRemove list is empty, exiting early.
            return;
        }

        RecipeManager recipeManager = server.getRecipeManager();
        try {
            Field recipesField = getRecipesField(recipeManager);
            if (recipesField == null) {
                LOGGER.error("[Mod Disable] [CRITICAL] recipesField was not created! If you are seeing this error, report this issue to the Github repo!");
                return;
            }

            @SuppressWarnings (value="unchecked")
            Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes = (Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>>) recipesField.get(recipeManager);

            // Create a new map to hold the modified recipes
            Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> newRecipes = new HashMap<>();

            for (Map.Entry<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> entry : recipes.entrySet()) {
                Map<ResourceLocation, Recipe<?>> recipeMap = new HashMap<>(entry.getValue());

                List<ResourceLocation> toRemove = new ArrayList<>();

                for (Map.Entry<ResourceLocation, Recipe<?>> recipeEntry : recipeMap.entrySet()) {
                    Recipe<?> recipe = recipeEntry.getValue();
                    if (recipesToRemove.contains(recipe.getId().toString())) {
                        removedRecipes.put(recipe.getId().toString(), recipe);
                        toRemove.add(recipe.getId());
                        LOGGER.info("[Mod_Disable] Removed recipe: {}", recipe.getId());
                    }
                }

                if (!toRemove.isEmpty()) {
                    recipeMap.keySet().removeAll(toRemove);
                }
                newRecipes.put(entry.getKey(), recipeMap);


            }

            recipesField.set(recipeManager, newRecipes);

            recipesToRemove.clear();

            //V 1.0.1 - Add NullPointerException here, so no NullPointerException happens
        } catch (IllegalAccessException | NullPointerException er) {
            er.printStackTrace();
        }
    }

    public static void enableRecipe(String recipeId, MinecraftServer server) {
        RecipeManager recipeManager = server.getRecipeManager();
        Recipe<?> recipe = removedRecipes.get(recipeId);
        if (recipe != null) {
            try {
                //Non-production but working way to get recipesField: Field recipesField = RecipeManager.class.getDeclaredField("recipes");
                Field recipesField = findRecipesField(recipeManager);
                recipesField.setAccessible(true);
                @SuppressWarnings (value="unchecked")
                Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes = (Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>>) recipesField.get(recipeManager);

                //Create a new map to hold the modified recipes
                Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> newRecipes = new HashMap<>(recipes);

                newRecipes.computeIfAbsent(recipe.getType(), k -> new HashMap<>()).put(recipe.getId(), recipe);

                removedRecipes.remove(recipeId);
                recipesField.set(recipeManager, newRecipes);
                LOGGER.info("[Mod_Disable] Re-enabled recipe: {}", recipeId);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            LOGGER.info("[Mod_Disable] Recipe not found: {}", recipeId);
        }
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
                        item = ForgeRegistries.ITEMS.getValue(recipeId);


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

            for (Item item : ForgeRegistries.ITEMS) {
                ResourceLocation itemRegistryName = ForgeRegistries.ITEMS.getKey(item);
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
