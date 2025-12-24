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

package com.kaloyandonev.moddisable.kubejs;

import com.kaloyandonev.moddisable.Constants;
import com.kaloyandonev.moddisable.helpers.RecipeManager;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@SuppressWarnings(value = "unused")
public class KubeJSItemManager {

    public static final Logger LOGGER = LoggerFactory.getLogger(Constants.MOD_ID);

    public void enableItem(String playerUUID, String itemToEnable, MinecraftServer server) {
        try {
            RecipeManager.EnableItem(playerUUID, itemToEnable, server);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void disableItem(String playerUUID, String itemToDisable, MinecraftServer server) {
        try {
            RecipeManager.DisableItem(playerUUID, itemToDisable, server);
        } catch (IOException e) {
            LOGGER.error(e.toString());
        }
    }

    public void enableNamespace(String playerUUID, String namespaceToEnable, MinecraftServer server) {
        try {
            RecipeManager.EnableNamespace(playerUUID, namespaceToEnable, server);
        } catch (IOException e) {
            LOGGER.error(e.toString());
        }
    }

    public void disableNamespace(String playerUUID, String namespacetoDisable, MinecraftServer server) {
        try {
            RecipeManager.DisableNamespace(playerUUID, namespacetoDisable, server);
        } catch (IOException e) {
            LOGGER.error(e.toString());
        }
    }

    public void enableAll(String playerUUID, MinecraftServer server) {
        try {
            RecipeManager.EnableAll(playerUUID, server);
        } catch (IOException e) {
            LOGGER.error(e.toString());
        }
    }

    public void disableAll(String playerUUID, MinecraftServer server) {
        try {
            RecipeManager.DisableAll(playerUUID, server);
        } catch (IOException e) {
            LOGGER.error(e.toString());
        }
    }
}
