/*
 * ModDisable
 * A Minecraft Mod to disable other Mods
 * Copyright (C) 2024 Kaloyan Ivanov Donev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

public class ToastNotifier {

    /**
     * Displays a system toast with a custom title and message.
     *
     * @param title   the title of the toast
     * @param message the message displayed in the toast
     */
    public static void showToast(String title, String message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null) {
            SystemToast.add(
                    minecraft.getToasts(),
                    SystemToast.SystemToastIds.TUTORIAL_HINT,
                    Component.literal(title),
                    Component.literal(message)
            );
        }
    }
}