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
import com.kaloyandonev.moddisable.Constants;
import com.kaloyandonev.moddisable.abstracts.ConfDir;
import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.StaticPathStorage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class JsonHelper {

    public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Constants.MOD_ID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static String jsonFieldName = "disabled_items";
    private static File dataDir = StaticPathStorage.getSubWorldFolderFile();

    static {
        if (!dataDir.exists()) {
            try {
                dataDir.mkdir();
            } catch (SecurityException e){
                LOGGER.error(e.toString());
            }

        }
    }

    // Method to get the data directory with lazy initialization
    private static File getDataDir() {
        dataDir = StaticPathStorage.getSubWorldFolderFile();

        // Ensure the directory exists
        if (!dataDir.exists()) {
            try {
                dataDir.mkdirs(); // Create the directory if it does not exist
            } catch (SecurityException e) {
                LOGGER.error(e.toString());
            }

        }
        return dataDir;
    }

    public static File getPlayerFile(Player player) {
        return new File(getDataDir(), player.getUUID() + ".json");
    }

    public static JsonObject readPlayerData(File file) {
        try (FileReader reader = new FileReader(file)) {
            return GSON.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            //If FileNotFoundException, return a new JsonObject.
            return new JsonObject();
        }
    }


    public static boolean isItemDisabled(Item item, Player player) {
        File playerFile = getPlayerFile(player);

        JsonObject data = readPlayerData(playerFile);

        if (!data.has(jsonFieldName)) {
            return false;
        }

        JsonArray disabledItems = data.getAsJsonArray(jsonFieldName);
        for (JsonElement element : disabledItems) {
            if (element.getAsString().equals(BuiltInRegistries.ITEM.getKey(item).toString())) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings(value = "unused")
    public static boolean defaultDisabledListChecksumManager() {
        Path configDir = ConfDir.getConfigDir();
        Path configPath = configDir.resolve("ModDisable/DefaultDisabledItemsList.json");
        Path configChecksumPath = configDir.resolve("ModDisable/Checksum.txt");

        File configFile = configPath.toFile();
        File configChecksumFile = configChecksumPath.toFile();

        try {
            MessageDigest mdigest = MessageDigest.getInstance("MD5");
            String checksum = FileSecurity.checksum(mdigest, configFile);
            if (!configChecksumFile.exists()) {
                boolean FileCreateStatus = configChecksumFile.createNewFile();
            }
            String checksumFromFile = Files.readString(configChecksumPath);
            if (checksum.equals(checksumFromFile)) {
                return false;
            } else {
                Files.writeString(configChecksumPath, checksum);
                return true;
            }

        } catch (NoSuchAlgorithmException | IOException e) {
            LOGGER.error(e.toString());
        }
        return false;
    }
}
