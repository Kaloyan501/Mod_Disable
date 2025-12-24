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

import com.kaloyandonev.moddisable.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class RecursiveFolderDeleter {

    public static final Logger LOGGER = LoggerFactory.getLogger(Constants.MOD_ID);

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { // If the folder is not empty
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file); // Recursively delete subdirectories
                } else {
                    try {
                        file.delete(); // Delete files
                    } catch (SecurityException e) {
                        LOGGER.error(e.toString());
                    }
                }
            }
        }
        try {
            folder.delete(); // Delete the folder
        } catch (SecurityException e) {
            LOGGER.error(e.toString());
        }
    }
}
