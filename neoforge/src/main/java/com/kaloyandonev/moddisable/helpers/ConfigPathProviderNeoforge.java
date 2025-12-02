package com.kaloyandonev.moddisable.helpers;

import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ConfigPathProviderNeoforge {
    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
