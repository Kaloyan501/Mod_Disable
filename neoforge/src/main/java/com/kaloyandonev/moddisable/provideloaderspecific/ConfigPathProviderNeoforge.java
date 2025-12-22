package com.kaloyandonev.moddisable.provideloaderspecific;

import com.kaloyandonev.moddisable.abstracts.ConfDir;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ConfigPathProviderNeoforge implements ConfDir.Impl {
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}

