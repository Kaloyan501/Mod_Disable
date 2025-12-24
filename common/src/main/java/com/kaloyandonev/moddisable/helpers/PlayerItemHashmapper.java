/*
 * //ModDisable
 * //A Minecraft Mod to disable other Mods
 * //Copyright (C) 2024-2026 Kaloyan Ivanov Donev
 *
 * //This program is free software: you can redistribute it and/or modify
 * //it under the terms of the GNU General Public License as published by
 * //the Free Software Foundation, either version 3 of the License, or
 * //(at your option) any later version.
 *
 * //This program is distributed in the hope that it will be useful,
 * //but WITHOUT ANY WARRANTY; without even the implied warranty of
 * //MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * //GNU General Public License for more details.
 *
 * //You should have received a copy of the GNU General Public License
 * // along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kaloyandonev.moddisable.helpers;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.kaloyandonev.moddisable.Constants;
import com.kaloyandonev.moddisable.abstracts.ConfDir;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Set;

public class PlayerItemHashmapper {

    public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Constants.MOD_ID);

    public static void hashmapPlayerItems(File playerDisabledItems) {

        Path configDir = ConfDir.getConfigDir();
        Path configPath = configDir.resolve("ModDisable/DefaultDisabledItemsList.json");

        try {
            Set<String> playerDisabled = loadJsonAsSet(playerDisabledItems.toPath().toString());
            Set<String> defaultDisabled = loadJsonAsSet(configPath.toString());

            playerDisabled.retainAll(defaultDisabled);
            saveDisabledItems(playerDisabledItems.toPath().toString(), playerDisabled);

        } catch (Exception e) {
            LOGGER.error(e.toString());
        }

    }

    public static Set<String> loadJsonAsSet(String path) throws Exception {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(path)) {
            JsonObject obj = gson.fromJson(reader, JsonObject.class);
            Type setType = new TypeToken<Set<String>>() {
            }.getType();
            return gson.fromJson(obj.get("disabled_items"), setType);
        }
    }

    public static void saveDisabledItems(String path, Set<String> items) throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonObject obj = new JsonObject();
        obj.add("disabled_items", gson.toJsonTree(items));

        try (FileWriter writer = new FileWriter(path)) {
            gson.toJson(obj, writer);
        }
    }
}
