
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
package com.kaloyandonev.moddisable.helpers;

import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.StaticPathStorage;
import net.minecraft.server.MinecraftServer;

import java.io.File;

public class processAllDisabledItemsFromJson {

    //private static final File DATA_DIR = new File("disabled_items");
    private static File DATA_DIR = StaticPathStorage.getSubWorldFolderFile();

    // Method to get the data directory with lazy initialization
    private static File getDataDir() {
        //if (DATA_DIR == null) {
        DATA_DIR = StaticPathStorage.getSubWorldFolderFile();

        // Ensure the directory exists
        if (!DATA_DIR.exists()) {
            DATA_DIR.mkdirs(); // Create the directory if it does not exist
        }
        //}
        return DATA_DIR;
    }

    public static void processAllDisabledItemsFromJson() {
        getDataDir();
        if (!getDataDir().exists() || !DATA_DIR.isDirectory()){
            System.out.println("Directory not found: " + DATA_DIR.getAbsolutePath());
            return;
        }

        File[] files = DATA_DIR.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            System.out.println("No files found in dir: " + DATA_DIR.getAbsolutePath());
        }

        for (File file : files) {
            System.out.println("Disabling recipes for file: " + file.getName());
            System.out.println("JSON file path is: " + file.getAbsolutePath());
            RecipeDisabler.queueRecipeRemovalFromJson(file.getAbsolutePath());
        }
    }

}
