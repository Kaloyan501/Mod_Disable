package com.kaloyandonev.moddisable.events.custom;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class PlayerInteractEvent {
    public interface SheepShearCallback {
        Event<SheepShearCallback> EVENT = EventFactory.createArrayBacked(SheepShearCallback.class,
                (listeners) -> (player, sheep) -> {
                    for (SheepShearCallback listener : listeners) {
                        InteractionResult result = listener.interact(player, sheep);

                        if (result != InteractionResult.PASS) {
                            return result;
                        }
                    }

                    return InteractionResult.PASS;
                });

        InteractionResult interact(Player player, Sheep sheep);
    }

    @Mixin(Sheep.class)
    public class SheepEntityMixin {

        @Inject(
                method = "mobInteract",
                at = @At(
                        value = "INVOKE",
                        target = "Lnet/minecraft/world/entity/animal/Sheep;shear(Lnet/minecraft/sounds/SoundSource;)V"
                ),
                cancellable = true
        )
        private void onShear(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
            InteractionResult result = SheepShearCallback.EVENT.invoker()
                    .interact(player, (Sheep) (Object) this);

            if (result == InteractionResult.FAIL) {
                cir.setReturnValue(result);
            }
        }
    }
}
