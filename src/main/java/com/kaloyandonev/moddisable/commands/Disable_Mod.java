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

package com.kaloyandonev.moddisable.commands;

import com.kaloyandonev.moddisable.helpers.isSinglePlayer;
import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.kaloyandonev.moddisable.helpers.RecipeDisabler;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import static com.kaloyandonev.moddisable.helpers.config.InitialStateDataHandler.executeConfigRequest;
import static com.kaloyandonev.moddisable.helpers.config.InitialStateDataHandler.executeReinitRequest;


public class Disable_Mod{

    private static final Logger logger = LogManager.getLogger(Disable_Mod.class);
    private final isSinglePlayer isSinglePlayer = new isSinglePlayer();

    public Disable_Mod(IEventBus modEventBus){

        modEventBus.addListener(this::ClientCode);
        modEventBus.addListener(this::onLoadComplete);

        NeoForge.EVENT_BUS.register(this);
    }

    public void onLoadComplete(FMLLoadCompleteEvent event){
        isSinglePlayer.checkisSinglePlayer(event);
    }

    //@OnlyIn(Dist.CLIENT)
    private void ClientCode(final FMLCommonSetupEvent event) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            logger.warn("ClientCode is about to run!");
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


    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(
                Commands.literal("disable_mod")
                        // 'enable' command
                        .then(Commands.literal("enable")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.literal("namespace")
                                        .then(Commands.argument("namespace", StringArgumentType.string())
                                                .executes(context -> execute(context, "enable", StringArgumentType.getString(context, "namespace")))
                                        )
                                )
                                .then(Commands.literal("item")
                                        .then(Commands.argument("item", ItemArgument.item(buildContext))
                                                .executes(context -> executeWithItem(context, "enable", ItemArgument.getItem(context, "item").getItem()))
                                        )
                                )
                        )
                        // 'disable' command
                        .then(Commands.literal("disable")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.literal("namespace")
                                        .then(Commands.argument("namespace", StringArgumentType.string())
                                                .executes(context -> execute(context, "disable", StringArgumentType.getString(context, "namespace")))
                                        )
                                )
                                .then(Commands.literal("item")
                                        .then(Commands.argument("item", ItemArgument.item(buildContext))
                                                .executes(context -> executeWithItem(context, "disable", ItemArgument.getItem(context, "item").getItem()))
                                        )
                                )
                        )
                        // 'config' command
                        .then(Commands.literal("config")
                                .then(Commands.literal("DefaultDisabledItemsListFromPlayerUUID")
                                        .requires(source -> source.hasPermission(2))
                                        .then(Commands.argument("player", StringArgumentType.string())
                                                .executes(context -> executeConfigRequest(context, "DefaultDisabledItemsListFromPlayerUUID", StringArgumentType.getString(context, "player"), context.getSource()))
                                        )
                                )
                                .then(Commands.literal("init")
                                        .executes(context -> executeConfigRequest(context, "Init", "Null", context.getSource()))
                                )
                                .then(Commands.literal("reinit")
                                        .requires(source -> source.hasPermission(2))
                                        .then(Commands.argument("PlayerUUID", StringArgumentType.string())
                                                .executes(context -> executeReinitRequest(context, "Reinit", StringArgumentType.getString(context, "PlayerUUID"), "nonconfirm"))
                                                .then(Commands.argument("confirm", StringArgumentType.string())
                                                        .executes(context -> executeReinitRequest(context, "Reinit", StringArgumentType.getString(context, "PlayerUUID"), StringArgumentType.getString(context, "confirm")))
                                                )
                                        )
                                )
                        )
        );
    }

    //Method to handle namespaces
    private static int execute(CommandContext<CommandSourceStack> context, String action,  String namespace){
        CommandSourceStack source = context.getSource();
        String actionTarget = action + " " + "namespace";

        Player player;

        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(Component.literal("You must be a player to use this command!"));
            return 0;
        }

        switch (actionTarget) {
            case "enable namespace":
                try {
                    RecipeDisabler.EnableNamespace(player.getStringUUID(), namespace);
                } catch (IOException e) {
                    logger.error("Enabling namespace failed.", e);
                }
                source.sendSuccess(() -> Component.literal("[ModDisable] Mod namespace enabled!"), false);
                break;
            case "disable namespace":
                logger.info("[Mod Disable]Namespace argument: {}", namespace);
                try {
                    RecipeDisabler.DisableNamespace(player.getStringUUID(), namespace);
                } catch (IOException e){
                    logger.error("Disabling namespace failed.", e);
                }

                source.sendSuccess(() -> Component.literal("[ModDisable] Mod namespace disabled!"), false);
                break;
            default:
                source.sendFailure(Component.literal("[ModDisable] Invalid action!"));
                return 0;
        }
        return 1;
    }
    //Method to handle items
    private static int executeWithItem(CommandContext<CommandSourceStack> context, String action, Item item){
        CommandSourceStack source = context.getSource();
        ItemStack itemStack = new ItemStack(item);
        String itemName = itemStack.getDisplayName().getString();
        Player player;

        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(Component.literal("You must be a player to use this command!"));
            return 0;
        }


        switch (action + " " + "item") {
            case "enable item":
                source.sendSuccess(() -> Component.literal("[ModDisable] Mod item " + itemName + " enabled!"), false);
                Registry<Item> registry = BuiltInRegistries.ITEM;
                ResourceLocation key = registry.getKey(item);
                String idStr = key.toString();

                try{
                    RecipeDisabler.EnableItem(player.getStringUUID(), idStr);
                } catch (IOException e) {
                    logger.error("Enabling item failed with exception.", e);
                }

                break;
            case "disable item":

                source.sendSuccess(() -> Component.literal("[ModDisable] Mod item " + itemName + " disabled!"), false);
                Registry<Item> registry1 = BuiltInRegistries.ITEM;
                ResourceLocation key1 = registry1.getKey(item);
                String idStr1 = key1.toString();

                try {
                    RecipeDisabler.DisableItem(player.getStringUUID(), idStr1);
                } catch (IOException e) {
                    logger.error("Disabling item failed with exception", e);
                }


                break;
            default:
                source.sendFailure(Component.literal("[ModDisable] Invalid action!"));
                return 0;
        }
        return 1;
    }
}
