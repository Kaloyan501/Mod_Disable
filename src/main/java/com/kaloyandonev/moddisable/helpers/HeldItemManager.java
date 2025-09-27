package com.kaloyandonev.moddisable.helpers;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import java.io.IOException;

public class HeldItemManager {
    public static void EnableHeldItem(Player player, CommandSourceStack source){
        Item HeldItem = player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
        try {
            RecipeManager.EnableItem(player.getStringUUID(), HeldItem.toString());
        } catch (IOException e) {
            source.sendFailure(Component.literal("Exception thrown while trying to disable item in hand. " + e));
        }
    }

    public static void DisableHeldItem(Player player, CommandSourceStack source){
        Item HeldItem = player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
        try {
            RecipeManager.DisableItem(player.getStringUUID(), HeldItem.toString());
        } catch (IOException e) {
            source.sendFailure(Component.literal("Exception thrown while trying to disable item in hand. " + e));
        }
    }

    public static void EnableHeldNamespace(Player player, CommandSourceStack source){
        Item HeldItem = player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
        try {
            String input = HeldItem.toString();
            String result = input.replaceAll(":(.*)", "");
            RecipeManager.EnableNamespace(player.getStringUUID(), result);
        } catch (IOException e) {
            source.sendFailure(Component.literal("Exception thrown while trying to disable item in hand. " + e));
        }
    }

    public static void DisableHeldNamespace(Player player, CommandSourceStack source){
        Item HeldItem = player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
        try {
            String input = HeldItem.toString();
            String result = input.replaceAll(":(.*)", "");
            RecipeManager.DisableNamespace(player.getStringUUID(), result);
        } catch (IOException e) {
            source.sendFailure(Component.literal("Exception thrown while trying to disable item in hand. " + e));
        }
    }
}
