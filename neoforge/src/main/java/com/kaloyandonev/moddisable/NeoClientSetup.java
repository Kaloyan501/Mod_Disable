package com.kaloyandonev.moddisable;

import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.ScreenCrator;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;

public class NeoClientSetup {
    public static void ClientCode(final FMLCommonSetupEvent event) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            //logger.warn("ClientCode is about to run!");
            Minecraft.getInstance().execute(() -> {
                // Create your screen Creator instance
                ScreenCrator screenCrator = new ScreenCrator(
                        Component.literal("Migration Title"), // Title of the screen
                        Component.literal("Description of the screen"), // Description
                        confirmed -> false
                );

                // Open the new screen
                Minecraft.getInstance().setScreen(screenCrator);
            });
        }
    }
}
