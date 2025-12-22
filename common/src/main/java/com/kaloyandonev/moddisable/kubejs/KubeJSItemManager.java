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
