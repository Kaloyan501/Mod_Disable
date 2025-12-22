package com.kaloyandonev.moddisable.helpers;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public final class UseDetector {

    private UseDetector() {
    }

    public static void register() {

        /* =====================
           ITEM USE (right-click)
           ===================== */
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResultHolder.pass(player.getItemInHand(hand));
            }

            ItemStack stack = serverPlayer.getItemInHand(hand);
            BlockPos pos = serverPlayer.blockPosition();

            boolean cancel = runHandleUse(serverPlayer, stack, pos);

            return cancel
                    ? InteractionResultHolder.fail(stack)
                    : InteractionResultHolder.pass(stack);
        });

        /* =====================
           ENTITY ATTACK
           ===================== */
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }

            ItemStack stack = serverPlayer.getMainHandItem();
            BlockPos pos = serverPlayer.blockPosition();

            boolean cancel = runHandleUse(serverPlayer, stack, pos);

            return cancel ? InteractionResult.FAIL : InteractionResult.PASS;
        });

        /* =====================
           BLOCK PLACE / USE
           ===================== */
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }

            ItemStack stack = serverPlayer.getItemInHand(hand);
            if (!(stack.getItem() instanceof BlockItem)) {
                return InteractionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();

            boolean cancel = runHandleUse(serverPlayer, stack, pos);

            return cancel ? InteractionResult.FAIL : InteractionResult.PASS;
        });
    }

    /* =========================================================
       FABRIC BRIDGE — adapts Runnable-based cancellation
       ========================================================= */
    private static boolean runHandleUse(Player player, ItemStack stack, BlockPos pos) {
        final boolean[] cancel = {false};
        handleUse(player, stack, pos, () -> cancel[0] = true);
        return cancel[0];
    }

    /* =========================================================
       ORIGINAL LOGIC (unchanged)
       ========================================================= */
    private static void handleUse(Player player, ItemStack itemStack, BlockPos pos, Runnable cancelAction) {
        if (player == null || itemStack.isEmpty() || pos == null) return;

        if (!ServerCheckHelper.isConnectedToDedicatedServer()) {
            if (JsonHelper.isItemDisabled(itemStack.getItem(), player)) {
                handleItemUse(player);
                syncInventory(player);
                cancelAction.run();
            }
        }
    }

    private static void handleItemUse(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        CommandSourceStack source = serverPlayer.createCommandSourceStack();
        source.sendFailure(Component.literal("This item is disabled!"));
        serverPlayer.displayClientMessage(Component.literal("This item is disabled!"), true);
    }

    private static void syncInventory(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.inventoryMenu.sendAllDataToRemote();
        }
    }
}
