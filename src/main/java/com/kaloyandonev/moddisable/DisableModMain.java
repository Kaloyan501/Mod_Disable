//ModDisable
//A Minecraft Mod to disable other Mods
//Copyright (C) 2024 Kaloyan Ivanov Donev

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

import com.google.gson.*;
import com.kaloyandonev.moddisable.disablelogic.playerjoinsyncpacket.PlayerJoinSyncPacket;
import com.kaloyandonev.moddisable.helpers.ServerCheckHelper;
import com.kaloyandonev.moddisable.helpers.UseDetector;
import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.ClientWorldFolderFinder;
import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.MigrateTask;
import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.StaticPathStorage;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerLifecycleEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

import com.kaloyandonev.moddisable.helpers.processAllDisabledItemsFromJson;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.io.File;
import java.io.FileReader;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(DisableModMain.MODID)
public class DisableModMain
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "moddisable";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public DisableModMain(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register ourselves for server and other game events we are interested in
        //NeoForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        NeoForge.EVENT_BUS.register(new UseDetector());
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

