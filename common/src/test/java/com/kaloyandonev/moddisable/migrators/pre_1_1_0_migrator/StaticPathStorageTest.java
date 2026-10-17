package com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class StaticPathStorageTest {

    @AfterEach
    void tearDown() {
        StaticPathStorage.setSubWorldFolderPath(null);
    }

    @Test
    void getSubWorldFolderPathReturnsNullBeforeInit() {
        assertThat(StaticPathStorage.getSubWorldFolderPath()).isNull();
    }

    @Test
    void setAndGetSubWorldFolderPath(@TempDir File tempDir) {
        Path path = tempDir.toPath().resolve("Mod_Disable_Data");
        StaticPathStorage.setSubWorldFolderPath(path);

        assertThat(StaticPathStorage.getSubWorldFolderPath()).isEqualTo(path);
        assertThat(StaticPathStorage.getSubWorldFolderFile()).isEqualTo(path.toFile());
        assertThat(StaticPathStorage.getSubWorldFolderFile().getPath()).endsWith("Mod_Disable_Data");
    }

    @Test
    void getSubWorldFolderFileThrowsIfNotInitialized() {
        assertThat(StaticPathStorage.getSubWorldFolderPath()).isNull();
        assertThatThrownBy(() -> StaticPathStorage.getSubWorldFolderFile())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void pathNormalizationWorks(@TempDir File tempDir) {
        Path nested = tempDir.toPath().resolve("world").resolve("..").resolve("Mod_Disable_Data");
        Path normalized = nested.normalize();
        StaticPathStorage.setSubWorldFolderPath(normalized);

        assertThat(StaticPathStorage.getSubWorldFolderPath().normalize()).isEqualTo(Paths.get(tempDir.getAbsolutePath(), "Mod_Disable_Data"));
    }
}
