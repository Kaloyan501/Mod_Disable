//ModDisable
//A Minecraft Mod to disable other Mods
//Copyright (C) 2024 Kaloyan Ivanov Donev

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
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.kaloyandonev.moddisable.helpers.JsonHelper;
import com.kaloyandonev.moddisable.helpers.RecipeDisabler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

import static com.kaloyandonev.moddisable.helpers.config.InitialStateDataHandler.executeConfigRequest;
import static com.kaloyandonev.moddisable.helpers.config.InitialStateDataHandler.executeReinitRequest;


public class Disable_Mod {

    public static boolean IsDebugEnabled = false;
    public static File DATA_DIR = null;



    private static final Logger logger = LogManager.getLogger(Disable_Mod.class);
    private isSinglePlayer isSinglePlayer = new isSinglePlayer();

    public Disable_Mod(){

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::ClientCode);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onLoadComplete);




        MinecraftForge.EVENT_BUS.register(this);
    }

    public void onLoadComplete(FMLLoadCompleteEvent event){
        isSinglePlayer.checkisSinglePlayer(event);
    }

    public static void ResetDataDir(){
        DATA_DIR = null;
    }

    @OnlyIn(Dist.CLIENT)
    private void ClientCode(final FMLCommonSetupEvent event) {
        logger.warn("ClientCode is about to run!");
        Minecraft.getInstance().execute(() -> {
            // Create your screenCrator instance
            ScreenCrator screenCrator = new ScreenCrator(
                    Component.literal("Migration Title"), // Title of the screen
                    Component.literal("Description of the screen"), // Description
                    confirmed -> {return false;}
            );

            // Open the new screen
            Minecraft.getInstance().setScreen(screenCrator);
        });
    }


    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(
                Commands.literal("disable_mod")
                        // 'enable' command
                        .then(Commands.literal("enable")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.literal("namespace")
                                        .then(Commands.argument("namespace", StringArgumentType.string())
                                                .executes(context -> execute(context, "enable", "namespace", StringArgumentType.getString(context, "namespace")))
                                        )
                                )
                                .then(Commands.literal("item")
                                        .then(Commands.argument("item", ItemArgument.item(buildContext))
                                                .executes(context -> executeWithItem(context, "enable", "item", ItemArgument.getItem(context, "item").getItem()))
                                        )
                                )
                        )
                        // 'disable' command
                        .then(Commands.literal("disable")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.literal("namespace")
                                        .then(Commands.argument("namespace", StringArgumentType.string())
                                                .executes(context -> execute(context, "disable", "namespace", StringArgumentType.getString(context, "namespace")))
                                        )
                                )
                                .then(Commands.literal("item")
                                        .then(Commands.argument("item", ItemArgument.item(buildContext))
                                                .executes(context -> executeWithItem(context, "disable", "item", ItemArgument.getItem(context, "item").getItem()))
                                        )
                                )
                        )
                        // 'config' command
                        .then(Commands.literal("config")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.literal("DefaultDisabledItemsListFromPlayerUUID")
                                        .then(Commands.argument("player", StringArgumentType.string())
                                                .executes(context -> executeConfigRequest(context, "DefaultDisabledItemsListFromPlayerUUID", StringArgumentType.getString(context, "player")))
                                        )
                                )
                                .then(Commands.literal("init")
                                        .executes(context -> executeConfigRequest(context, "Init", "Null"))
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


    // Method to get the data directory with lazy initialization
    private static File getDataDir() {
        DATA_DIR = StaticPathStorage.getSubWorldFolderFile();

        // Ensure the directory exists
        if (!DATA_DIR.exists()) {
            DATA_DIR.mkdirs(); // Create the directory if it does not exist
        }
        return DATA_DIR;
    }

    //Method to handle per-modpack config

    //Method to handle namespaces
    private static int execute(CommandContext<CommandSourceStack> context, String action, String target, String namespace){
        CommandSourceStack source = context.getSource();
        String actionTarget = action + " " + target;

        Player player;

        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(Component.literal("You must be a player to use this command!"));
            return 0;
        }

        String PlayerFile = new File(getDataDir(), player.getUUID().toString() + ".json").toString();

        if (IsDebugEnabled = true) {
            //source.sendSuccess(() -> Component.literal("[ModDisable] [DEBUG] PlayerFile var is: " + PlayerFile), true);
        }

        switch (actionTarget) {
            case "enable namespace":

                RecipeDisabler.removeItemsByNamespace(namespace, player);
                RecipeDisabler.enableAllRecipes(source.getServer());
                RecipeDisabler.queueRecipeRemovalFromJson(PlayerFile);
                source.sendSuccess(() -> Component.literal("[ModDisable] Mod namespace enabled!"), false);
                break;
            case "disable namespace":
                logger.info("[Mod Disable]Namespace argument: {}", namespace);
                RecipeDisabler.disableRecipesByNamespace(namespace, context.getSource().getServer(), context.getSource().getPlayer());
                source.sendSuccess(() -> Component.literal("[ModDisable] Mod namespace disabled!"), false);
                break;
            default:
                source.sendFailure(Component.literal("[ModDisable] Invalid action!"));
                return 0;
        }
        return 1;
    }
    //Method to handle items
    private static int executeWithItem(CommandContext<CommandSourceStack> context, String action, String target, Item item){
        CommandSourceStack source = context.getSource();
        ItemStack itemStack = new ItemStack(item);
        Item Item = ItemArgument.getItem(context, "item").getItem();
        String itemName = itemStack.getDisplayName().getString();
        String actionTarget = action + " " + target + " " + itemName;
        //source.sendSuccess(() -> Component.literal("[ModDisable] [Debug] actionTarget is " + actionTarget), false);
        Player player;


        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(Component.literal("You must be a player to use this command!"));
            return 0;
        }

        String PlayerFile = new File(getDataDir(), player.getUUID().toString() + ".json").toString();

        if (IsDebugEnabled = true) {
            //source.sendSuccess(() -> Component.literal("[ModDisable] [DEBUG] PlayerFile var is: " + PlayerFile), true);
        }


        switch (action + " " + target) {
            case "enable item":

                JsonHelper.enableItem(item, player);
                source.sendSuccess(() -> Component.literal("[ModDisable] Mod item " + itemName + " enabled!"), false);
                if (IsDebugEnabled) {
                    //source.sendSuccess(() -> Component.literal("[ModDisable] [DEBUG] RecipeDisabler is about to run!"), false);
                }
                RecipeDisabler.enableAllRecipes(source.getServer());
                RecipeDisabler.queueRecipeRemovalFromJson(PlayerFile);
                break;
            case "disable item":

                JsonHelper.disableItem(item, player);
                source.sendSuccess(() -> Component.literal("[ModDisable] Mod item " + itemName + " disabled!"), false);
                RecipeDisabler.queueRecipeRemovalFromJson(PlayerFile);
                break;
            default:
                source.sendFailure(Component.literal("[ModDisable] Invalid action!"));
                return 0;
        }
        return 1;
    }

    //Remove this in production version
    private static int executeDebug(CommandContext<CommandSourceStack> context, String action, String target) {
        CommandSourceStack source = context.getSource();
        String actionTarget = action + " " + target;
        Player player;

        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(Component.literal("[ModDisable] You must be a player to run this command!"));
        }

        if (IsDebugEnabled = true){
            source.sendSuccess(() -> Component.literal("[ModDisable] [DEBUG] A debug command was just ran, the debug boolean should be set to false and the debug command registrations should be removed in the production version!"), false);
            switch (actionTarget){
                case "debug debug_disable_recipe_stick":
                   RecipeDisabler.queueRecipeRemoval("minecraft:stick");
                   break;
                case "debug enable_recipe_stick":
                    RecipeDisabler.enableRecipe("minecraft:stick", source.getServer());
                    break;
                default:
                    source.sendFailure(Component.literal("[ModDisable] [DEBUG] Look at line 165."));
                    break;
            }
        } else {
            return 1;
        }
        return 1;
    }
}
