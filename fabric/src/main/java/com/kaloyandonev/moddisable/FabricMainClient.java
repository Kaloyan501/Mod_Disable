/*
 * //ModDisable
 * //A Minecraft Mod to disable other Mods
 * //Copyright (C) 2024-2026 Kaloyan Ivanov Donev
 *
 * //This program is free software: you can redistribute it and/or modify
 * //it under the terms of the GNU General Public License as published by
 * //the Free Software Foundation, either version 3 of the License, or
 * //(at your option) any later version.
 *
 * //This program is distributed in the hope that it will be useful,
 * //but WITHOUT ANY WARRANTY; without even the implied warranty of
 * //MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * //GNU General Public License for more details.
 *
 * //You should have received a copy of the GNU General Public License
 * // along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kaloyandonev.moddisable;

import com.kaloyandonev.moddisable.helpers.MigrateTask;
import com.kaloyandonev.moddisable.helpers.ProcessAllDisabledItemsFromJson;
import com.kaloyandonev.moddisable.helpers.ServerCheckHelper;
import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.ClientTickHandler;
import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.ClientWorldFolderFinder;
import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.ScreenCrator;
import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.StaticPathStorage;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.nio.file.Path;

import static com.kaloyandonev.moddisable.FabricMain.LOGGER;

@SuppressWarnings(value = "unused")
public class FabricMainClient implements ClientModInitializer {
    public static void ServerFolderFinderInit(ServerLevel world) {
        ClientWorldFolderFinder folderFinder = new ClientWorldFolderFinder();
        Path subWorldFolderPath = folderFinder.getWorldSubfolderPath(world);

        StaticPathStorage.setSubWorldFolderPath(subWorldFolderPath);
        LOGGER.info("subWorldFolderPath is {}", subWorldFolderPath);

        ClientTickHandler clientTickHandler = new ClientTickHandler();
        MigrateTask migrateTask = new MigrateTask();
        migrateTask.performMigration();
    }

    public static void ServerFolderDedicatedInit(MinecraftServer server) {
        ServerLevel world = server.getLevel(Level.OVERWORLD);

        ClientWorldFolderFinder folderFinder = new ClientWorldFolderFinder();
        Path subWorldFolderPath = folderFinder.getWorldSubfolderPath(world);
        LOGGER.info("subWorldFolderPath is {}", subWorldFolderPath);

        StaticPathStorage.setSubWorldFolderPath(subWorldFolderPath);

        ProcessAllDisabledItemsFromJson.processAllDisabledItemsFromJson();
    }

    @Override
    public void onInitializeClient() {

        ClientCode();

        /*
        ServerCheckHelper.init(() ->
                FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
        );
         */
    }

    private void ClientCode() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            Minecraft.getInstance().execute(() -> {
                ScreenCrator screenCrator = new ScreenCrator(
                        Component.literal("Migration Title"), // Title of the screen
                        Component.literal("Description of the screen"), // Description
                        confirmed -> false
                );

                // Open the new screen
                Minecraft.getInstance().setScreen(screenCrator);
            });
        }

        // Some client setup code
        LOGGER.info("HELLO FROM CLIENT SETUP");
        LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        Minecraft client = Minecraft.getInstance();
        if (ServerCheckHelper.isConnectedToDedicatedServer()) {
            LOGGER.info("[Mod Disable] t[DEBUG] We are connected to a dedicated server!");
        } else {
            LOGGER.info("[Mod Disable] [DEBUG] We are NOT connected to a dedicated server!");
        }
    }
}
