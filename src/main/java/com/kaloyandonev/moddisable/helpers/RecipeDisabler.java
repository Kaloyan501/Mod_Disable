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
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
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
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.*;
import java.io.File;


@Mod.EventBusSubscriber(modid = DisableModMain.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RecipeDisabler {
    private static final List<String> recipesToRemove = new ArrayList<>();
    private static final Map<String, Recipe<?>> removedRecipes = new HashMap<>();

    public static JsonArray previousJsonArray;

    public static void queueRecipeRemoval(String recipeId) {
        System.out.println("/////////////////////////queueRecipeRemoval is about to disable item: " + recipeId);
        recipesToRemove.add(recipeId);
        System.out.println(recipesToRemove);
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event){
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

    private static void removeQueuedRecipes(MinecraftServer server) {
        RecipeManager recipeManager = server.getRecipeManager();
        try {
            Field recipesField = findRecipesField(recipeManager);
            recipesField.setAccessible(true);
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
                        System.out.print("Removed recipe: " + recipe.getId());
                    }
                }

                for (ResourceLocation recipeId : toRemove) {
                    recipeMap.remove(recipeId);
                }

                newRecipes.put(entry.getKey(), recipeMap);
            }

            recipesField.set(recipeManager, newRecipes);

            recipesToRemove.clear();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void enableRecipe(String recipeId, MinecraftServer server) {
        RecipeManager recipeManager = server.getRecipeManager();
        Recipe<?> recipe = removedRecipes.get(recipeId);
        if (recipe != null) {
            try {
                Field recipesField = RecipeManager.class.getDeclaredField("recipes");
                recipesField.setAccessible(true);
                Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes = (Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>>) recipesField.get(recipeManager);

                //Create a new map to hold the modified recipes
                Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> newRecipes = new HashMap<>(recipes);

                newRecipes.computeIfAbsent(recipe.getType(), k -> new HashMap<>()).put(recipe.getId(), recipe);

                removedRecipes.remove(recipeId);
                recipesField.set(recipeManager, newRecipes);
                System.out.println("Re-enabled recipe: " + recipeId);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            System.out.print("Recipe not found: " + recipeId);
        }
    }

    public static void queueRecipeRemovalFromJson(String jsonFilePath) {
        JsonArray jsonArray = JsonHelper.readJsonArrayFromFile(jsonFilePath);
        previousJsonArray = jsonArray;

        if (jsonArray != null) {
            for (JsonElement element : jsonArray) {
                String recipeId = element.getAsString();
                if (!recipesToRemove.contains(recipeId)) {
                    System.out.println("////////////////// queueRecipeRemovalFromJson is about to give recipe " + recipeId + " to queueRecipeRemoval!");
                    queueRecipeRemoval(recipeId);
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


    public static void enableRecipesByNamespace(String namespace, MinecraftServer server, Player player){

        RecipeManager recipeManager = server.getRecipeManager();
        try{
            Field recipesField = RecipeManager.class.getDeclaredField("recipes");
            recipesField.setAccessible(true);
            Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes = (Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>>) recipesField.get(recipeManager);
            ResourceLocation recipeId;
            Item item;

            for (Map<ResourceLocation, Recipe<?>> recipeMap : recipes.values()) {
                for (Recipe<?> recipe : recipeMap.values()) {
                    if (recipe.getId().getNamespace().equals(namespace)) {
                        recipeId = recipe.getId();
                        item = ForgeRegistries.ITEMS.getValue(recipeId);

                        //if (item != null) {
                            JsonHelper.enableItem(item, player);
                            System.out.println("////Enabled item " + item);
                        //} else {
                            //System.out.println("Item not found for recipe: " + recipeId);
                        //}


                    }
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void removeItemsByNamespace(String namespace, Player player) {
        File playerFile = JsonHelper.getPlayerFile(player);
        JsonObject data = JsonHelper.readPlayerData(playerFile);

        if (data.has("disabled_items")) {
            System.out.println("////removedItemsByNamespace data.has disabled_items if was passed!");
            JsonArray disabledItems = data.getAsJsonArray("disabled_items");
            JsonArray newDisabledItems = new JsonArray();

            for (JsonElement element : disabledItems) {
                System.out.println("////namespace + : if passed!");
                if (!element.getAsString().startsWith(namespace + ":")) {
                    newDisabledItems.add(element);
                }
            }

            data.add("disabled_items", newDisabledItems);
            JsonHelper.writePlayerData(playerFile, data);
            System.out.println("Removed items with namespace: " + namespace);
        }
    }

    public static void disableRecipesByNamespace(String namespace, MinecraftServer server, Player player) {

        if (Disable_Mod.IsDebugEnabled) {
            System.out.println("disableRecipesByNamespace called with namespace: " + namespace);
        }

        RecipeManager recipeManager = server.getRecipeManager();
        try {
            Field recipesField = findRecipesField(recipeManager);
            recipesField.setAccessible(true);
            if (Disable_Mod.IsDebugEnabled) {
                System.out.println("Reflection on recipesField successful");
            }
            Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes = (Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>>) recipesField.get(recipeManager);

            for (Map<ResourceLocation, Recipe<?>> recipeMap : recipes.values()) {
                for (Recipe<?> recipe : recipeMap.values()) {
                    ResourceLocation recipeId = recipe.getId();
                    if (Disable_Mod.IsDebugEnabled) {
                        System.out.println("Checking recipe: " + recipeId);
                    }
                    if (recipeId.getNamespace().equals(namespace)) {
                        if (Disable_Mod.IsDebugEnabled) {
                            System.out.println("If recipe.getId().getNamespace().equals(namespace) passed!");
                            System.out.println("recipeId is: " + recipeId);
                        }
                        Item item = ForgeRegistries.ITEMS.getValue(recipeId);
                        if (Disable_Mod.IsDebugEnabled) {
                            System.out.println("Item is: " + item);
                        }
                        if (item != null) {
                            JsonHelper.disableItem(item, player);
                            queueRecipeRemoval(recipeId.toString());
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static Field findRecipesField(RecipeManager recipeManager) {
        try {
            return RecipeManager.class.getDeclaredField("recipes");
        } catch (NoSuchFieldException e) {
            // Try the obfuscated name (for example, in a production environment)
            for (Field field : RecipeManager.class.getDeclaredFields()) {
                if (Map.class.isAssignableFrom(field.getType())) {
                    return field;
                }
            }
        }
        return null;
    }



}
