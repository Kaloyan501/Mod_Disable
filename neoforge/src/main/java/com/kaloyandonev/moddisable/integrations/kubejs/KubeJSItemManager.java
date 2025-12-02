package com.kaloyandonev.moddisable.integrations.kubejs;

import com.kaloyandonev.moddisable.helpers.RecipeManager;
import net.minecraft.world.item.crafting.Recipe;

import java.io.IOException;

public class KubeJSItemManager {
    public void enableItem(String playerUUID, String itemToEnable) {
        try {
            RecipeManager.EnableItem(playerUUID, itemToEnable);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void disableItem(String playerUUID, String itemToDisable) {
        try {
            RecipeManager.DisableItem(playerUUID, itemToDisable);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    public void enableNamespace(String playerUUID, String namespaceToEnable) {
        try {
            RecipeManager.EnableNamespace(playerUUID, namespaceToEnable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void disableNamespace(String playerUUID, String namespacetoDisable) {
        try {
            RecipeManager.DisableNamespace(playerUUID, namespacetoDisable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void enableAll(String playerUUID) {
        try {
            RecipeManager.EnableAll(playerUUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void disableAll(String playerUUID){
        try {
            RecipeManager.DisableAll(playerUUID);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
