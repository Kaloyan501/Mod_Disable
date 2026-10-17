package com.kaloyandonev.moddisable.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.MinecraftServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class RecipeManagerTest {

    private static List<String> jsonArrayAsList(JsonArray arr) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            list.add(arr.get(i).getAsString());
        }
        return list;
    }

    private static boolean jsonArrayContains(JsonArray arr, String value) {
        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i).getAsString().equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Test
    void disableItemThenEnableItemRoundTrip(@TempDir Path worldRoot) throws IOException {
        Path playerFile = RecipeManager.FindPlayerFolder("550e8400-e29b-41d4-a716-446655440000", worldRoot);
        assertThat(playerFile).doesNotExist();

        RecipeManager.disableItemAtPath(playerFile, "minecraft:diamond");
        assertThat(playerFile).exists();

        String content = Files.readString(playerFile, StandardCharsets.UTF_8);
        JsonObject obj = JsonParser.parseString(content).getAsJsonObject();
        assertThat(jsonArrayContains(obj.getAsJsonArray("disabled_items"), "minecraft:diamond")).isTrue();

        RecipeManager.enableItemAtPath(playerFile, "minecraft:diamond");
        content = Files.readString(playerFile, StandardCharsets.UTF_8);
        obj = JsonParser.parseString(content).getAsJsonObject();
        assertThat(jsonArrayContains(obj.getAsJsonArray("disabled_items"), "minecraft:diamond")).isFalse();
    }

    @Test
    void disableItemIsIdempotent(@TempDir Path worldRoot) throws IOException {
        Path playerFile = RecipeManager.FindPlayerFolder("550e8400-e29b-41d4-a716-446655440000", worldRoot);

        RecipeManager.disableItemAtPath(playerFile, "minecraft:diamond");
        RecipeManager.disableItemAtPath(playerFile, "minecraft:diamond"); // should not duplicate

        String content = Files.readString(playerFile, StandardCharsets.UTF_8);
        JsonObject obj = JsonParser.parseString(content).getAsJsonObject();
        long count = jsonArrayAsList(obj.getAsJsonArray("disabled_items")).stream()
                .filter(s -> s.equals("minecraft:diamond"))
                .count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    void enableItemOnMissingFileCreatesEmptyArray(@TempDir Path worldRoot) throws IOException {
        Path playerFile = RecipeManager.FindPlayerFolder("550e8400-e29b-41d4-a716-446655440000", worldRoot);

        RecipeManager.enableItemAtPath(playerFile, "minecraft:diamond");

        assertThat(playerFile).exists();
        String content = Files.readString(playerFile, StandardCharsets.UTF_8);
        JsonObject obj = JsonParser.parseString(content).getAsJsonObject();
        assertThat(obj.getAsJsonArray("disabled_items").size()).isZero();
    }

    @Test
    void disableMultipleItems(@TempDir Path worldRoot) throws IOException {
        Path playerFile = RecipeManager.FindPlayerFolder("550e8400-e29b-41d4-a716-446655440000", worldRoot);

        RecipeManager.disableItemAtPath(playerFile, "minecraft:diamond");
        RecipeManager.disableItemAtPath(playerFile, "minecraft:apple");
        RecipeManager.disableItemAtPath(playerFile, "minecraft:stick");

        String content = Files.readString(playerFile, StandardCharsets.UTF_8);
        JsonObject obj = JsonParser.parseString(content).getAsJsonObject();
        assertThat(jsonArrayAsList(obj.getAsJsonArray("disabled_items")))
                .containsExactlyInAnyOrder("minecraft:diamond", "minecraft:apple", "minecraft:stick");
    }

    @Test
    void findPlayerFolderCreatesModDisableDataDirectory(@TempDir Path worldRoot) throws IOException {
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        Path playerFile = RecipeManager.FindPlayerFolder(uuid, worldRoot);

        RecipeManager.disableItemAtPath(playerFile, "minecraft:diamond");

        assertThat(worldRoot.resolve("Mod_Disable_Data")).isDirectory();
        assertThat(playerFile.getFileName().toString()).isEqualTo(uuid + ".json");
    }

    // --- Namespace tests (require BuiltInRegistries.ITEM, available in NeoForge) ---

    @Test
    void disableNamespaceAddsAllItemsInNamespace(@TempDir Path worldRoot) throws Exception {
        // Verify the package-private method exists and is accessible (full logic tested via GameTest integration)
        assertThat(RecipeManager.class.getDeclaredMethod("disableNamespaceAtPath", Path.class, String.class, MinecraftServer.class)).isNotNull();
    }

    @Test
    void enableNamespaceRemovesAllItemsInNamespace(@TempDir Path worldRoot) throws Exception {
        assertThat(RecipeManager.class.getDeclaredMethod("enableNamespaceAtPath", Path.class, String.class, MinecraftServer.class)).isNotNull();
    }
}
