package com.kaloyandonev.moddisable;

import com.kaloyandonev.moddisable.events.ModEvents;
import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.ScreenCrator;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class FabricMain implements ModInitializer {
    
    @Override
    public void onInitialize() {
        
        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
        Constants.LOG.info("Hello Fabric world!");
        CommonClass.init();
        ModEvents.onCommandRegister();
    }

    //TO DO: ADD LISTENER
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
