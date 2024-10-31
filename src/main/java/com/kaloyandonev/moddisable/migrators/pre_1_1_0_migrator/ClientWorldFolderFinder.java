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


package com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientWorldFolderFinder {

    private static final Logger logger = LogManager.getLogger();

    public Path getWorldSubfolderPath(ServerLevel serverLevel) {
        if (serverLevel == null) {
            logger.error("[Mod Disable] [World Save Data Processor] ServerLevel is null. Cannot get world path.");
            return null;
        }

        Path dataSubFolder = serverLevel.getServer().getWorldPath(LevelResource.ROOT)
                .normalize()
                .toAbsolutePath()
                .resolve("Mod_Disable_Data");

        logger.info("[Mod Disable] [World Save Data Processor] Checking path: " + dataSubFolder);

        if (Files.exists(dataSubFolder)) {
            if (Files.isDirectory(dataSubFolder)) {
                logger.info("[Mod Disable] [World Save Data Processor] Directory exists: " + dataSubFolder);
                return dataSubFolder;
            } else {
                logger.error("[Mod Disable] [World Save Data Processor] Expected directory, got file: " + dataSubFolder);
                return null;
            }
        } else {
            logger.info("[Mod Disable] [World Save Data Processor] Directory does not exist. Creating: " + dataSubFolder);
            try {
                Files.createDirectories(dataSubFolder);
                logger.info("[Mod Disable] [World Save Data Processor] Directory created: " + dataSubFolder);
                return dataSubFolder;
            } catch (IOException e) {
                logger.error("[Mod Disable] [World Save Data Processor] Failed to create directory: " + dataSubFolder);
                logger.error(e);
                return null;
            }
        }
    }
}
