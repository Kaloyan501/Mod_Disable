package com.kaloyandonev.moddisable.events;

import com.kaloyandonev.moddisable.abstracts.commands.CommandManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class ModEvents {

    public static MinecraftServer SERVER;

    public static void onCommandRegister(){
        ServerLifecycleEvents.SERVER_STARTED.register(server -> SERVER = server);
        CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandBuildContext, commandSelection) -> {
            CommandManager.registerCommand(commandDispatcher, commandBuildContext, SERVER);
        }));
    }
}
