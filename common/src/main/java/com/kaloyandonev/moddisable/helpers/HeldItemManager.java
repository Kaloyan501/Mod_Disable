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

package com.kaloyandonev.moddisable.helpers;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.io.IOException;

public class HeldItemManager {
    public static void EnableHeldItem(Player player, CommandSourceStack source, MinecraftServer server) {
        Item HeldItem = player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
        try {
            RecipeManager.EnableItem(player.getStringUUID(), HeldItem.toString(), server);
        } catch (IOException e) {
            source.sendFailure(Component.literal("Exception thrown while trying to disable item in hand. " + e));
        }
    }

    public static void DisableHeldItem(Player player, CommandSourceStack source, MinecraftServer server) {
        Item HeldItem = player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
        try {
            RecipeManager.DisableItem(player.getStringUUID(), HeldItem.toString(), server);
        } catch (IOException e) {
            source.sendFailure(Component.literal("Exception thrown while trying to disable item in hand. " + e));
        }
    }

    public static void EnableHeldNamespace(Player player, CommandSourceStack source, MinecraftServer server) {
        Item HeldItem = player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
        try {
            String input = HeldItem.toString();
            String result = input.replaceAll(":(.*)", "");
            RecipeManager.EnableNamespace(player.getStringUUID(), result, server);
        } catch (IOException e) {
            source.sendFailure(Component.literal("Exception thrown while trying to disable item in hand. " + e));
        }
    }

    public static void DisableHeldNamespace(Player player, CommandSourceStack source, MinecraftServer server) {
        Item HeldItem = player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
        try {
            String input = HeldItem.toString();
            String result = input.replaceAll(":(.*)", "");
            RecipeManager.DisableNamespace(player.getStringUUID(), result, server);
        } catch (IOException e) {
            source.sendFailure(Component.literal("Exception thrown while trying to disable item in hand. " + e));
        }
    }
}
