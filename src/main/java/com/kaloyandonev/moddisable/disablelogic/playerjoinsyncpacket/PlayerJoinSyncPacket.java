package com.kaloyandonev.moddisable.disablelogic.playerjoinsyncpacket;

import ca.weblite.objc.Client;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaloyandonev.moddisable.DisableModMain;
import io.netty.buffer.ByteBuf;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.NoteBlockEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handlers.ClientPayloadHandler;
import net.neoforged.neoforge.network.handlers.ServerPayloadHandler;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static com.kaloyandonev.moddisable.DisableModMain.MODID;

public class PlayerJoinSyncPacket {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent ev) {
        PayloadRegistrar r = ev.registrar("player_join_sync")
                .executesOn(HandlerThread.NETWORK);

        // Client → Server: use ServerPayloadHandler::handleDataOnMain
        r.playToServer(
                PlayerJoinRequest.TYPE,
                PlayerJoinRequest.STREAM_CODEC,
                new DirectionalPayloadHandler<>(null,
                        ServerPayloadHandler::onRequest)
        );

        // Server → Client: use ClientPayloadHandler::handleDataOnMain
        r.playToClient(
                PlayerJoinResponse.TYPE,
                PlayerJoinResponse.STREAM_CODEC,
                new DirectionalPayloadHandler<>(ClientPayloadHandler::onResponse,
                        null)
        );
    }

    public record PlayerJoinRequest(String itemName, String uuid)
            implements CustomPacketPayload
    {
        public static final Type<PlayerJoinRequest> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "player_join_request"));

        public static final StreamCodec<ByteBuf, PlayerJoinRequest> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, PlayerJoinRequest::itemName,
                        ByteBufCodecs.STRING_UTF8, PlayerJoinRequest::uuid,
                        PlayerJoinRequest::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record PlayerJoinResponse(String itemName, String uuid, boolean isDisabled)
            implements CustomPacketPayload
    {
        public static final Type<PlayerJoinResponse> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "player_join_response"));

        public static final StreamCodec<ByteBuf, PlayerJoinResponse> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, PlayerJoinResponse::itemName,
                        ByteBufCodecs.STRING_UTF8, PlayerJoinResponse::uuid,
                        ByteBufCodecs.BOOL,        PlayerJoinResponse::isDisabled,
                        PlayerJoinResponse::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public class ServerPayloadHandler {
        public static void onRequest(PlayerJoinRequest req, IPayloadContext ctx) {
            String item = req.itemName();
            UUID id = UUID.fromString(req.uuid());

            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            Path serverDir = server.getWorldPath(LevelResource.ROOT);

            Path jsonPath = serverDir.resolve(id.toString() + ".json");

            boolean isDisabled = false;


            try {
                String content = Files.readString(jsonPath, StandardCharsets.UTF_8);

                JsonObject obj = JsonParser
                        .parseString(content)
                        .getAsJsonObject();

                JsonArray arr = obj.getAsJsonArray("disabled_items");


                for (JsonElement e : arr) {
                    if (item.equals(e.getAsString())) {
                        isDisabled = true;
                        break;
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            PlayerJoinResponse response = new PlayerJoinResponse(item, id.toString(), isDisabled);
            ctx.reply(response);

        }
    }

    public class ClientPayloadHandler {
        public static void onResponse(PlayerJoinResponse resp, IPayloadContext ctx) {
            // … same body as your handleDataOnMain …
        }
    }


}
