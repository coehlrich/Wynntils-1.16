/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.map.overlays.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynntils.McIf;
import com.wynntils.core.framework.enums.MouseButton;
import com.wynntils.core.framework.rendering.ScreenRenderer;
import com.wynntils.core.framework.ui.elements.UIEColorWheel;
import com.wynntils.core.utils.Utils;
import com.wynntils.modules.map.MapModule;
import com.wynntils.modules.map.configs.MapConfig;
import com.wynntils.modules.map.instances.MapProfile;
import com.wynntils.modules.map.instances.PathWaypointProfile;
import com.wynntils.modules.map.instances.PathWaypointProfile.PathPoint;
import com.wynntils.modules.map.overlays.objects.MapPathWaypointIcon;
import com.wynntils.modules.map.overlays.objects.WorldMapIcon;
import com.wynntils.transition.GlStateManager;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.GuiLabel;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.function.Consumer;

public class PathWaypointCreationUI extends WorldMapUI {

    private GuiLabel nameFieldLabel;
    private TextFieldWidget nameField;
    private CheckboxButton hiddenBox;
    private CheckboxButton circularBox;

    private GuiLabel helpText;
    private CheckboxButton addToFirst;
    private CheckboxButton showIconsBox;

    private UIEColorWheel colorWheel;

    private PathWaypointProfile originalProfile;
    private PathWaypointProfile profile;
    private MapPathWaypointIcon icon;
    private WorldMapIcon wmIcon;

    private boolean hidden;

    public PathWaypointCreationUI() {
        this(null);
    }

    public PathWaypointCreationUI(PathWaypointProfile profile) {
        super();
        removeWorkingProfile();

        this.allowMovement = false;

        this.profile = new PathWaypointProfile(originalProfile = profile);
        icon = new MapPathWaypointIcon(this.profile);
        wmIcon = new WorldMapIcon(icon);
        hidden = !this.profile.isEnabled;
        this.profile.isEnabled = true;

        if (originalProfile != null && originalProfile.size() > 0) {
            updateCenterPosition(originalProfile.getX(), originalProfile.getZ());
        }
    }

    @Override
    public void init() {
        super.init();

        addButton(new Button(22, 23, 60, 18, new StringTextComponent("Save"), button -> {
            profile.isEnabled = !hiddenBox.selected();
            setCircular();
            profile.name = nameField.getValue();
            if (originalProfile != null) {
                MapConfig.Waypoints.INSTANCE.pathWaypoints.set(MapConfig.Waypoints.INSTANCE.pathWaypoints.indexOf(originalProfile), profile);
            } else {
                MapConfig.Waypoints.INSTANCE.pathWaypoints.add(profile);
            }
            MapConfig.Waypoints.INSTANCE.saveSettings(MapModule.getModule());
            McIf.mc().setScreen(new PathWaypointOverwiewUI());
        }));
        addButton(new Button(22, 46, 60, 18, new StringTextComponent("Cancel"), button -> {
            McIf.mc().setScreen(new PathWaypointOverwiewUI());
        }));
        addButton(new Button(22, 69, 60, 18, new StringTextComponent("Reset"), button -> {
            McIf.mc().setScreen(new PathWaypointCreationUI(originalProfile));
        }));
        addButton(new Button(22, 92, 60, 18, new StringTextComponent("Clear"), button -> {
            int sz;
            while ((sz = profile.size()) != 0)
                profile.removePoint(sz - 1);
            onChange();
        }));

        boolean returning = nameField != null;
        String name = returning ? nameField.getValue() : profile.name;

        nameField = new TextFieldWidget(McIf.mc().font, this.width - 183, 23, 160, 20, new StringTextComponent("Name"));
        nameField.setValue(name);
        nameFieldLabel = new GuiLabel(McIf.mc().font, 0, this.width - 218, 30, 40, 10, 0xFFFFFF);
        nameFieldLabel.addLine("Name");

        if (!returning) {
            colorWheel = new UIEColorWheel(1, 0, -168, 46, 20, 20, true, profile::setColor, this);
            colorWheel.setColor(profile.getColor());
        }

        hiddenBox = addButton(new CheckboxButton(this.width - 143, 72, 11, 11, new StringTextComponent("Hidden"), hidden)); // TODO: check align
        circularBox = addButton(new CheckboxButton(this.width - 83, 72, 11, 11, new StringTextComponent("Circular"), profile.isCircular));

        helpText = new GuiLabel(McIf.mc().font, 1, 22, this.height - 36, 120, 10, 0xFFFFFF);
        helpText.addLine("Shift + drag to pan");
        helpText.addLine("Right click to remove points");

        addToFirst = addButton(new CheckboxButton(this.width - 100, this.height - 47, 11, 11, new StringTextComponent("Add to start"), false));
        showIconsBox = addButton(new CheckboxButton(this.width - 100, this.height - 34, 11, 11, new StringTextComponent("Show icons"), true));

    }

    @Override
    protected void forEachIcon(Consumer<WorldMapIcon> c) {
        super.forEachIcon(c);
        if (wmIcon != null) c.accept(wmIcon);
    }

    @Override
    protected void createIcons() {
        super.createIcons();
        removeWorkingProfile();
    }

    private void removeWorkingProfile() {
        // Remove the icon for the current path being created / edited, as it is handled separately
        if (profile == null) return;

        icons.removeIf(c -> c.getInfo() instanceof MapPathWaypointIcon && ((MapPathWaypointIcon) c.getInfo()).getProfile() == originalProfile);
    }

    private void setCircular() {
        if (circularBox.selected() != profile.isCircular) {
            profile.isCircular = circularBox.selected();
            onChange();
        }
    }

    private void onChange() {
        icon.profileChanged();
        resetIcon(wmIcon);
    }

    private void removeClosePoints(int worldX, int worldZ) {
        float scaleFactor = getScaleFactor();
        if (profile.size() != 0) {
            // On right click remove all close points
            boolean changed = false;
            while (profile.size() != 0) {
                PathPoint last = profile.getPoint(profile.size() - 1);
                int dx = worldX - last.getX();
                int dz = worldZ - last.getZ();
                int dist_sq = dx * dx + dz * dz;
                if (scaleFactor * dist_sq <= 100) {
                    profile.removePoint(profile.size() - 1);
                    changed = true;
                } else {
                    break;
                }
            }

            while (profile.size() != 0) {
                PathPoint first = profile.getPoint(0);
                int dx = worldX - first.getX();
                int dz = worldZ - first.getZ();
                int dist_sq = dx * dx + dz * dz;
                if (scaleFactor * dist_sq <= 100) {
                    profile.removePoint(0);
                    changed = true;
                } else {
                    break;
                }
            }

            if (changed) onChange();
        }
    }

    private boolean handleMouse(int mouseX, int mouseY, int mouseButton) {
        if (hasShiftDown() || nameField.isFocused())
            return false;

        for (IGuiEventListener button : this.children) {
            if (button.isMouseOver(mouseX, mouseY)) {
                return false;
            }
        }
        if (colorWheel.isHovering()) return false;
        if (mouseX >= nameField.x && mouseX < nameField.x + nameField.getWidth() && mouseY >= nameField.y && mouseY < nameField.y + nameField.getHeight())
            return false;

        if (mouseButton == 0) {
            // Add points on left click
            MapProfile map = MapModule.getModule().getMainMap();
            int worldX = getMouseWorldX(mouseX, map);
            int worldZ = getMouseWorldZ(mouseY, map);

            if (profile.size() == 0) {
                profile.addPoint(new PathPoint(worldX, worldZ));
                onChange();
                return true;
            } else if (addToFirst.selected()) {
                PathPoint first = profile.getPoint(profile.size() - 1);
                int dx = worldX - first.getX();
                int dz = worldZ - first.getZ();
                int dist_sq = dx * dx + dz * dz;
                if (4 < dist_sq) {
                    profile.insertPoint(0, new PathPoint(worldX, worldZ));
                    onChange();
                    return true;
                }
            } else {
                PathPoint last = profile.getPoint(profile.size() - 1);
                int dx = worldX - last.getX();
                int dz = worldZ - last.getZ();
                int dist_sq = dx * dx + dz * dz;
                if (4 < dist_sq) {
                    profile.addPoint(new PathPoint(worldX, worldZ));
                    onChange();
                    return true;
                }
            }
        } else if (mouseButton == 1) {
            // Remove points close to right click
            MapProfile map = MapModule.getModule().getMainMap();
            int worldX = getMouseWorldX(mouseX, map);
            int worldZ = getMouseWorldZ(mouseY, map);

            removeClosePoints(worldX, worldZ);
            return true;
        }
        return false;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (handleMouse(mouseX, mouseY, mouseButton)) return;

        nameField.mouseClicked(mouseX, mouseY, mouseButton);
        MouseButton button = mouseButton == 0 ? MouseButton.LEFT : mouseButton == 1 ? MouseButton.RIGHT : mouseButton == 2 ? MouseButton.MIDDLE : MouseButton.UNKNOWN;
        colorWheel.click(mouseX, mouseY, button, null);

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double d1, double d2) {
        if (handleMouse((int) mouseX, (int) mouseY, mouseButton)) return true;

        super.mouseDragged(mouseX, mouseY, mouseButton, d1, d2);
        return true;
    }

    @Override
    public void tick() {
        colorWheel.tick(0);
        super.tick();
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            Utils.tab(
                    hasShiftDown() ? -1 : +1,
                nameField, colorWheel.textBox.textField
            );
            return true;
        }
        colorWheel.keyTyped(typedChar, keyCode, null);
        nameField.charTyped(typedChar, keyCode);
        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        boolean isShiftKeyDown = hasShiftDown();

        updatePosition(mouseX, mouseY, !nameField.isFocused() && isShiftKeyDown && clicking[0] && !clicking[1]);
        if (isShiftKeyDown && clicking[1]) {
            updateCenterPositionWithPlayerPosition();
        }

        hidden = hiddenBox.selected();
        setCircular();

        ScreenRenderer.beginGL(0, 0);

        drawMap(mouseX, mouseY, partialTicks);

        if (showIconsBox.isChecked()) {
            drawIcons(mouseX, mouseY, partialTicks);
        } else {
            createMask();
            GlStateManager.enableBlend();
            wmIcon.drawScreen(mouseX, mouseY, partialTicks, getScaleFactor(), renderer);
            clearMask();
        }

        drawCoordinates(mouseX, mouseY, partialTicks);

        colorWheel.position.refresh();
        colorWheel.render(mouseX, mouseY);

        ScreenRenderer.endGL();


        if (nameField != null) nameField.drawTextBox();

        nameFieldLabel.drawLabel(McIf.mc(), mouseX, mouseY);
        helpText.drawLabel(McIf.mc(), mouseX, mouseY);

        super.render(matrix, mouseX, mouseY, partialTicks);
    }
}
