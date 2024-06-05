//ModDisable
//A Minecraft Mod to disable other Mods
//Copyright (C) 2024 Kaloyan Ivanov Donev

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

import com.kaloyandonev.moddisable.DisableModMain;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.core.jmx.Server;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import javax.json.Json;
import javax.swing.text.JTextComponent;
import java.awt.*;

@Mod.EventBusSubscriber(modid = DisableModMain.MODID, bus = EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class UseDetector {
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            ItemStack mainHandItem = player.getMainHandItem();
            if (JsonHelper.isItemDisabled(mainHandItem.getItem(), player)) {
                handleItemUse(player, event.getItemStack());
                event.setCanceled(true);
            }
        }

    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player) {
            Player player = (Player) entity;
            ItemStack mainHandItem = player.getMainHandItem();
            if (JsonHelper.isItemDisabled(mainHandItem.getItem(), player)) {
                handleItemUse(player, event.getItemStack());
                event.setCanceled(true);
            }

        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onUseItemFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof Player) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            ItemStack mainHandItem = player.getMainHandItem();
            if (JsonHelper.isItemDisabled(mainHandItem.getItem(), player)) {
                handleItemUse(player, event.getItem());
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onAttackEntity(AttackEntityEvent event) {
        Player attacker = event.getEntity();
        ItemStack mainHandItem = attacker.getMainHandItem();
        if (event.getEntity() instanceof Player) {
            if (JsonHelper.isItemDisabled(mainHandItem.getItem(), attacker)) {
                Player player = (Player) event.getEntity();
                handleItemUse(player, event.getEntity().getMainHandItem());
                event.setCanceled(true);
            }
        }
    }

    private void handleItemUse(Player player, ItemStack itemStack) {
        if (player ==null || itemStack == null) return;

        if (JsonHelper.isItemDisabled(itemStack.getItem(), player) == false) {
            if (player instanceof ServerPlayer) {
                CommandSourceStack sourceStack = player.createCommandSourceStack();

                sourceStack.sendFailure(Component.literal("This item is disabled!"));
            }
        }
    }
}
