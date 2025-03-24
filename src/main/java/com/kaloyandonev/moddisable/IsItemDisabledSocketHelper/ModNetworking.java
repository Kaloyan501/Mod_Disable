/*
 * ModDisable
 * A Minecraft Mod to disable other Mods
 * Copyright (C) 2024 Kaloyan Ivanov Donev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kaloyandonev.moddisable.IsItemDisabledSocketHelper;

import com.kaloyandonev.moddisable.DisableModMain;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.neoforged.neoforge.network.registration.NetworkChannel;

public class ModNetworking {
    // The protocol version for your messages.
    private static final String PROTOCOL_VERSION = "1";

    /**
     * Registers a new network channel along with all packets.
     * The callback (the second parameter) is where you define your messages.
     */
    public static final NetworkChannel CHANNEL = NetworkRegistry.register(
            // The channel ID (ResourceLocation)
            new ResourceLocation(DisableModMain.MODID, "main_channel"),

            // The channel configuration callback
            (channel) -> {
                // Register your first packet (client -> server, for example).
                channel.message(IsItemDisableDedicatedPacket.class, 0)
                        .decoder(IsItemDisableDedicatedPacket::decode)
                        .encoder(IsItemDisableDedicatedPacket::encode)
                        // 'consumerMainThread' typically schedules handling on the main thread
                        .consumerMainThread(IsItemDisableDedicatedPacket::handle)
                        .add();

                // Register your second packet (server -> client, for example).
                channel.message(ItemDisableDedicatedResponsePacket.class, 1)
                        .decoder(ItemDisableDedicatedResponsePacket::decode)
                        .encoder(ItemDisableDedicatedResponsePacket::encode)
                        .consumerMainThread(ItemDisableDedicatedResponsePacket::handle)
                        .add();
            },

            // The protocol version
            PROTOCOL_VERSION
    );

    /**
     * Example usage from elsewhere in your code:
     * ModNetworking.CHANNEL.sendToServer(new IsItemDisableDedicatedPacket(itemName));
     */
}
