package com.kaloyandonev.moddisable.mixins;

import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeManager.class)
public interface RecipeManagerAccessor {
    @Accessor("byName")
    Map<ResourceLocation, RecipeHolder<?>> getByName();

    @Accessor("byName")
    void setByName(Map<ResourceLocation, RecipeHolder<?>> byName);
}