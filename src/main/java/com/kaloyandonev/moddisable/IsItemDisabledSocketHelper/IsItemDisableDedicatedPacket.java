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

import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.StaticPathStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Supplier;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IsItemDisableDedicatedPacket {
    private final String itemName;
    private final UUID playerUUID;

    private static final Logger logger = LogManager.getLogger(IsItemDisableDedicatedPacket.class);

    public IsItemDisableDedicatedPacket(String itemName, UUID playerUUID) {
        this.itemName = itemName;
        this.playerUUID = playerUUID;
    }

    // Decode the packet data from the byte buffer
    public static IsItemDisableDedicatedPacket decode(FriendlyByteBuf buf) {
        String itemName = buf.readUtf(32767);  // Read the item name
        UUID playerUUID = buf.readUUID();  // Read the player's UUID
        return new IsItemDisableDedicatedPacket(itemName, playerUUID);
    }

    // Encode the packet data into the byte buffer
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(itemName);
        buf.writeUUID(playerUUID);
    }

    // Handle the packet on the server side
    public static void handle(IsItemDisableDedicatedPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Handle the packet on the server thread
            boolean isDisabled = checkIfItemDisabledForPlayer(message.itemName, message.playerUUID);

            // Send the response back to the client
            ModNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(context::getSender),
                    new ItemDisableDedicatedResponsePacket(message.itemName, isDisabled));
        });
        context.setPacketHandled(true);
    }

    // Server-side logic to check if the item is disabled for the player based on UUID
    private static boolean checkIfItemDisabledForPlayer(String itemName, UUID playerUUID) {
        // Get the folder containing the player's JSON files
        File folder = StaticPathStorage.getSubWorldFolderFile();

        // Ensure the folder exists
        if (folder == null || !folder.isDirectory()) {
            logger.error("[Mod Disable] Folder does not exist or is not a directory: {}", folder);
            return false;
        }

        // Construct the file path for the specific player's JSON file based on UUID
        File playerFile = new File(folder, playerUUID.toString() + ".json");

        // Check if the player's file exists
        if (!playerFile.exists()) {
            logger.error("[Mod Disable] Player JSON file does not exist: {}", playerFile);
            return false;
        }

        // Try to read and parse the player's JSON file
        try (FileReader reader = new FileReader(playerFile)) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

            // Get the "disabled_items" array from the JSON
            JsonArray disabledItems = jsonObject.getAsJsonArray("disabled_items");

            // Check if the item is in the "disabled_items" array
            for (JsonElement element : disabledItems) {
                if (element.getAsString().equals(itemName)) {
                    return true; // Item is disabled for this player
                }
            }
        } catch (IOException e) {
            logger.error("[Mod Disable] Error reading player JSON file: {}", e.getMessage());
            return false;
        }

        return false;  // Item is not disabled for this player
    }
}
