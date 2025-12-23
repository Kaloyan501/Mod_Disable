package com.kaloyandonev.moddisable;

import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.ScreenCrator;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(
        modid = Constants.MOD_ID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class NeoClientSetup {

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ScreenCrator screenCrator = new ScreenCrator(
                    Component.literal("Migration Title"),
                    Component.literal("Description of the screen"),
                    confirmed -> false
            );

            Minecraft.getInstance().setScreen(screenCrator);
        });
    }
}
