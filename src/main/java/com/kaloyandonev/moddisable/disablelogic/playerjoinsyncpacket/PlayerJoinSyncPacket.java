package com.kaloyandonev.moddisable.disablelogic.playerjoinsyncpacket;

import com.kaloyandonev.moddisable.DisableModMain;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PlayerJoinSyncPacket {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("PlayerJoinSyncPacket");
    }

    public record PlayerJoinSyncPacketRegister(String SerializedJson) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<PlayerJoinSyncPacketRegister> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(DisableModMain.MODID, "PlayerJoinSyncPacket"));

        public static final StreamCodec<ByteBuf, PlayerJoinSyncPacketRegister> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8,
                PlayerJoinSyncPacketRegister::SerializedJson,
                PlayerJoinSyncPacketRegister::new
        );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

}
