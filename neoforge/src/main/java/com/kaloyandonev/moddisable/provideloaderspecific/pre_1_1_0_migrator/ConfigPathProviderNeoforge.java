package com.kaloyandonev.moddisable.provideloaderspecific.pre_1_1_0_migrator;

import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ConfigPathProviderNeoforge {
    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
