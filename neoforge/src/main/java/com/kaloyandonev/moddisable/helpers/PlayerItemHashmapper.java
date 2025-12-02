package com.kaloyandonev.moddisable.helpers;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Set;

public class PlayerItemHashmapper {
    public static void PlayerItemHashmapper(File playerDisabledItems){

        Path configDir = FMLPaths.CONFIGDIR.get();
        Path configPath = configDir.resolve("ModDisable/DefaultDisabledItemsList.json");

        try {
            Set<String> playerDisabled = loadJsonAsSet(playerDisabledItems.toPath().toString());
            Set<String> defaultDisabled = loadJsonAsSet(configPath.toString());

            playerDisabled.retainAll(defaultDisabled);
            saveDisabledItems(playerDisabledItems.toPath().toString(), playerDisabled);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static Set<String> loadJsonAsSet(String path) throws Exception {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(path)) {
            JsonObject obj = gson.fromJson(reader, JsonObject.class);
            Type setType = new TypeToken<Set<String>>() {}.getType();
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
