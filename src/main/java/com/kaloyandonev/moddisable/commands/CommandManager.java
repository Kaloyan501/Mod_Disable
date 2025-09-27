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

import com.kaloyandonev.moddisable.helpers.HeldItemManager;
import com.kaloyandonev.moddisable.helpers.RecipeManager;
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
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Objects;

import static com.kaloyandonev.moddisable.helpers.config.InitialStateDataHandler.executeConfigRequest;
import static com.kaloyandonev.moddisable.helpers.config.InitialStateDataHandler.executeReinitRequest;


public class CommandManager {

    private static final Logger logger = LogManager.getLogger(CommandManager.class);
    private final isSinglePlayer isSinglePlayer = new isSinglePlayer();

    public CommandManager(IEventBus modEventBus) {

        modEventBus.addListener(this::ClientCode);
        modEventBus.addListener(this::onLoadComplete);

        NeoForge.EVENT_BUS.register(this);
    }

    public void onLoadComplete(FMLLoadCompleteEvent event) {
        isSinglePlayer.checkisSinglePlayer(event);
    }

    @OnlyIn(Dist.CLIENT)
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
                                        .then(Commands.literal("hand")
                                                .executes(context -> execute(context, "enable", "hand"))
                                        )
                                        .then(Commands.argument("namespace", StringArgumentType.string())
                                                .executes(context -> execute(context, "enable", StringArgumentType.getString(context, "namespace")))
                                        )
                                )
                                .then(Commands.literal("item")
                                        .then(Commands.literal("hand")
                                                .executes(context -> executeWithItem(context, "enable", null, true))
                                        )
                                        .then(Commands.argument("item", ItemArgument.item(buildContext))
                                                .executes(context -> executeWithItem(context, "enable", ItemArgument.getItem(context, "item").getItem(), false))
                                        )
                                )
                                .then(Commands.literal("all")
                                        .executes(context -> execute(context, "enable", "all"))
                                )
                        )
                        // 'disable' command
                        .then(Commands.literal("disable")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.literal("namespace")
                                        .then(Commands.literal("hand")
                                                .executes(context -> execute(context, "disable", "hand"))
                                        )
                                        .then(Commands.argument("namespace", StringArgumentType.string())
                                                .executes(context -> execute(context, "disable", StringArgumentType.getString(context, "namespace")))
                                        )
                                )
                                .then(Commands.literal("item")
                                        .then(Commands.literal("hand")
                                                .executes(context -> executeWithItem(context, "disable", null, true))
                                        )
                                        .then(Commands.argument("item", ItemArgument.item(buildContext))
                                                .executes(context -> executeWithItem(context, "disable", ItemArgument.getItem(context, "item").getItem(), false))
                                        )
                                )
                                .then(Commands.literal("all")
                                        .executes(context -> execute(context, "disable", "all"))
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
                        .then(Commands.literal("help")
                                .executes(context -> executeHelp(context)))
        );
    }


    //Method to handle namespaces
    private static int execute(CommandContext<CommandSourceStack> context, String action, String namespace) {
        CommandSourceStack source = context.getSource();
        Player player;

        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(Component.literal("You must be a player to use this command!"));
            return 0;
        }

        // Handle "disable all" explicitly
        if (action.equals("disable") && namespace.equals("all")) {
            try {
                RecipeManager.DisableAll(player.getStringUUID());
            } catch (IOException e) {
                logger.error("Disabling all items failed with error:", e);
                source.sendFailure(Component.literal("Disabling all items failed with error:" + e));
                return 0;
            }
            source.sendSuccess(() -> Component.literal("[ModDisable] All items disabled!"), false);
            return 1;
        }

        if (action.equals("enable") && namespace.equals("all")) {
            try {
                RecipeManager.EnableAll(player.getStringUUID());
            } catch (IOException e) {
                logger.error("Enabling all items failed with error:", e);
                source.sendFailure(Component.literal("[ModDisable] Enabling all items failed with error:" + e));
                return 0;
            }
            source.sendSuccess(() -> Component.literal("[ModDisable] All items enabled"), false);
            return 1;
        }

        // Continue with normal actionTarget logic
        String actionTarget = action + " namespace";

        switch (actionTarget) {
            case "enable namespace":
                try {
                    if (Objects.equals(namespace, "hand")) {
                        HeldItemManager.EnableHeldNamespace(player, source);
                    } else {
                        RecipeManager.EnableNamespace(player.getStringUUID(), namespace);
                    }
                    source.sendSuccess(() -> Component.literal("[ModDisable] Mod namespace enabled!"), false);
                    return 0;

                } catch (IOException e) {
                    logger.error("Enabling namespace failed.", e);
                    source.sendFailure(Component.literal("[ModDisable] Enabling namespace failed." + e));
                    return 0;
                }
                //break;

            case "disable namespace":
                logger.info("[Mod Disable]Namespace argument: {}", namespace);
                try {
                    if (Objects.equals(namespace, "hand")) {
                        HeldItemManager.DisableHeldNamespace(player, source);
                    } else {
                        RecipeManager.DisableNamespace(player.getStringUUID(), namespace);
                    }
                    source.sendSuccess(() -> Component.literal("[ModDisable] Mod namespace disabled!"), false);
                    return 0;

                } catch (IOException e) {
                    logger.error("Disabling namespace failed.", e);
                    source.sendFailure(Component.literal("[ModDisable] Disabling namespace failed." + e));
                    return 0;
                }

                //break;

            default:
                source.sendFailure(Component.literal("[ModDisable] Invalid action!"));
                return 0;
        }

        //return 1;
    }

    //Method to handle items
    private static int executeWithItem(CommandContext<CommandSourceStack> context, String action, Item item, boolean handFlag) {

        CommandSourceStack source = context.getSource();
        Player player;
        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(Component.literal("You must be a player to use this command!"));
            return 0;
        }

        if ((item == null && handFlag)) {
            switch (action + " " + "hand") {
                case "enable hand":
                    HeldItemManager.EnableHeldItem(player, source);
                    source.sendSuccess(() -> Component.literal("[ModDisable] Enabled held item successfully!"), false);
                    return 0;
                case "disable hand":
                    HeldItemManager.DisableHeldItem(player, source);
                    source.sendSuccess(() -> Component.literal("[ModDisable] Disabled held item successfully!"), false);
                    return 0;
            }
        } else {
            ItemStack itemStack = new ItemStack(item);
            String itemName = itemStack.getDisplayName().getString();
            switch (action + " " + "item") {
                case "enable item":
                    Registry<Item> registry = BuiltInRegistries.ITEM;
                    ResourceLocation key = registry.getKey(item);
                    String idStr = key.toString();

                    try {
                        RecipeManager.EnableItem(player.getStringUUID(), idStr);
                        source.sendSuccess(() -> Component.literal("[ModDisable] Mod item " + itemName + " enabled!"), false);
                        return 0;
                    } catch (IOException e) {
                        logger.error("Enabling item failed with exception.", e);
                        source.sendFailure(Component.literal("Enabling item failed with exception" + e));
                        return 0;
                    }


                    //break;
                case "disable item":
                    Registry<Item> registry1 = BuiltInRegistries.ITEM;
                    ResourceLocation key1 = registry1.getKey(item);
                    String idStr1 = key1.toString();

                    try {
                        RecipeManager.DisableItem(player.getStringUUID(), idStr1);
                        source.sendSuccess(() -> Component.literal("[ModDisable] Mod item " + itemName + " disabled!"), false);
                        return 0;
                    } catch (IOException e) {
                        logger.error("Disabling item failed with exception", e);
                        source.sendFailure(Component.literal("Disabling item failed with exception" + e));
                        return 0;
                    }
                    //break;
                default:
                    source.sendFailure(Component.literal("[ModDisable] Invalid action!"));
                    return 0;
            }
        }
        return 1;
    }

    private static int executeHelp(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        source.sendSuccess(() -> Component.literal("[ModDisable] Help\n" +
                "/disable_mod - Main command to interface with this mod\n" +
                "/disable_mod enable/disable item <item name goes here> - Enables/disables a specific item\n" +
                "/disable_mod enable/disable item hand - Disables the item in hand\n" +
                "/disable_mod enable/disable namespace <namespace goes here> - Disables all items within a namespace (for example, all items starting with minecraft: )\n" +
                "/disable_mod enable/disable namespace hand - Disables all items within the namespace of the currently held item\n" +
                "/disable_mod config DefaultDisabledItemsListFromPlayerUUID - generates a default disabled items list for a modpack, allowing large servers to not lag while searching for namespaces for new players. First, disable all namespaces you want to be disabled for new players for your player, then provide your UUID to this command.\n" +
                "/disable_mod init - Copies the default disabled items list to the player that initiated the command's UUID. Use this instead of manually disabling every mod namespace.\n" +
                "/disable_mod reinit - Reinits a corrupted disabled items list by doing the same as init. Note: This will delete the selected player's mod unlock progress!\n" +
                "/disable_mod disable/enable all - disables/enables all items currently registered in the item registry."), false);
        return 0;
    }
}
