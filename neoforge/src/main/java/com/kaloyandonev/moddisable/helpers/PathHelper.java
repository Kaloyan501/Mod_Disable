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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.io.Writer;

public class PathHelper {

    public static Path getFullWorldPath() throws IOException {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        Path worldRelative = server.getWorldPath(LevelResource.ROOT);

        if (server.isDedicatedServer()) {
            // Dedicated servers: worldRelative is under the server root.
            return worldRelative.toAbsolutePath().normalize();
        } else {
            // Integrated servers: prepend .minecraft/saves
            Path mcHome = server.getServerDirectory();
            return mcHome
                    //.resolve("saves")
                    .resolve(worldRelative)
                    .toAbsolutePath()
                    .normalize();
        }
    }

    /**
     *
     * Function that checks if the player's disabled items json file exists before returning the path.
     *
     * @param playerUUID
     * @return
     *
     */


    public static Path getPlayerJsonFile(String playerUUID) throws IOException {
        // Base directory for all player data
        Path serverDir = PathHelper.getFullWorldPath()
                .resolve("Mod_Disable_Data");
        // Ensure the directory exists (creates parents if needed)
        Files.createDirectories(serverDir);

        // The target JSON file
        Path jsonPath = serverDir.resolve(playerUUID + ".json");

        // If the file is missing, create it with an empty disabled_items array
        if (Files.notExists(jsonPath)) {
            JsonObject root = new JsonObject();
            root.add("disabled_items", new JsonArray());

            // Use pretty printing
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

            // Create the file (fail if it somehow got created in the meantime)
            try (BufferedWriter writer = Files.newBufferedWriter(
                    jsonPath,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW
            )) {
                gson.toJson(root, writer);
                System.out.println("Created new JSON file at " + jsonPath);
            }
        } else {
            System.out.println("JSON file already exists at " + jsonPath);
        }

        return jsonPath;
    }

}
