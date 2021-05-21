package com.wynntils.core.framework.ui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class ImageSwitcherButton extends Button {

    private int index = 0;
    public final int texX;
    public final int texY;
    public final int width;
    public final int height;
    public final float scale;
    public final int count;
    public final int diffY;
    public final ResourceLocation resource;
    public final int texWidth;
    public final int texHeight;

    public ImageSwitcherButton(int x, int y, int texX, int texY, int width, int height, float scale, int count, int diffY, ResourceLocation resource, int texWidth, int texHeight, IPressable onPress, ITooltip onTooltip, ITextComponent name) {
        super(x, y, (int) (width * scale), (int) (height * scale), name, onPress, onTooltip);
        this.texX = texX;
        this.texY = texY;
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.count = count;
        this.diffY = diffY;
        this.resource = resource;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
    }

    @Override
    public void renderButton(MatrixStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_) {
        Minecraft.getInstance().getTextureManager().bind(resource);
        int x = this.texX + width * index;
        int y = this.texY + (this.isHovered() ? diffY : 0);

        p_230431_1_.pushPose();
        p_230431_1_.scale(scale, scale, scale);
        blit(p_230431_1_, (int) (this.x * (1 / scale)), (int) (this.y * (1 / scale)), x, y, width, height, texWidth, texHeight);
        p_230431_1_.popPose();
        if (this.isHovered()) {
            renderToolTip(p_230431_1_, p_230431_3_, p_230431_3_);
        }
    }

    public int index() {
        return this.index;
    }

    @Override
    public void onPress() {
        if (Screen.hasShiftDown()) {
            index--;
        } else {
            index++;
        }
        index %= count;
        if (index < 0) {
            index = count - 1;
        }
        super.onPress();
    }

}
