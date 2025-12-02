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


package com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator;

import com.mojang.logging.LogUtils;

import java.io.File;
import java.util.regex.Pattern;

public class Migration_110_Json_Check {

    private static final String DISABLE_MOD_FOLDER_NAME = "disabled_items";
    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.json$");

    private static boolean lock = false;

    public static boolean checkForDisableModFolder() {
        File minecraftRoot = new File(".");
        File disableModFolder = new File(minecraftRoot, DISABLE_MOD_FOLDER_NAME);

        if (lock == false) {
            if (disableModFolder.exists() && disableModFolder.isDirectory()) {
                File[] files = disableModFolder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && file.getName().endsWith(".json")) {
                            if (isValidUUIDFilename(file.getName())) {
                                LogUtils.getLogger().info("Found valid UUID JSON file: " + file.getName());
                                return true;

                            }
                        }
                    }
                }
            } else {
                LogUtils.getLogger().info("No 'disable_mod' folder found.");
            }

            lock = true;
        }
        return false;
    }


    private static boolean isValidUUIDFilename(String filename) {
        return UUID_PATTERN.matcher(filename).matches();
    }
}
