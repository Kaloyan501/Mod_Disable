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


package com.kaloyandonev.moddisable.IsItemDisabledSocketHelper;

import com.kaloyandonev.moddisable.DisableModMain;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

//Send packet using ModNetworking.CHANNEL.sendToServer(new IsItemDisableDedicatedPacket(itemName));

public class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";
    private static boolean isRegistered = false; // Add this flag

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(DisableModMain.MODID, "main_channel"), // Replace "yourmodid" with your mod's ID
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void registerPackets() {
        if (!isRegistered) { // Check if the channel is already registered
            int packetId = 0;

            // Register client-to-server packet (IsItemDisableDedicatedPacket)
            CHANNEL.registerMessage(packetId++, IsItemDisableDedicatedPacket.class,
                    IsItemDisableDedicatedPacket::encode,
                    IsItemDisableDedicatedPacket::decode,
                    IsItemDisableDedicatedPacket::handle);

            // Register server-to-client packet (ItemDisableDedicatedResponsePacket)
            CHANNEL.registerMessage(packetId++, ItemDisableDedicatedResponsePacket.class,
                    ItemDisableDedicatedResponsePacket::encode,
                    ItemDisableDedicatedResponsePacket::decode,
                    ItemDisableDedicatedResponsePacket::handle);

            isRegistered = true; // Set the flag to true to indicate that the channel is registered
        }
    }
}

