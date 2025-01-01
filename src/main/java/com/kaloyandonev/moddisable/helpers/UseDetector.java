package com.kaloyandonev.moddisable.helpers;

import com.kaloyandonev.moddisable.DisableModMain;
import com.kaloyandonev.moddisable.IsItemDisabledSocketHelper.DataAwaiter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = DisableModMain.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class UseDetector {
    private static final Logger logger = LogManager.getLogger();

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        BlockPos pos = event.getPos();
        handleUse(player, event.getItemStack(), pos, () -> event.setCanceled(true));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        BlockPos pos = player.blockPosition();
        handleUse(player, event.getItemStack(), pos, () -> event.setCanceled(true));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onUseItemFinish(LivingEntityUseItemEvent.Finish event) {
        Player player = (Player) event.getEntity();
        BlockPos pos = player.blockPosition();
        handleUse(player, event.getItem(), pos, () -> event.setCanceled(true));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        BlockPos pos = player.blockPosition();
        handleUse(player, player.getMainHandItem(), pos, () -> event.setCanceled(true));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockInteraction(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level world = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = world.getBlockState(pos);

        ItemStack blockItemStack = new ItemStack(state.getBlock().asItem());
        handleUse(player, blockItemStack, pos, () -> event.setCanceled(true));

    }

    private void handleUse(Player player, ItemStack itemStack, BlockPos pos, Runnable cancelAction) {
        if (player == null || itemStack.isEmpty() || pos == null) return;

        // Check if the item is disabled locally
        if (!ServerCheckHelper.isConnectedToDedicatedServer()) {
            if (JsonHelper.isItemDisabled(itemStack.getItem(), player)) {
                handleItemUse(player, itemStack);
                syncInventory(player); // Sync inventory after cancel
                cancelAction.run(); // Cancel the action
            }
        } else {
            // For dedicated servers, check item state from the server
            CompletableFuture<Boolean> futureResponse = DataAwaiter.getInstance().sendAndAwaitResponse(itemStack.getItem().toString());
            futureResponse.thenAccept(isDisabled -> {
                if (isDisabled) {
                    player.getServer().execute(() -> {
                        handleItemUse(player, itemStack);
                        syncInventory(player); // Sync inventory after server response
                        cancelAction.run(); // Cancel the action
                    });
                }
            }).exceptionally(e -> {
                logger.error("Error checking item state: {}", e.getMessage(), e);
                return null;
            });
        }
    }

    private void handleItemUse(Player player, ItemStack itemStack) {
        if (player == null || itemStack.isEmpty()) return;

        // Notify the player if the item is disabled
        if (!JsonHelper.isItemDisabled(itemStack.getItem(), player) && player instanceof ServerPlayer) {
            CommandSourceStack sourceStack = player.createCommandSourceStack();
            sourceStack.sendFailure(Component.literal("This item is disabled!"));
        }
    }

    private void syncInventory(Player player) {
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            serverPlayer.inventoryMenu.sendAllDataToRemote(); // Sync the inventory
        }
    }
}
