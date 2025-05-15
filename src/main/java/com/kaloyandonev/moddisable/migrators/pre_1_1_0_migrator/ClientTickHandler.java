//ModDisable
//A Minecraft Mod to disable other Mods
//Copyright (C) 2024-2025 Kaloyan Ivanov Donev

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


package com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator;

import com.kaloyandonev.moddisable.DisableModMain;
import com.kaloyandonev.moddisable.commands.Disable_Mod;
import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.ScreenCrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = DisableModMain.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientTickHandler {

    public static boolean hasRun = false;
    public static long startTime = 0;
    public static boolean hasRendered = false;
    public static boolean migrationConfrimed;


    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        if (hasRendered == false && Migration_110_Json_Check.checkForDisableModFolder()) {

            Minecraft minecraft = Minecraft.getInstance();

            // Check if we are on the title screen and not yet run
            if (minecraft.screen instanceof TitleScreen) {
                if (!hasRun) {
                    // Start a delay to ensure the title screen is almost loaded
                    startTime = System.currentTimeMillis();
                    hasRun = true;
                }

                // Check if the delay has passed to show your screen
                if (System.currentTimeMillis() - startTime > 5000) { // Adjust delay as needed
                    // Show your custom screen
                    minecraft.execute(() -> {
                        ScreenCrator screenCrator = new ScreenCrator(
                                Component.literal("Old Mod Disable data detected!"),
                                Component.literal("Mod Disable version 1.1.0 and up has changed the way disabled items are stored. The previous system used an all world approach, while 1.1.0 uses a per world approach. If you don't know what this message means, it is recommended to click Migrate Mod Disable Data to specific world and select the main world where you play this modpack. Thank you for reading this warning!"),
                                confirmed -> {
                                    migrationConfrimed = confirmed;
                                    if (confirmed) {
                                        migrationConfrimed = true;
                                        ToastNotifier.showToast("Select World", "Select world to migrate data to.");
                                    }
                                    // Handle migration logic here
                                    return false; // Adjust as needed
                                }
                        );

                        // Set the custom screen
                        minecraft.setScreen(screenCrator);
                    });

                    // Reset the flag and timer to prevent repeated screen setting
                    hasRun = false;
                    startTime = 0;
                    hasRendered = true;
                }
            }

        }
    }

}
