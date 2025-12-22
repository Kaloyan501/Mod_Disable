package com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

public class ClientTickHandler {

    public static boolean hasRun = false;
    public static long startTime = 0;
    public static boolean hasRendered = false;
    public static boolean migrationConfrimed;

    public static boolean onClientTick() {
        if (!hasRendered && Migration_110_Json_Check.checkForDisableModFolder()) {

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
        return migrationConfrimed;
    }
}
