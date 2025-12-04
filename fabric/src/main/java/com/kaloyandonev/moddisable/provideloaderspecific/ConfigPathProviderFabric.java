package com.kaloyandonev.moddisable.provideloaderspecific;

import com.kaloyandonev.moddisable.abstracts.ConfDir;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class ConfigPathProviderFabric implements ConfDir.Impl {
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
