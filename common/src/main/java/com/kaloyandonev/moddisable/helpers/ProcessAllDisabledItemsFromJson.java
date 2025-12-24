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

import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.StaticPathStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class ProcessAllDisabledItemsFromJson {

    private static final Logger logger = LogManager.getLogger(ProcessAllDisabledItemsFromJson.class);
    private static File DATA_DIR = StaticPathStorage.getSubWorldFolderFile();

    // Method to get the data directory with lazy initialization
    private static File getDataDir() {
        DATA_DIR = StaticPathStorage.getSubWorldFolderFile();

        // Ensure the directory exists
        if (!DATA_DIR.exists()) {
            try {
                DATA_DIR.mkdirs(); // Create the directory if it does not exist
            } catch (SecurityException e) {
                logger.error(e.toString());
            }

        }
        return DATA_DIR;
    }

    public static void processAllDisabledItemsFromJson() {
        getDataDir();
        if (!getDataDir().exists() || !DATA_DIR.isDirectory()) {
            System.out.println("Directory not found: " + DATA_DIR.getAbsolutePath());
            return;
        }

        File[] files = DATA_DIR.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            logger.debug("[Mod Disable] No files found in dir: {}", DATA_DIR.getAbsolutePath());
        }

        assert files != null;
        for (File file : files) {
            logger.debug("[Mod Disable]Disabling recipes for file: {}", file.getName());
            logger.debug("[Mod Disable]JSON file path is: {}", file.getAbsolutePath());
            //RecipeManager.queueRecipeRemovalFromJson(file.getAbsolutePath());
        }
    }

}
