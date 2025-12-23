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
