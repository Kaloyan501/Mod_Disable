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

package com.kaloyandonev.moddisable.events;

import com.kaloyandonev.moddisable.DisableModMain;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.kaloyandonev.moddisable.commands.Disable_Mod;
import net.minecraftforge.server.command.ConfigCommand;

@Mod.EventBusSubscriber(modid = DisableModMain.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event){
        Disable_Mod.registerCommand(event.getDispatcher(), event.getBuildContext());

        ConfigCommand.register(event.getDispatcher());
    }
}
