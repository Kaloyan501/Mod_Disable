package com.kaloyandonev.moddisable.helpers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
                    .resolve("saves")
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
        Path serverDir = PathHelper.getFullWorldPath();
        Path jsonPath = serverDir.resolve("Mod_Disable_Data").resolve(playerUUID + ".json");

        if (!Files.exists(jsonPath)) {
            Files.createDirectories(jsonPath.getParent());

            JsonObject jsonObject = new JsonObject();
            jsonObject.add("disabled_items", new JsonArray());
            System.out.println("Creating non-existent json file.");

            Gson gson = new Gson();
            try (Writer writer = Files.newBufferedWriter(jsonPath)) {
                gson.toJson(jsonObject, writer);
            }
        } else {
            System.out.println("Json file exists.");
        }

        return jsonPath;
    }

}
