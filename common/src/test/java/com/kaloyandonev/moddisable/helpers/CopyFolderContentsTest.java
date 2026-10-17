package com.kaloyandonev.moddisable.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CopyFolderContentsTest {

    @Test
    void copyFolderContentsCopiesAllFiles(@TempDir File sourceDir, @TempDir File destDir) throws IOException {
        Files.writeString(sourceDir.toPath().resolve("a.txt"), "content-a", StandardCharsets.UTF_8);
        Files.writeString(sourceDir.toPath().resolve("b.txt"), "content-b", StandardCharsets.UTF_8);
        Files.createDirectories(sourceDir.toPath().resolve("sub"));
        Files.writeString(sourceDir.toPath().resolve("sub").resolve("c.txt"), "content-c", StandardCharsets.UTF_8);

        new CopyFolderContents().copyFolderContents(sourceDir.toPath(), destDir.toPath());

        assertThat(Files.readString(new File(destDir, "a.txt").toPath(), StandardCharsets.UTF_8)).isEqualTo("content-a");
        assertThat(Files.readString(new File(destDir, "b.txt").toPath(), StandardCharsets.UTF_8)).isEqualTo("content-b");
        assertThat(Files.readString(new File(destDir, "sub/c.txt").toPath(), StandardCharsets.UTF_8)).isEqualTo("content-c");
    }

    @Test
    void copyFolderContentsCreatesDestination(@TempDir File sourceDir, @TempDir File parent) throws IOException {
        File destDir = new File(parent, "new-dest");
        Files.writeString(sourceDir.toPath().resolve("a.txt"), "hello", StandardCharsets.UTF_8);

        new CopyFolderContents().copyFolderContents(sourceDir.toPath(), destDir.toPath());

        assertThat(destDir).exists();
        assertThat(new File(destDir, "a.txt")).exists();
    }

    @Test
    void copyFolderContentsReplacesExisting(@TempDir File sourceDir, @TempDir File destDir) throws IOException {
        Files.writeString(sourceDir.toPath().resolve("a.txt"), "new", StandardCharsets.UTF_8);
        Files.writeString(destDir.toPath().resolve("a.txt"), "old", StandardCharsets.UTF_8);

        new CopyFolderContents().copyFolderContents(sourceDir.toPath(), destDir.toPath());

        assertThat(Files.readString(new File(destDir, "a.txt").toPath(), StandardCharsets.UTF_8)).isEqualTo("new");
    }
}
