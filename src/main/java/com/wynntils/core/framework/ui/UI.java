///*
// *  * Copyright © Wynntils - 2018 - 2021.
// */
//
//package com.wynntils.core.framework.ui;
//
//import com.mojang.blaze3d.matrix.MatrixStack;
//import com.wynntils.McIf;
//import com.wynntils.core.framework.enums.MouseButton;
//import com.wynntils.core.framework.rendering.ScreenRenderer;
//import com.wynntils.core.framework.rendering.textures.Textures;
//import com.wynntils.core.framework.ui.elements.UIEClickZone;
//import com.wynntils.core.framework.ui.elements.UIEColorWheel;
//import com.wynntils.core.framework.ui.elements.UIEList;
//import com.wynntils.core.framework.ui.elements.UIETextBox;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.screen.Screen;
//import com.wynntils.transition.GlStateManager;
//import net.minecraft.util.text.ITextComponent;
//import net.minecraft.util.text.StringTextComponent;
//
//import java.io.IOException;
//import java.lang.reflect.Field;
//import java.util.ArrayList;
//import java.util.ConcurrentModificationException;
//import java.util.List;
//
//public abstract class UI extends Screen {
//    private ScreenRenderer screenRenderer = new ScreenRenderer();
//    protected long ticks = 0;
//    protected int screenWidth = 0, screenHeight = 0, mouseX = 0, mouseY = 0;
//    protected List<UIElement> UIElements = new ArrayList<>();
//
//    private boolean initiated = false;
//
//    protected UI() {
//        super(StringTextComponent.EMPTY);
//    }
//
//    @Override
//    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
//        if (!initiated) { initiated = true; onInit(); }
//        super.render(matrix, mouseX, mouseY, partialTicks);
//        this.mouseX = mouseX;
//        this.mouseY = mouseY;
//
//        ScreenRenderer.beginGL(0, 0);
//
//        screenWidth = ScreenRenderer.screen.getGuiScaledWidth();
//        screenHeight = ScreenRenderer.screen.getGuiScaledHeight();
//
//        onRenderPreUIE(screenRenderer);
//        for (UIElement uie : UIElements) {
//            uie.position.refresh(ScreenRenderer.screen);
//            if (!uie.visible) continue;
//            uie.render(mouseX, mouseY);
//        }
//
//        onRenderPostUIE(screenRenderer);
//
//        ScreenRenderer.endGL();
//    }
//
//    @Override public void updateScreen() {
//        ticks++; onTick();
//        for (UIElement uie : UIElements)
//            uie.tick(ticks);
//    }
//    @Override public void init() { if (!initiated) { initiated = true; onInit(); } onWindowUpdate(); }
//    @Override public void onGuiClosed() {onClose();}
//
//    @Override
//    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
//        super.mouseClicked(mouseX, mouseY, mouseButton);
//        try {
//            for (UIElement uie : UIElements)
//                if (uie instanceof UIEList) {
//                    List<UIElement> UIElements_old = this.UIElements;
//                    this.UIElements = ((UIEList) uie).elements;
//                    mouseClicked(mouseX, mouseY, mouseButton);
//                    this.UIElements = UIElements_old;
//                } else if (uie instanceof UIEClickZone)
//                    ((UIEClickZone) uie).click(mouseX, mouseY, mouseButton > 2 ? MouseButton.UNKNOWN : MouseButton.values()[mouseButton], this);
//        } catch (ConcurrentModificationException ignored) {}
//    }
//
//    @Override
//    protected void mouseReleased(int mouseX, int mouseY, int state) {
//        super.mouseReleased(mouseX, mouseY, state);
//        for (UIElement uie : UIElements) {
//            if (uie instanceof UIEList) {
//                List<UIElement> UIElements_old = this.UIElements;
//                this.UIElements = ((UIEList) uie).elements;
//                mouseReleased(mouseX, mouseY, state);
//                this.UIElements = UIElements_old;
//            } else if (uie instanceof UIEClickZone)
//                ((UIEClickZone) uie).release(mouseX, mouseY, state > 2 ? MouseButton.UNKNOWN : MouseButton.values()[state], this);
//        }
//    }
//
//    @Override
//    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double d1, double d2) {
//        for (UIElement uie : UIElements) {
//            if (uie instanceof UIEList) {
//                List<UIElement> UIElements_old = this.UIElements;
//                this.UIElements = ((UIEList) uie).elements;
//                mouseDragged(mouseX, mouseY, mouseButton, d1, d2);
//                this.UIElements = UIElements_old;
//            } else if (uie instanceof UIEClickZone)
//                ((UIEClickZone) uie).clickMove(mouseX, mouseY, mouseButton > 2 ? MouseButton.UNKNOWN : MouseButton.values()[clickedMouseButton], timeSinceLastClick, this);
//        }
//        return true;
//    }
//
//    @Override
//    protected void keyTyped(char typedChar, int keyCode) throws IOException {
//        super.keyTyped(typedChar, keyCode);
//        for (UIElement uie : UIElements) {
//            if (uie instanceof UIEList) {
//                List<UIElement> UIElements_old = this.UIElements;
//                this.UIElements = ((UIEList) uie).elements;
//                keyTyped(typedChar, keyCode);
//                this.UIElements = UIElements_old;
//            } else if (uie instanceof UIETextBox) {
//                ((UIETextBox) uie).keyTyped(typedChar, keyCode, this);
//            } else if (uie instanceof UIEColorWheel)
//                ((UIEColorWheel) uie).keyTyped(typedChar, keyCode, this);
//        }
//    }
//
//    // v  USE THESE INSTEAD OF GUISCREEN METHODS IF POSSIBLE  v \\
//    public abstract void onInit();
//    public abstract void onClose();
//    public abstract void onTick();
//    public abstract void onRenderPreUIE(ScreenRenderer render);
//    public abstract void onRenderPostUIE(ScreenRenderer render);
//    public abstract void onWindowUpdate();
//
//    @Override
//    protected void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {  // fix for alpha problems after doing default background
//        super.drawGradientRect(left, top, right, bottom, startColor, endColor);
//        GlStateManager.enableBlend();
//    }
//
//    public static void setupUI(UI ui) {
//        for (Field f : ui.getClass().getFields()) {
//            try {
//                UIElement uie = (UIElement) f.get(ui);
//                if (uie != null)
//                    ui.UIElements.add(uie);
//            } catch (Exception ignored) {}
//        }
//    }
//
//    public void show() {
//        setupUI(this);
//        McIf.mc().setScreen(this);
//    }
//
//    public static abstract class CommonUIFeatures {
//        static ScreenRenderer render = new ScreenRenderer();
//        public static void drawBook() {
//            int wh = ScreenRenderer.screen.getGuiScaledWidth()/2, hh = ScreenRenderer.screen.getGuiScaledHeight()/2;
//            render.drawRect(Textures.UIs.book, wh - 200, hh - 110, wh + 200, hh + 110, 0, 0, 400, 220);
//        }
//        public static void drawScrollArea() {
//            int wh = ScreenRenderer.screen.getGuiScaledWidth()/2, hh = ScreenRenderer.screen.getGuiScaledHeight()/2;
//            render.drawRect(Textures.UIs.book_scrollarea_settings, wh - 190, hh - 100, wh - 12, hh + 85, 0, 0, 178, 185);
//        }
//    }
//}
