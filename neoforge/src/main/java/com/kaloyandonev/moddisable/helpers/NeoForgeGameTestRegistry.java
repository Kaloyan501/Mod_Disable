package com.kaloyandonev.moddisable.helpers;

import com.kaloyandonev.moddisable.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

public class NeoForgeGameTestRegistry {

    private static final TestEnvironmentDefinition<?> SIMPLE_ENV =
            new TestEnvironmentDefinition.Functions(Optional.empty(), Optional.empty());

    public static void registerGameTests(RegisterGameTestsEvent event) {
        Identifier envId = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "simple_env");
        var envHolder = event.registerEnvironment(envId, SIMPLE_ENV);

        registerTest(event, envHolder, "disable_item_roundtrip", helper -> testDisableEnableItem(helper), 200);
        registerTest(event, envHolder, "disable_namespace", helper -> testDisableNamespace(helper), 200);
        registerTest(event, envHolder, "disable_all", helper -> testDisableAll(helper), 200);
        registerTest(event, envHolder, "enable_all", helper -> testEnableAll(helper), 200);
        registerTest(event, envHolder, "item_registry", helper -> testItemRegistry(helper), 100);
    }

    private static void registerTest(RegisterGameTestsEvent event,
                                     net.minecraft.core.Holder<TestEnvironmentDefinition<?>> envHolder,
                                     String testName,
                                     ThrowingGameTestFn testFn,
                                     int maxTicks) {
        TestData<net.minecraft.core.Holder<TestEnvironmentDefinition<?>>> testData =
                new TestData<>(envHolder, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "empty"),
                        maxTicks, 0, true, Rotation.NONE);

        GameTestInstance instance = new GameTestInstance(testData) {
            @Override
            public void run(GameTestHelper helper) {
                try {
                    testFn.apply(helper);
                    helper.succeed();
                } catch (Throwable t) {
                    helper.fail(t.getMessage() != null ? t.getMessage() : t.toString());
                    throw new RuntimeException(t);
                }
            }

            @Override
            public com.mojang.serialization.MapCodec<? extends GameTestInstance> codec() {
                return null;
            }

            @Override
            protected net.minecraft.network.chat.MutableComponent typeDescription() {
                return Component.literal("ModDisable NeoForge game test: " + testName);
            }
        };

        event.registerTest(Identifier.fromNamespaceAndPath(Constants.MOD_ID, testName), instance);
    }

    @FunctionalInterface
    interface ThrowingGameTestFn {
        Void apply(GameTestHelper helper) throws Exception;
    }

    private static Void testDisableEnableItem(GameTestHelper helper) throws Exception {
        MinecraftServer server = helper.getLevel().getServer();
        Path worldRoot = server.getWorldPath(LevelResource.ROOT);
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        Path playerFile = RecipeManager.FindPlayerFolder(uuid, worldRoot);

        RecipeManager.disableItemAtPath(playerFile, "minecraft:diamond");
        String disabled = new String(java.nio.file.Files.readAllBytes(playerFile));
        if (!disabled.contains("minecraft:diamond")) {
            throw new AssertionError("Item should be disabled: " + disabled);
        }

        RecipeManager.enableItemAtPath(playerFile, "minecraft:diamond");
        String enabled = new String(java.nio.file.Files.readAllBytes(playerFile));
        if (enabled.contains("minecraft:diamond")) {
            throw new AssertionError("Item should be enabled: " + enabled);
        }

        return null;
    }

    private static Void testDisableNamespace(GameTestHelper helper) throws Exception {
        MinecraftServer server = helper.getLevel().getServer();
        Path worldRoot = server.getWorldPath(LevelResource.ROOT);
        String uuid = "550e8400-e29b-41d4-a716-446655440001";
        Path playerFile = RecipeManager.FindPlayerFolder(uuid, worldRoot);

        RecipeManager.disableNamespaceAtPath(playerFile, "minecraft", server);
        String content = new String(java.nio.file.Files.readAllBytes(playerFile));
        if (!content.contains("minecraft:diamond")) {
            throw new AssertionError("Namespace disable should add minecraft:diamond: " + content);
        }

        return null;
    }

    private static Void testDisableAll(GameTestHelper helper) throws Exception {
        MinecraftServer server = helper.getLevel().getServer();
        Path worldRoot = server.getWorldPath(LevelResource.ROOT);
        String uuid = "550e8400-e29b-41d4-a716-446655440002";
        Path playerFile = RecipeManager.FindPlayerFolder(uuid, worldRoot);

        RecipeManager.DisableAll(uuid, server);
        String content = new String(java.nio.file.Files.readAllBytes(playerFile));
        if (!content.contains("minecraft:diamond")) {
            throw new AssertionError("Disable all should include minecraft:diamond: " + content);
        }

        return null;
    }

    private static Void testEnableAll(GameTestHelper helper) throws Exception {
        MinecraftServer server = helper.getLevel().getServer();
        Path worldRoot = server.getWorldPath(LevelResource.ROOT);
        String uuid = "550e8400-e29b-41d4-a716-446655440003";
        Path playerFile = RecipeManager.FindPlayerFolder(uuid, worldRoot);

        RecipeManager.DisableAll(uuid, server);
        RecipeManager.EnableAll(uuid, server);
        String content = new String(java.nio.file.Files.readAllBytes(playerFile));
        if (!content.contains("[]")) {
            throw new AssertionError("Enable all should clear disabled items: " + content);
        }

        return null;
    }

    private static Void testItemRegistry(GameTestHelper helper) {
        if (!BuiltInRegistries.ITEM.containsKey(Identifier.tryParse("minecraft:diamond"))) {
            throw new AssertionError("minecraft:diamond should be registered");
        }
        if (!BuiltInRegistries.ITEM.containsKey(Identifier.tryParse("minecraft:apple"))) {
            throw new AssertionError("minecraft:apple should be registered");
        }
        return null;
    }
}
