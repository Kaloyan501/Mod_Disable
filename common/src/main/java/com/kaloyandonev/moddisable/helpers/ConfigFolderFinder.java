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

package com.kaloyandonev.moddisable.helpers;

import java.io.File;

public class ConfigFolderFinder {
    public static File getModConfigFolder() {
        File configDir = null;

        // Try Forge / NeoForge
        try {
            Class<?> fmlPathsClass = Class.forName("net.minecraftforge.fml.loading.FMLPaths");
            Object configPath = fmlPathsClass.getMethod("CONFIGDIR").invoke(null);
            configDir = (File) configPath.getClass().getMethod("toFile").invoke(configPath);
        } catch (Exception ignored) {
        }

        // Try Fabric
        if (configDir == null) {
            try {
                Class<?> fabricLoaderClass = Class.forName("net.fabricmc.loader.api.FabricLoader");
                Object loader = fabricLoaderClass.getMethod("getInstance").invoke(null);
                configDir = (File) fabricLoaderClass.getMethod("getConfigDir").invoke(loader);
            } catch (Exception ignored) {
            }
        }

        // Fallback
        if (configDir == null) {
            configDir = new File(System.getProperty("user.dir"), "config");
        }

        // Your mod-specific folder
        File modFolder = new File(configDir, "ModDisable");
        if (!modFolder.exists()) modFolder.mkdirs();

        return modFolder;
    }
}
