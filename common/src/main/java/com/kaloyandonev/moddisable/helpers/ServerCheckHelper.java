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

public final class ServerCheckHelper {

    private ServerCheckHelper() {
    }

    /**
     * @return true if running on the client AND connected to a dedicated server.
     * false on singleplayer, menu, LAN, or any dedicated server runtime.
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
