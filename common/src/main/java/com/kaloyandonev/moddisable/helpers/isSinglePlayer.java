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
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class isSinglePlayer {

    public boolean isSinglePlayer;
    private static final Logger logger = LogManager.getLogger(isSinglePlayer.class);

    public void checkisSinglePlayer() {
        Minecraft.getInstance().execute(() -> {
            Level world = Minecraft.getInstance().level;
            logger.info("[Mod Disable] [Debug] World is {}" , world);
            if (world != null) {
                isSinglePlayer = Minecraft.getInstance().isSingleplayer();
                if (isSinglePlayer) {
                    logger.info("[Mod Disable] Detected single player world, will use single player saving method.");
                }
            }
        });
    }

    public boolean getIsSinglePlayer() {
        return isSinglePlayer;
    }

}
