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


package com.kaloyandonev.moddisable.helpers;

import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.ClientTickHandler;
import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.Migration_110_Json_Check;
import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.StaticPathStorage;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

//@OnlyIn(Dist.CLIENT)
public class MigrateTask {
    private static final Logger logger = LogManager.getLogger();
    //private CommandManager disable_mod = new CommandManager();
    private final isSinglePlayer isSinglePlayer = new isSinglePlayer();

    public void performMigration() {
        boolean migrationConfrimed = ClientTickHandler.migrationConfrimed;
        logger.info("[Mod Disable] [Migration 1.1.0] [DEBUG] migrationConfirmed is " + migrationConfrimed);
        logger.info("[Mod Disable] [Migration 1.1.0] [DEBUG] isSinglePlayer is " + isSinglePlayer.isSinglePlayer);
        logger.info("[Mod Disable] [Migration 1.1.0] [DEBUG] checkforDisableModFolder" + Migration_110_Json_Check.checkForDisableModFolder());

        if (Migration_110_Json_Check.checkForDisableModFolder() && migrationConfrimed) {
            Path path = StaticPathStorage.getSubWorldFolderPath();

            Minecraft minecraft = Minecraft.getInstance();
            String minecraftDir = minecraft.gameDirectory.getAbsolutePath();
            File disabledItemsPath = new File(minecraftDir, "disabled_items");

            CopyFolderContents fileCopier = new CopyFolderContents();
            Path sourcePath = disabledItemsPath.toPath();

            try {

                fileCopier.copyFolderContents(sourcePath, path);
                logger.info("[Mod Disable] [Migrator 1.1.0] Files copied successfully.");
                RecursiveFolderDeleter.deleteFolder(sourcePath.toFile());
                ProcessAllDisabledItemsFromJson.processAllDisabledItemsFromJson();
            } catch (IOException e) {
                logger.error(e.toString());
                logger.info("[Mod Disable] [Migrator 1.1.0] Files copy error.");
            }

        }
    }
}
