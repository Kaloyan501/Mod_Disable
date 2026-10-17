package com.kaloyandonev.moddisable.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class PlayerItemHashmapperTest {

    @Test
    void loadJsonAsSetParsesDisabledItems(@TempDir File tempDir) throws Exception {
        File file = new File(tempDir, "player.json");
        String json = "{\"disabled_items\": [\"minecraft:diamond\", \"minecraft:apple\"]}";
        Files.writeString(file.toPath(), json, StandardCharsets.UTF_8);

        Set<String> result = PlayerItemHashmapper.loadJsonAsSet(file.toPath().toString());

        assertThat(result).containsExactlyInAnyOrder("minecraft:diamond", "minecraft:apple");
    }

    @Test
    void loadJsonAsSetReturnsEmptySetForMissingField(@TempDir File tempDir) throws Exception {
        File file = new File(tempDir, "empty.json");
        Files.writeString(file.toPath(), "{\"other\": []}", StandardCharsets.UTF_8);

        Set<String> result = PlayerItemHashmapper.loadJsonAsSet(file.toPath().toString());

        assertThat(result).isEmpty();
    }

    @Test
    void saveDisabledItemsThenLoadJsonAsSetRoundTrips(@TempDir File tempDir) throws Exception {
        File file = new File(tempDir, "roundtrip.json");

        PlayerItemHashmapper.saveDisabledItems(file.toPath().toString(), java.util.Set.of("minecraft:diamond", "minecraft:apple"));

        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        JsonObject obj = new com.google.gson.JsonParser().parseString(content).getAsJsonObject();
        assertThat(obj.has("disabled_items")).isTrue();

        Set<String> loaded = PlayerItemHashmapper.loadJsonAsSet(file.toPath().toString());
        assertThat(loaded).containsExactlyInAnyOrder("minecraft:diamond", "minecraft:apple");
    }

    @Test
    void hashmapPlayerItemsRetainsOnlyDefaultDisabled(@TempDir File tempDir) throws Exception {
        // Set up config dir via reflection since ConfDir requires a platform impl
        com.kaloyandonev.moddisable.abstracts.ConfDir.Impl impl = () -> tempDir.toPath().resolve("ModDisable").toAbsolutePath();
        com.kaloyandonev.moddisable.abstracts.ConfDir.init(impl);

        // Create default disabled list in config dir
        Path configDir = com.kaloyandonev.moddisable.abstracts.ConfDir.getConfigDir();
        Files.createDirectories(configDir.resolve("ModDisable"));
        File defaultList = configDir.resolve("ModDisable/DefaultDisabledItemsList.json").toFile();
        Files.writeString(defaultList.toPath(), "{\"disabled_items\": [\"minecraft:diamond\", \"minecraft:apple\"]}", StandardCharsets.UTF_8);

        // Player file has diamond + stick; after hashmap only diamond (in default) remains
        File playerFile = new File(tempDir, "player.json");
        Files.writeString(playerFile.toPath(), "{\"disabled_items\": [\"minecraft:diamond\", \"minecraft:stick\"]}", StandardCharsets.UTF_8);

        PlayerItemHashmapper.hashmapPlayerItems(playerFile);

        Set<String> result = PlayerItemHashmapper.loadJsonAsSet(playerFile.toPath().toString());
        assertThat(result).containsExactly("minecraft:diamond");
    }
}
