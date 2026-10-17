package com.kaloyandonev.moddisable.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileSecurityTest {

    private static final String KNOWN_INPUT = "hello world";
    private static final String KNOWN_MD5 = "5eb63bbbe01eeed093cb22bb8f5acdc3";

    @Test
    void checksumReturnsKnownMd5ForKnownInput(@TempDir File tempDir) throws Exception {
        File file = new File(tempDir, "test.txt");
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(KNOWN_INPUT.getBytes(StandardCharsets.UTF_8));
        }

        MessageDigest digest = MessageDigest.getInstance("MD5");
        String result = FileSecurity.checksum(digest, file);

        assertThat(result).isEqualTo(KNOWN_MD5);
    }

    @Test
    void checksumIsConsistentAcrossCalls(@TempDir File tempDir) throws Exception {
        File file = new File(tempDir, "consistent.txt");
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write("deterministic content".getBytes(StandardCharsets.UTF_8));
        }

        MessageDigest digest1 = MessageDigest.getInstance("MD5");
        MessageDigest digest2 = MessageDigest.getInstance("MD5");
        String result1 = FileSecurity.checksum(digest1, file);
        String result2 = FileSecurity.checksum(digest2, file);

        assertThat(result1).isEqualTo(result2);
    }

    @Test
    void checksumChangesWhenContentChanges(@TempDir File tempDir) throws Exception {
        File file = new File(tempDir, "changing.txt");
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write("version one".getBytes(StandardCharsets.UTF_8));
        }
        MessageDigest digest1 = MessageDigest.getInstance("MD5");
        String before = FileSecurity.checksum(digest1, file);

        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write("version two".getBytes(StandardCharsets.UTF_8));
        }
        MessageDigest digest2 = MessageDigest.getInstance("MD5");
        String after = FileSecurity.checksum(digest2, file);

        assertThat(after).isNotEqualTo(before);
    }

    @Test
    void checksumUsesSuppliedDigestAlgorithm() throws Exception {
        // SHA-256 should produce a different (longer) hash than MD5 for the same input
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(KNOWN_INPUT.getBytes(StandardCharsets.UTF_8));
        // Use reflection-free approach: write to temp file and checksum both
        java.io.File tempFile = java.io.File.createTempFile("digest-algo", ".txt");
        tempFile.deleteOnExit();
        try (java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile)) {
            out.write(KNOWN_INPUT.getBytes(StandardCharsets.UTF_8));
        }

        String md5Result = FileSecurity.checksum(md5, tempFile);
        String sha256Result = FileSecurity.checksum(sha256, tempFile);

        assertThat(md5Result).hasSize(32); // MD5 hex = 32 chars
        assertThat(sha256Result).hasSize(64); // SHA-256 hex = 64 chars
        assertThat(md5Result).isNotEqualTo(sha256Result);
    }
}
