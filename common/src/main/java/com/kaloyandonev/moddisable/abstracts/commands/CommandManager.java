package com.kaloyandonev.moddisable.abstracts.commands;

import com.kaloyandonev.moddisable.helpers.HeldItemManager;
import com.kaloyandonev.moddisable.helpers.RecipeManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Objects;

import static com.kaloyandonev.moddisable.helpers.config.InitialStateDataHandler.executeConfigRequest;
import static com.kaloyandonev.moddisable.helpers.config.InitialStateDataHandler.executeReinitRequest;

public class CommandManager {

    private static final Logger logger = LogManager.getLogger(CommandManager.class);

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
                                .executes(CommandManager::executeHelp))
        );
    }


    //Method to handle namespaces
    private static int execute(CommandContext<CommandSourceStack> context, String action, String namespace) {
        CommandSourceStack source = context.getSource();
        Player player;
        MinecraftServer server = context.getSource().getServer();

        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(Component.literal("You must be a player to use this command!"));
            return 0;
        }

        // Handle "disable all" explicitly
        if (action.equals("disable") && namespace.equals("all")) {
            try {
                RecipeManager.DisableAll(player.getStringUUID(), server);
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
                RecipeManager.EnableAll(player.getStringUUID(), server);
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
                        HeldItemManager.EnableHeldNamespace(player, source, server);
                    } else {
                        RecipeManager.EnableNamespace(player.getStringUUID(), namespace, server);
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
                        HeldItemManager.DisableHeldNamespace(player, source, server);
                    } else {
                        RecipeManager.DisableNamespace(player.getStringUUID(), namespace, server);
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
        MinecraftServer server = context.getSource().getServer();
        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(Component.literal("You must be a player to use this command!"));
            return 0;
        }

        if ((item == null && handFlag)) {
            switch (action + " " + "hand") {
                case "enable hand":
                    HeldItemManager.EnableHeldItem(player, source, server);
                    source.sendSuccess(() -> Component.literal("[ModDisable] Enabled held item successfully!"), false);
                    return 0;
                case "disable hand":
                    HeldItemManager.DisableHeldItem(player, source, server);
                    source.sendSuccess(() -> Component.literal("[ModDisable] Disabled held item successfully!"), false);
                    return 0;
            }
        } else {
            assert item != null;
            ItemStack itemStack = new ItemStack(item);
            String itemName = itemStack.getDisplayName().getString();
            switch (action + " " + "item") {
                case "enable item":
                    Registry<Item> registry = BuiltInRegistries.ITEM;
                    ResourceLocation key = registry.getKey(item);
                    String idStr = key.toString();

                    try {
                        RecipeManager.EnableItem(player.getStringUUID(), idStr, server);
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
                        RecipeManager.DisableItem(player.getStringUUID(), idStr1, server);
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

        source.sendSuccess(() -> Component.literal("""
                [ModDisable] Help
                /disable_mod - Main command to interface with this mod
                /disable_mod enable/disable item <item name goes here> - Enables/disables a specific item
                /disable_mod enable/disable item hand - Disables the item in hand
                /disable_mod enable/disable namespace <namespace goes here> - Disables all items within a namespace (for example, all items starting with minecraft: )
                /disable_mod enable/disable namespace hand - Disables all items within the namespace of the currently held item
                /disable_mod config DefaultDisabledItemsListFromPlayerUUID - generates a default disabled items list for a modpack, allowing large servers to not lag while searching for namespaces for new players. First, disable all namespaces you want to be disabled for new players for your player, then provide your UUID to this command.
                /disable_mod init - Copies the default disabled items list to the player that initiated the command's UUID. Use this instead of manually disabling every mod namespace.
                /disable_mod reinit - Reinits a corrupted disabled items list by doing the same as init. Note: This will delete the selected player's mod unlock progress!
                /disable_mod disable/enable all - disables/enables all items currently registered in the item registry."""), false);
        return 0;
    }
}
