package com.kaloyandonev.moddisable.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class RecursiveFolderDeleterTest {

    @Test
    void deleteFolderRemovesNestedContents(@TempDir File parent) throws IOException {
        File target = new File(parent, "to-delete");
        Files.createDirectories(target.toPath().resolve("deep").resolve("nested"));
        Files.writeString(target.toPath().resolve("deep").resolve("nested").resolve("keep.txt"), "data", StandardCharsets.UTF_8);
        Files.writeString(target.toPath().resolve("top.txt"), "data", StandardCharsets.UTF_8);

        RecursiveFolderDeleter.deleteFolder(target);

        assertThat(target).doesNotExist();
        assertThat(new File(target, "deep/nested/keep.txt")).doesNotExist();
    }

    @Test
    void deleteFolderHandlesEmptyFolder(@TempDir File parent) {
        File target = new File(parent, "empty");
        assertThat(target.mkdirs()).isTrue();

        RecursiveFolderDeleter.deleteFolder(target);

        assertThat(target).doesNotExist();
    }

    @Test
    void deleteFolderHandlesNullListFiles(@TempDir File parent) {
        // A directory that cannot be read returns null from listFiles; we just verify no crash
        File target = new File(parent, "unreadable");
        assertThat(target.mkdirs()).isTrue();
        target.setReadable(false);

        try {
            RecursiveFolderDeleter.deleteFolder(target);
        } finally {
            target.setReadable(true);
        }

        assertThat(target).doesNotExist();
    }
}
