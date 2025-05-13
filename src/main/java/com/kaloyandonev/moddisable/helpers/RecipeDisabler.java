package com.kaloyandonev.moddisable.helpers;

import com.google.gson.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class RecipeDisabler {

    void FindPlayerFolder(String playerUUID){
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            Path worldRoot = server.getWorldPath(LevelResource.ROOT);
            System.out.println("World folder: " + worldRoot.toAbsolutePath());
        } else {
            System.err.println("Server not available yet.");
        }

        Path worldRoot = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
        Path playerFile = worldRoot.resolve("Mod_Disable_Data").resolve(playerUUID + ".json");

        if (!playerFile.getParent().toFile().exists()) {
            try {
                Files.createDirectories(worldRoot.resolve("Mod_Disable_Data"));
            } catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    void DisableItem(){

    }
}
