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
package com.kaloyandonev.moddisable;

import com.kaloyandonev.moddisable.abstracts.ConfDir;
import com.kaloyandonev.moddisable.helpers.*;
import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.ClientTickHandler;
import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.ClientWorldFolderFinder;
import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.StaticPathStorage;
import com.kaloyandonev.moddisable.provideloaderspecific.ConfigPathProviderNeoforge;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.kaloyandonev.moddisable.Constants.MOD_ID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MOD_ID)
public class NeoMain {
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);
    private final isSinglePlayer isSinglePlayer = new isSinglePlayer();


    public NeoMain(IEventBus modBus) {

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        //modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        NeoForge.EVENT_BUS.register(new UseDetector());
        NeoForge.EVENT_BUS.register(new ServerModEvents());
        NeoForge.EVENT_BUS.register(this);
        ATTACHMENT_TYPES.register(modBus);
            /*
            ServerCheckHelper.init(() -> true);
             */
        ConfigPathProviderNeoforge configPathProviderNeoforge = new ConfigPathProviderNeoforge();
        ConfDir.init(configPathProviderNeoforge);
    }

    @SubscribeEvent
    public void onLoadComplete(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        isSinglePlayer.update(server);
    }

    public static class ServerModEvents {

        @SubscribeEvent
        public void onServerStarted(ServerStartedEvent event) {

            LOGGER.warn("HELLO from server started! (Not to be confused with starting!)");

            MinecraftServer server = event.getServer();
            ServerLevel world = server.getLevel(Level.OVERWORLD);

            // =========================
            // 1. WORLD PATH INITIALIZATION
            // =========================
            Path subWorldFolderPath = null;

            if (!server.isDedicatedServer()) {
                ClientWorldFolderFinder folderFinder = new ClientWorldFolderFinder();
                subWorldFolderPath = folderFinder.getWorldSubfolderPath(world);

                ClientTickHandler clientTickHandler = new ClientTickHandler();
                MigrateTask migrateTask = new MigrateTask();
                migrateTask.performMigration();

            } else {
                // Dedicated server path resolution
                ClientWorldFolderFinder folderFinder = new ClientWorldFolderFinder();
                subWorldFolderPath = folderFinder.getWorldSubfolderPath(world);
            }

            // Critical: must always happen BEFORE any processing
            if (subWorldFolderPath == null) {
                LOGGER.error("[Mod Disable] Failed to resolve subWorldFolderPath");
                return;
            }

            StaticPathStorage.setSubWorldFolderPath(subWorldFolderPath);
            LOGGER.info("subWorldFolderPath is {}", subWorldFolderPath);

            // =========================
            // 2. JSON PROCESSING
            // =========================
            LOGGER.debug("[Mod Disable] MigrateTask is about to run!");
            ProcessAllDisabledItemsFromJson.processAllDisabledItemsFromJson();

            // =========================
            // 3. CHECKSUM VALIDATION
            // =========================
            boolean checkSumInvalid = JsonHelper.defaultDisabledListChecksumManager();

            if (checkSumInvalid) {
                try {
                    Path worldFolderPath = PathHelper.getFullWorldPath(server);
                    Path modDisableDataPath = worldFolderPath.resolve("Mod_Disable_Data");

                    List<String> fileNames = Files.list(modDisableDataPath)
                            .map(p -> p.getFileName().toString())
                            .toList();

                    for (String fileName : fileNames) {
                        PlayerItemHashmapper.hashmapPlayerItems(
                                modDisableDataPath.resolve(fileName).toFile()
                        );
                    }

                } catch (IOException e) {
                    LOGGER.error("Checksum processing failed", e);
                }
            }
        }
    }
    @SuppressWarnings(value = "unused")
    public static class ClientModEvents {
        @SubscribeEvent
        public void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
            Minecraft client = Minecraft.getInstance();
            MinecraftServer server = client.getSingleplayerServer();
            if (ServerCheckHelper.isConnectedToDedicatedServer()) {
                LOGGER.info("[Mod Disable] t[DEBUG] We are connected to a dedicated server!");
            } else {
                LOGGER.info("[Mod Disable] [DEBUG] We are NOT connected to a dedicated server!");
            }
        }
    }
}

