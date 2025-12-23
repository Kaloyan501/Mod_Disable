//ModDisable
//A Minecraft Mod to disable other Mods
//Copyright (C) 2024-2025 Kaloyan Ivanov Donev

//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.


package com.kaloyandonev.moddisable.helpers;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;

public class ServerCheckHelper {
    /**
     * Checks if the Minecraft client is connected to a dedicated server.
     * @return true if connected to a dedicated server, false otherwise.
     */
    public static boolean isConnectedToDedicatedServer() {
        // Check if running on the client or server side
        if (FMLLoader.getDist() == Dist.CLIENT) {
            return Minecraft.getInstance().isSingleplayer() ? false : true;
        } else {
            return false; // On dedicated server, return false
        }
    }
}
