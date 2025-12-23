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

import java.io.File;
import java.nio.file.Path;

public class StaticPathStorage {
    private static Path subWorldFolderPath;

    public static Path getSubWorldFolderPath() {
        return subWorldFolderPath;
    }

    public static void setSubWorldFolderPath(Path path) {
        subWorldFolderPath = path;
    }

    public static File getSubWorldFolderFile() {
        return subWorldFolderPath.toFile();
    }
}
