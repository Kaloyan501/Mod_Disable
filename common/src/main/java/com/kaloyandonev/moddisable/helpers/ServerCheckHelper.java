package com.kaloyandonev.moddisable.helpers;

public final class ServerCheckHelper {

    private ServerCheckHelper() {
    }

    /**
     * @return true if running on the client AND connected to a dedicated server.
     *         false on singleplayer, menu, LAN, or any dedicated server runtime.
     */
    public static boolean isConnectedToDedicatedServer() {
        try {
            // Avoids classloading on dedicated servers
            Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
            Object minecraft = minecraftClass.getMethod("getInstance").invoke(null);

            if (minecraft == null) {
                return false;
            }

            boolean singleplayer = (boolean) minecraftClass
                    .getMethod("isSingleplayer")
                    .invoke(minecraft);

            return !singleplayer;
        } catch (Throwable ignored) {
            // Covers:
            // - Dedicated server (Fabric/NeoForge)
            // - Data generators
            // - Early startup
            return false;
        }
    }
}
