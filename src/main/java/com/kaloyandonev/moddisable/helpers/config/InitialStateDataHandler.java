package com.kaloyandonev.moddisable.helpers.config;

import com.kaloyandonev.moddisable.helpers.RecipeDisabler;
import com.kaloyandonev.moddisable.helpers.ServerCheckHelper;
import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.StaticPathStorage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class InitialStateDataHandler {

    static final Logger logger = LogManager.getLogger();

    public static int executeConfigRequest(CommandContext<CommandSourceStack> context, String action, String argument) {


        CommandSourceStack source = context.getSource();


        File GeneralConfigFolder = new File(Minecraft.getInstance().gameDirectory, "config/ModDisable");

        if (!GeneralConfigFolder.exists()) {
            logger.info("[Mod Disable] Creating config folder.");
            GeneralConfigFolder.mkdir();
        }



        switch (action) {
            case "DefaultDisabledItemsListFromPlayerUUID":
                File SourceFile = new File(StaticPathStorage.getSubWorldFolderFile(), argument + ".json");
                File DestionationFile = new File(GeneralConfigFolder, "DefaultDisabledItemsList.json");

                if (!SourceFile.exists()) {
                    source.sendFailure(Component.literal("Disabled items list does not exist for the provided UUID. Please provide only the UUID, including the - (Example: 550e8400-e29b-41d4-a716-446655440000 NOT 550e8400-e29b-41d4-a716-446655440000.json or 550e8400e29b41d4a716446655440000. You can find all player UUIDs in your world's folder under Mod_Disable_Data. Additional info: Player file path: " + SourceFile.toPath()));
                } else {
                    try {
                        copyFile(SourceFile, DestionationFile);
                        source.sendSuccess(() -> Component.literal("[Mod Disable] Default disabled items list set successfully."), false);
                    } catch (IOException e) {
                        source.sendFailure(Component.literal("[Mod Disable] executeConfigRequest threw an IOException. Returning a non-zero state. Exception: " + e));
                        logger.error("[Mod Disable] executeConfigRequest threw an IOException. Returning a non-zero state.");
                        logger.error(e);
                        return 1;
                    }
                }
            return 0;

            case "Init":
                Player player;

                try {
                    player = source.getPlayerOrException();
                    File PlayerDisabledItemsFile = new File(StaticPathStorage.getSubWorldFolderFile(), player.getUUID().toString() + ".json");
                    File DefaultDisabledItemsList = new File(GeneralConfigFolder, "DefaultDisabledItemsList.json");

                    if (!PlayerDisabledItemsFile.exists()) {
                        try {
                            copyFile(DefaultDisabledItemsList, PlayerDisabledItemsFile);
                            source.sendSuccess(() -> Component.literal("[Mod Disable] Default disabled items list copied to your player disabled items list location."), false);
                            RecipeDisabler.enableAllRecipes(source.getServer());
                            RecipeDisabler.queueRecipeRemovalFromJson(PlayerDisabledItemsFile.toString());
                            source.sendSuccess(() -> Component.literal("[Mod Disable] Reload of disabled recipes started for your player."), false);
                        } catch (IOException e) {
                            source.sendFailure(Component.literal("[Mod Disable] An exception occurred while trying to copy the default disabled items list to your player disabled items list. Exception is: " + e));
                            logger.error("[Mod Disable] An exception occurred while trying to copy the default disabled items list to the player disabled items list for player with UUID {} Exception is: {}", player.getUUID(), e);
                        }

                    } else {
                        if (!ServerCheckHelper.isConnectedToDedicatedServer()) {
                            source.sendFailure(Component.literal("Your player's default disabled items list is already initialized. This is not an error. If you think your disabled items list is corrupted, please run /disable_mod config reinit " + player.getUUID()));
                        } else {
                            source.sendFailure(Component.literal("Your player's default disabled items list is already initialized. This is not an error. If you think your file is corrupted and are trying to reinitialize it, please contact this message to your server administrator and tell them to run the /disable_mod config reinit command with your UUID, which is " + player.getUUID()));
                        }
                    }

                } catch (CommandSyntaxException e) {
                    source.sendFailure(Component.literal("[Mod Disable] You must be a player to run this command!"));
                }
        }

        return 1;
    }

    public static int executeReinitRequest(CommandContext<CommandSourceStack> context, String action, String argument, String Confirm) {

        File GeneralConfigFolder = new File(Minecraft.getInstance().gameDirectory, "config/ModDisable");

        CommandSourceStack source = context.getSource();

        switch(action){
            case "Reinit":
                if ("nonconfirm".equals(Confirm)) {
                    source.sendFailure(Component.literal("[Mod Disable] WARNING! This will delete the mod unlock progress of the player you selected! If you are sure you want to do this, please type the command: /disable_mod config reinit " + argument + " confirm"));
                }else if ("confirm".equals(Confirm)){
                    File PlayerDisabledItemsFile = new File(StaticPathStorage.getSubWorldFolderFile(), argument + ".json");
                    File DefaultDisabledItemsList = new File(GeneralConfigFolder, "DefaultDisabledItemsList.json");

                    if (PlayerDisabledItemsFile.exists()) {
                        PlayerDisabledItemsFile.delete();
                        try {
                            copyFile(DefaultDisabledItemsList, PlayerDisabledItemsFile);
                            RecipeDisabler.enableAllRecipes(source.getServer());
                            RecipeDisabler.queueRecipeRemovalFromJson(PlayerDisabledItemsFile.toString());
                            source.sendSuccess(() -> Component.literal("[Mod Disable] Reinit done."), false);
                        } catch (IOException e) {
                            source.sendFailure(Component.literal("[Mod Disable] An exception was thrown while copying the disabled list file. Exception is: " + e));
                        }
                    } else {
                        source.sendFailure(Component.literal("[Mod Disable] Disabled items list for UUID " + argument + " does not exist."));
                    }
                }
        }

        return 1;
    }

    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        // Ensure the parent directory of the target file exists
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs(); // Create directories if needed
        }
        // Perform the copy operation
        Files.copy(sourceFile.toPath(), targetFile.toPath());
    }

}