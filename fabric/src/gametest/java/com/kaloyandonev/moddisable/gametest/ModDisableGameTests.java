package com.kaloyandonev.moddisable.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.gametest.framework.GameTestHelper;

import static org.assertj.core.api.Assertions.assertThat;

public class ModDisableGameTests {

    @GameTest(setupTicks = 10, required = true)
    public void vanillaItemsAreRegistered(GameTestHelper helper) {
        // Verify that Minecraft item registry is available and contains vanilla items
        assertThat(BuiltInRegistries.ITEM.containsKey(Identifier.tryParse("minecraft:diamond"))).isTrue();
        assertThat(BuiltInRegistries.ITEM.containsKey(Identifier.tryParse("minecraft:apple"))).isTrue();
        assertThat(BuiltInRegistries.ITEM.getKey(Items.DIAMOND)).isEqualTo(Identifier.tryParse("minecraft:diamond"));

        // Verify we can create a mock player in the test level
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        assertThat(player).isNotNull();
        assertThat(player.getName().getString()).isNotEmpty();

        helper.succeed();
    }

    @GameTest(setupTicks = 10, required = true)
    public void mockPlayerHasLevel(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        assertThat(player).isNotNull();
        assertThat(player.level()).isNotNull();
        assertThat(player.blockPosition()).isEqualTo(BlockPos.ZERO);

        helper.succeed();
    }
}
