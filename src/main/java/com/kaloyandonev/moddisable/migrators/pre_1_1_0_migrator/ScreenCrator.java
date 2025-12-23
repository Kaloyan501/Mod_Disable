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


package com.kaloyandonev.moddisable.migrators.pre_1_1_0_migrator;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.network.chat.TextColor;


@OnlyIn(Dist.CLIENT)
public class ScreenCrator extends Screen {
    private final Component description;
    private MultiLineLabel message = MultiLineLabel.EMPTY;
    protected final ScreenCrator.MigratorListener listener;

    public ScreenCrator(Component pTitle, Component pDescription, MigratorListener pListener){
        super(pTitle);
        this.description = pDescription;
        this.listener = pListener;
    }

    @Override
    protected void init(){
        super.init();
        this.message = MultiLineLabel.create(this.font, this.description, this.width - 50);
        int i = (this.message.getLineCount() + 1) * 9;

        this.addRenderableWidget(Button.builder(Component.literal("Migrate Mod Disable data to specific world"), (button) -> {
            this.listener.Migraton_pre_1_1_0_proceed(true);
            this.minecraft.execute(() -> this.minecraft.setScreen(new SelectWorldScreen(this)));
        }).bounds(this.width / 2 - 235, 100 + i, 230, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Cancel (Migrate manually later)"), (button) -> {
            this.listener.Migraton_pre_1_1_0_proceed(false);
            this.minecraft.execute(() -> this.minecraft.setScreen(new TitleScreen()));
        }).bounds(this.width / 2 - 235 + 235, 100 + i, 230, 20).build());
    }


    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.message.renderCentered(pGuiGraphics, this.width / 2 , 70);
        pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 50, 15744024);

    }

    @OnlyIn(Dist.CLIENT)
    public interface MigratorListener {
        boolean Migraton_pre_1_1_0_proceed(boolean pConfirmed);
    }
}
