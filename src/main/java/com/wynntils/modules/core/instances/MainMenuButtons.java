/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.instances;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.wynntils.McIf;
import com.wynntils.Reference;
import com.wynntils.core.framework.rendering.textures.Textures;
import com.wynntils.core.utils.ServerUtils;
import com.wynntils.modules.core.config.CoreDBConfig;
import com.wynntils.modules.core.overlays.UpdateOverlay;
import com.wynntils.modules.core.overlays.ui.UpdateAvailableScreen;
import com.wynntils.modules.utilities.instances.ServerIcon;
import com.wynntils.webapi.WebManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;

public class MainMenuButtons {

    private static ServerList serverList = null;

    private static WynncraftButton lastButton = null;

    private static boolean alreadyLoaded = false;

    public static void addButtons(GuiScreenEvent.InitGuiEvent e, boolean resize) {
        if (!CoreDBConfig.INSTANCE.addMainMenuButton) return;

        if (lastButton == null || !resize) {
            ServerData s = getWynncraftServerData();
//            FMLClientHandler.instance().setupServerList();

            lastButton = new WynncraftButton(s, e.getGui().width / 2 + 104, e.getGui().height / 4 + 48 + 24, button -> {
                clickedWynncraftButton(((WynncraftButton) button).serverIcon.getServer(), e.getGui());
            });
            WebManager.checkForUpdates();
            UpdateOverlay.reset();

            e.addWidget(lastButton);

            // little pling when finished loading
            if (!alreadyLoaded) {
                McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.NOTE_BLOCK_PLING, 1f));
                alreadyLoaded = true;
            }
            return;
        }

        lastButton.x = e.getGui().width / 2 + 104;
        lastButton.y = e.getGui().height / 4 + 48 + 24;
        e.addWidget(lastButton);
    }

    private static void clickedWynncraftButton(ServerData server, Screen backGui) {
        if (hasUpdate()) {
            McIf.mc().setScreen(new UpdateAvailableScreen(server));
        } else {
            WebManager.skipJoinUpdate();
            ServerUtils.connect(backGui, server);
        }
    }

    private static boolean hasUpdate() {
        return !Reference.developmentEnvironment && WebManager.getUpdate() != null && WebManager.getUpdate().hasUpdate();
    }

    private static ServerData getWynncraftServerData() {
        return ServerUtils.getWynncraftServerData(serverList = new ServerList(McIf.mc()), true);
    }

    private static class WynncraftButton extends Button {

        private ServerIcon serverIcon;

        WynncraftButton(ServerData server, int x, int y, IPressable onClick) {
            super(x, y, 20, 20, StringTextComponent.EMPTY, onClick);

            serverIcon = new ServerIcon(server, true);
            serverIcon.onDone(r -> serverList.save());
        }

        @Override
        public void renderButton(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
            if (!visible) return;

            super.renderButton(matrix, mouseX, mouseY, partialTicks);

            ServerIcon.ping();
            ResourceLocation icon = serverIcon.getServerIcon();
            if (icon == null) icon = ServerIcon.UNKNOWN_SERVER;
            Minecraft.getInstance().getTextureManager().bind(icon);

            boolean hasUpdate = hasUpdate();

            matrix.pushPose();

            matrix.translate(x + 2, y + 2, 0);
            matrix.scale(0.5f, 0.5f, 0);
            RenderSystem.enableBlend();
            blit(matrix, 0, 0, 0.0F, 0.0F, 32, 32, 32, 32);
            if (!hasUpdate) {
                RenderSystem.disableBlend();
            }

            matrix.popPose();

            if (hasUpdate) {
                Textures.UIs.main_menu.bind();
                // When not provided with the texture size vanilla automatically assumes both the height and width are 256
                blit(matrix, x, y, 0, 0, 20, 20);
            }

            RenderSystem.disableBlend();
        }

    }

}
