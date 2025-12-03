package com.kaloyandonev.moddisable.helpers;

import net.minecraft.client.Minecraft;

public final class ServerCheckHelper {

    public interface Platform {
        boolean isClientEnvironment();
    }

    private static Platform PLATFORM;

    public static void init(Platform impl) {
        PLATFORM = impl;
    }

    /**
     * Checks if the Minecraft client is connected to a dedicated server.
     * Loader-agnostic.
     */
    public static boolean isConnectedToDedicatedServer() {
        if (!PLATFORM.isClientEnvironment()) {
            return false;
        }

        return !Minecraft.getInstance().isSingleplayer();
    }
}
