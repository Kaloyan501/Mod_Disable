package com.kaloyandonev.moddisable;

import com.kaloyandonev.moddisable.abstracts.ConfDir;
import com.kaloyandonev.moddisable.events.ModEvents;
import com.kaloyandonev.moddisable.helpers.*;
import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.ClientTickHandler;
import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.ClientWorldFolderFinder;
import com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator.StaticPathStorage;
import com.kaloyandonev.moddisable.provideloaderspecific.ConfigPathProviderFabric;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class FabricMain implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(Constants.MOD_ID);
    //private final isSinglePlayer isSinglePlayer = new isSinglePlayer();

    @Override
    public void onInitialize() {
        
        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
        UseDetector.register();
        Constants.LOG.info("Hello Fabric world!");
        CommonClass.init();
        ModEvents.onCommandRegister();
        ServerLifecycleEvents.SERVER_STARTED.register(SererModEvents::onServerStarted);
        ServerLifecycleEvents.SERVER_STARTED.register(SererModEvents::onServerStartedDedicated);
        //ClientPlayConnectionEvents.JOIN.register(this::onLoadComplete);
        ConfigPathProviderFabric configPathProviderFabric = new ConfigPathProviderFabric();
        ConfDir.init(() -> configPathProviderFabric.getConfigDir());

        /*
        ServerCheckHelper.init(() ->
                FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER
        );
         */
    }
    /*
    //TO DO: ADD LISTENER!
    private void onLoadComplete(
                                PacketSender sender,
                                Minecraft client){
        isSinglePlayer isSinglePlayer = new isSinglePlayer();
        isSinglePlayer.checkisSinglePlayer();
    }
    */
    public static class SererModEvents{
        public static void onServerStarted(MinecraftServer server){
            //  Do something when the server starts
            LOGGER.warn("HELLO from server started! (Not to be confused with starting!)");

            ServerLevel world = server.getLevel(Level.OVERWORLD);

            //if (!server.isDedicatedServer()) {
                ClientWorldFolderFinder folderFinder = new ClientWorldFolderFinder();
                Path subWorldFolderPath = folderFinder.getWorldSubfolderPath(world);

                StaticPathStorage.setSubWorldFolderPath(subWorldFolderPath);
                LOGGER.info("subWorldFolderPath is {}", subWorldFolderPath);

                //ClientTickHandler clientTickHandler = new ClientTickHandler();
                //MigrateTask migrateTask = new MigrateTask();
                //migrateTask.performMigration(clientTickHandler);
            //}

            LOGGER.debug("[Mod Disable] MigrateTask is about to run!");
            processAllDisabledItemsFromJson.processAllDisabledItemsFromJson();

            boolean CheckSumInvalid = JsonHelper.defaultDisabledListChecksumManger();
            if (CheckSumInvalid){
                try {
                    Path WorldFolderPath = PathHelper.getFullWorldPath(server);
                    Path Mod_Disable_DataPath = WorldFolderPath.resolve("Mod_Disable_Data");

                    List<String> fileNames = Files.list(Mod_Disable_DataPath).map(p -> p.getFileName().toString()).collect(Collectors.toList());
                    String[] fileNamesArray = fileNames.toArray(new String[0]);

                    for (String fileName : fileNamesArray) {
                        PlayerItemHashmapper.PlayerItemHashmapper(Mod_Disable_DataPath.resolve(fileName).toFile());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //TO DO: Make this only execude on dedicated
        public static void onServerStartedDedicated(MinecraftServer server){
            ServerLevel world = server.getLevel(Level.OVERWORLD);

            ClientWorldFolderFinder folderFinder = new ClientWorldFolderFinder();
            Path subWorldFolderPath = folderFinder.getWorldSubfolderPath(world);
            LOGGER.info("subWorldFolderPath is {}", subWorldFolderPath);

            StaticPathStorage.setSubWorldFolderPath(subWorldFolderPath);

            processAllDisabledItemsFromJson.processAllDisabledItemsFromJson();
        }
    }

    public static class ClientModEvents
    {
        public static void onClientSetup()
        {

        }
    }

}
