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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


        //Send packet using ModNetworking.CHANNEL.sendToServer(new IsItemDisableDedicatedPacket(itemName));

public class ItemDisableDedicatedResponsePacket {
    private final String itemName;
    private final boolean isDisabled;
    public static String IsItemDisabled = "Awaiting Data";

    public ItemDisableDedicatedResponsePacket(String itemName, boolean isDisabled) {
        this.itemName = itemName;
        this.isDisabled = isDisabled;
    }

    // Decode the packet data from the byte buffer
    public static ItemDisableDedicatedResponsePacket decode(FriendlyByteBuf buf) {
        String itemName = buf.readUtf(32767); // Read the item name
        boolean isDisabled = buf.readBoolean(); // Read the boolean (isDisabled)
        return new ItemDisableDedicatedResponsePacket(itemName, isDisabled);
    }

    // Encode the packet data into the byte buffer
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(itemName);
        buf.writeBoolean(isDisabled);
    }

    // Handle the packet on the client side
    public static void handle(ItemDisableDedicatedResponsePacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Update the static string or condition variable
            DataAwaiter.isItemDisabled = message.isDisabled ? "Disabled" : "Enabled";
            // Notify waiting function (if needed)
            DataAwaiter.getInstance().notifyDataReceived(message.isDisabled);
        });
        context.setPacketHandled(true);
    }
}
