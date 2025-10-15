package com.kaloyandonev.moddisable.integrations.kubejs;

import com.kaloyandonev.moddisable.helpers.RecipeManager;

import java.io.IOException;

public class KubeJSItemManager {
    public void enableItem(String playerUUID, String itemToEnable) {
        try {
            RecipeManager.EnableItem(playerUUID, itemToEnable);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
