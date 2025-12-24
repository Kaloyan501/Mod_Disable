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

import com.kaloyandonev.moddisable.disablelogic.playerjoinsyncpacket.PlayerJoinSyncPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class UseDetector {

    private static void handleUse(Player player, ItemStack itemStack, BlockPos pos, Runnable cancelAction) {
        if (player == null || itemStack.isEmpty() || pos == null) return;

        // Check if the item is disabled locally
        if (!ServerCheckHelper.isConnectedToDedicatedServer()) {
            if (JsonHelper.isItemDisabled(itemStack.getItem(), player)) {
                handleItemUse(player, itemStack);
                syncInventory(player); // Sync inventory after cancel
                cancelAction.run(); // Cancel the action
            }
        } else {

            Item item = itemStack.getItem();
            Registry<Item> registry = BuiltInRegistries.ITEM;

            ResourceLocation key = registry.getKey(item);
            String idStr = key.toString();

            PacketDistributor.sendToServer(new PlayerJoinSyncPacket.PlayerJoinRequest(idStr, player.getStringUUID()));
        }
    }

    private static void handleItemUse(Player player, ItemStack itemStack) {
        if (player == null || itemStack.isEmpty()) return;

        // Notify the player if the item is disabled
        if (!JsonHelper.isItemDisabled(itemStack.getItem(), player) && player instanceof ServerPlayer) {
            CommandSourceStack sourceStack = player.createCommandSourceStack();
            sourceStack.sendFailure(Component.literal("This item is disabled!"));
            player.displayClientMessage(Component.literal("This item is disabled!"), true);
        }
    }

    private static void syncInventory(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.inventoryMenu.sendAllDataToRemote(); // Sync the inventory
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer)) return;
        Player entity = event.getEntity();

        BlockPos pos = entity.blockPosition();
        handleUse(entity, event.getItemStack(), pos, () -> event.setCanceled(true));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onUseItemFinish(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof ServerPlayer)) return;
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player pPlayer)) {
            return; // Ignore non-player entities
        }

        BlockPos pos = pPlayer.blockPosition();
        handleUse(pPlayer, event.getItem(), pos, () -> event.setCanceled(true));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer)) return;
        Player entity = event.getEntity();

        BlockPos pos = entity.blockPosition();
        handleUse(entity, entity.getMainHandItem(), pos, () -> event.setCanceled(true));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        BlockState placedState = event.getPlacedBlock();
        Block placedBlock = placedState.getBlock();

        boolean isDisabled = JsonHelper.isItemDisabled(placedBlock.asItem(), player);

        if (isDisabled) {
            event.setCanceled(true);
        } else {
            return;
        }

        player.displayClientMessage(Component.literal("This item is disabled!"), true);

    }
}
