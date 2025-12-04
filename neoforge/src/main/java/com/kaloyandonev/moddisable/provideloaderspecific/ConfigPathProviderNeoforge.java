package com.kaloyandonev.moddisable.provideloaderspecific;

import com.kaloyandonev.moddisable.abstracts.ConfDir;
import com.kaloyandonev.moddisable.helpers.ServerCheckHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ConfigPathProviderNeoforge implements ConfDir.Impl {
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}

