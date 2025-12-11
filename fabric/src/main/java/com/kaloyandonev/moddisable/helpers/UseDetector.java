package com.kaloyandonev.moddisable.helpers;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class UseDetector {

    public static void register() {
        UseItemCallback.EVENT.register((Player player, Level world, InteractionHand hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            return InteractionResultHolder.pass(stack);
        });
    }

    private static void handleUse(Player player, ItemStack itemStack, BlockPos pos, Runnable cancelAction){
        if (player == null || itemStack.isEmpty() || pos == null) return;

        if (!ServerCheckHelper.isConnectedToDedicatedServer()) {
            if (JsonHelper.isItemDisabled(itemStack.getItem(), player)) {
                handleItemUse(player, itemStack);
                syncInventory(player);
                cancelAction.run();
            }
        } else {
            //Implement handling on server
        }
    }

    private static void handleItemUse(Player player, ItemStack itemStack) {
        if (player == null || itemStack.isEmpty()) return;

        if (!JsonHelper.isItemDisabled(itemStack.getItem(), player) && player instanceof ServerPlayer) {
            CommandSourceStack sourceStack = player.createCommandSourceStack();
            sourceStack.sendFailure(Component.literal("This item is disabled!"));
            player.displayClientMessage(Component.literal("This item is disabled!"), true);
        }
    }

    private static void syncInventory(Player player){
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            serverPlayer.inventoryMenu.sendAllDataToRemote();
        }
    }
}