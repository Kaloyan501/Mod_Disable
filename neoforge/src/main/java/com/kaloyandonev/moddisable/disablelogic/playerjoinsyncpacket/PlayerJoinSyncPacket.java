//ModDisable
//A Minecraft Mod to disable other Mods
//Copyright (C) 2024-2025 Kaloyan Ivanov Donev

//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.
package com.kaloyandonev.moddisable.disablelogic.playerjoinsyncpacket;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaloyandonev.moddisable.Constants;
import com.kaloyandonev.moddisable.helpers.PathHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class PlayerJoinSyncPacket {

    public static final Logger LOGGER = LoggerFactory.getLogger(Constants.MOD_ID);

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
            implements CustomPacketPayload {
        public static final Type<PlayerJoinRequest> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "player_join_request"));

        public static final StreamCodec<ByteBuf, PlayerJoinRequest> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, PlayerJoinRequest::itemName,
                        ByteBufCodecs.STRING_UTF8, PlayerJoinRequest::uuid,
                        PlayerJoinRequest::new
                );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record PlayerJoinResponse(String itemName, String uuid, boolean isDisabled)
            implements CustomPacketPayload {
        public static final Type<PlayerJoinResponse> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "player_join_response"));

        public static final StreamCodec<ByteBuf, PlayerJoinResponse> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, PlayerJoinResponse::itemName,
                        ByteBufCodecs.STRING_UTF8, PlayerJoinResponse::uuid,
                        ByteBufCodecs.BOOL, PlayerJoinResponse::isDisabled,
                        PlayerJoinResponse::new
                );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static class ServerPayloadHandler {
        public static void onRequest(PlayerJoinRequest req, IPayloadContext ctx) {
            String item = req.itemName();
            UUID id = UUID.fromString(req.uuid());

            boolean isDisabled = false;


            try {

                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                Path jsonPath = PathHelper.getPlayerJsonFile(req.uuid, server);


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
            } catch (IOException e) {
                LOGGER.error(e.toString());
            }

            PlayerJoinResponse response = new PlayerJoinResponse(item, id.toString(), isDisabled);
            ctx.reply(response);

        }
    }

    public static class ClientPayloadHandler {
        public static void onResponse(PlayerJoinResponse resp, IPayloadContext ctx) {
        }
    }
}
