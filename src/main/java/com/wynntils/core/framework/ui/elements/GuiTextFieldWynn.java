/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.framework.ui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;

import java.awt.Color;

public class GuiTextFieldWynn extends TextFieldWidget {

    private static final Color TEXT_FIELD_COLOR_1 = new Color(87, 65, 51);
    private static final Color TEXT_FIELD_COLOR_2 = new Color(120, 90, 71);

    public GuiTextFieldWynn(FontRenderer fontrendererObj, int x, int y, int width, int height, ITextComponent name) {
        super(fontrendererObj, x, y, width, height, name);

        this.setBordered(false);
    }

    @Override
    public void renderButton(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        fill(matrix, this.x - 2, this.y - 1, this.x + this.width - 1, this.y + this.height - 1, TEXT_FIELD_COLOR_1.getRGB());
        fill(matrix, this.x - 1, this.y, this.x + this.width - 2, this.y + this.height - 2, TEXT_FIELD_COLOR_2.getRGB());
        super.renderButton(matrix, mouseX, mouseY, partialTicks);
    }

}
