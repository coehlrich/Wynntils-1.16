/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.framework.ui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

/**
 * Fixes net.minecraft.client.gui.screen.GuiButtonImage and can also be scaled
 */
public class GuiButtonImageScale extends ImageButton {
    private float scaleFactor;

    public GuiButtonImageScale(int x, int y, int width, int height, int textureOffsetX, int textureOffsetY, int hoverImageYOffset, ResourceLocation resource, int textureWidth, int textureHeight, Button.IPressable onClick) {
        this(x, y, width, height, textureOffsetX, textureOffsetY, hoverImageYOffset, resource, textureWidth, textureHeight, onClick, StringTextComponent.EMPTY);
    }

    public GuiButtonImageScale(int x, int y, int width, int height, int textureOffsetX, int textureOffsetY, int hoverImageYOffset, ResourceLocation resource, int textureWidth, int textureHeight, Button.IPressable onClick, ITextComponent name) {
        this(x, y, width, height, textureOffsetX, textureOffsetY, hoverImageYOffset, resource, textureWidth, textureHeight, onClick, Button.NO_TOOLTIP, name);
    }

    public GuiButtonImageScale(int x, int y, int width, int height, int textureOffsetX, int textureOffsetY, int hoverImageYOffset, ResourceLocation resource, int textureWidth, int textureHeight, Button.IPressable onClick, Button.ITooltip tooltip, ITextComponent name) {
        super(x, y, width, height, textureOffsetX, textureOffsetY, hoverImageYOffset, resource, textureWidth, textureHeight, onClick, tooltip, name);
        setScaleFactor(1);
    }

    public GuiButtonImageScale setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
        return this;
    }

    @Override
    public void renderButton(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        if (scaleFactor != 1f) {
            matrix.pushPose();
            matrix.translate(x / 2, y / 2, 0);
            matrix.scale(scaleFactor, scaleFactor, scaleFactor);
        }
        super.renderButton(matrix, mouseX, mouseY, partialTicks);
        if (scaleFactor != 1f) {
            matrix.popPose();
        }
    }
}
