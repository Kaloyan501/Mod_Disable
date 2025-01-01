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

import net.minecraft.client.Minecraft;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataAwaiter {

    public static String isItemDisabled = "Awaiting Data"; // Track the status
    private final AtomicBoolean responseReceived = new AtomicBoolean(false); // Track if response is received
    private static DataAwaiter instance;

    // Singleton pattern to access the instance
    public static DataAwaiter getInstance() {
        if (instance == null) {
            instance = new DataAwaiter();
        }
        return instance;
    }

    // Method to send the packet and await the response
    public CompletableFuture<Boolean> sendAndAwaitResponse(String itemName) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Send packet to the server
        ModNetworking.CHANNEL.sendToServer(new IsItemDisableDedicatedPacket(itemName, Minecraft.getInstance().player.getUUID()));

        // Polling until the response is received
        new Thread(() -> {
            while (responseReceived.get() || "Awaiting Data".equals(isItemDisabled)) {
                try {
                    Thread.sleep(100); // Check every 100 milliseconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            // After the loop, we have the final status
            future.complete("Disabled".equals(isItemDisabled)); // Complete the future with the final value
        }).start();

        return future;
    }

    // Method to notify that data has been received
    public void notifyDataReceived(boolean isDisabled) {
        responseReceived.set(true); // Update response received
        isItemDisabled = isDisabled ? "Disabled" : "Enabled"; // Update status
    }
}
