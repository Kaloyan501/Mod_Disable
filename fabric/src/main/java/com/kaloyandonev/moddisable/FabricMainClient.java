package com.kaloyandonev.moddisable;

import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.ScreenCrator;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class FabricMainClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCode();
    }

    private void ClientCode(){
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            Minecraft.getInstance().execute(() -> {
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
