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

import com.kaloyandonev.moddisable.helpers.*;
import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.ClientWorldFolderFinder;
import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.MigrateTask;
import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.StaticPathStorage;
import com.mojang.datafixers.types.templates.Check;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion.MOD_ID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Main.MODID)
public class Main
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "moddisable";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);


    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);

        public Main(IEventBus modBus, ModContainer modContainer)
        {
            // Register ourselves for server and other game events we are interested in
            //NeoForge.EVENT_BUS.register(this);
            // Create the DeferredRegister for attachment types


            // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
            modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
            NeoForge.EVENT_BUS.register(new UseDetector());
            ATTACHMENT_TYPES.register(modBus);


        }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.GAME)
    //@OnlyIn(Dist.DEDICATED_SERVER)
    public static class ServerModEvents {


        // You can use SubscribeEvent and let the Event Bus discover methods to call
        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        //@SuppressWarnings(value = "unused")
        public static void onServerStarted(ServerStartedEvent event){


            //  Do something when the server starts
            LOGGER.warn("HELLO from server started! (Not to be confused with starting!)");

            MinecraftServer server = event.getServer();
            ServerLevel world = server.getLevel(Level.OVERWORLD);

            if (!server.isDedicatedServer()) {
                ClientWorldFolderFinder folderFinder = new ClientWorldFolderFinder();
                Path subWorldFolderPath = folderFinder.getWorldSubfolderPath(world);

                StaticPathStorage.setSubWorldFolderPath(subWorldFolderPath);
                LOGGER.info("subWorldFolderPath is {}", subWorldFolderPath);

                MigrateTask migrateTask = new MigrateTask();
                migrateTask.performMigration();
            }

            LOGGER.debug("[Mod Disable] MigrateTask is about to run!");
            processAllDisabledItemsFromJson.processAllDisabledItemsFromJson();

            boolean CheckSumInvalid = JsonHelper.defaultDisabledListChecksumManger();
            if (CheckSumInvalid = true){
                try {
                    Path WorldFolderPath = PathHelper.getFullWorldPath();
                    Path Mod_Disable_DataPath = WorldFolderPath.resolve("Mod_Disable_Data");

                    List<String> fileNames = Files.list(Mod_Disable_DataPath).map(p -> p.getFileName().toString()).collect(Collectors.toList());
                    String[] fileNamesArray = fileNames.toArray(new String[0]);

                    for (String fileName : fileNamesArray) {
                        PlayerItemHashmapper.PlayerItemHashmapper(Mod_Disable_DataPath.resolve(fileName).toFile());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @SubscribeEvent
        @OnlyIn(Dist.DEDICATED_SERVER)
        public static void onServerStartedDedicated(ServerStartedEvent event){

            MinecraftServer server = event.getServer();
            ServerLevel world = server.getLevel(Level.OVERWORLD);

            ClientWorldFolderFinder folderFinder = new ClientWorldFolderFinder();
            Path subWorldFolderPath = folderFinder.getWorldSubfolderPath(world);
            LOGGER.info("subWorldFolderPath is {}", subWorldFolderPath);

            StaticPathStorage.setSubWorldFolderPath(subWorldFolderPath);

            processAllDisabledItemsFromJson.processAllDisabledItemsFromJson();


        }

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    @SuppressWarnings(value = "unused")
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
            if (ServerCheckHelper.isConnectedToDedicatedServer()) {
                LOGGER.info("[Mod Disable] t[DEBUG] We are connected to a dedicated server!");
            } else {
                LOGGER.info("[Mod Disable] [DEBUG] We are NOT connected to a dedicated server!");
            }
        }
    }
}

