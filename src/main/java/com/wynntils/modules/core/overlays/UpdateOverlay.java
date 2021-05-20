/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.overlays;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynntils.McIf;
import com.wynntils.ModCore;
import com.wynntils.Reference;
import com.wynntils.core.framework.overlays.Overlay;
import com.wynntils.core.utils.Utils;
import com.wynntils.modules.core.CoreModule;
import com.wynntils.modules.core.config.CoreDBConfig;
import com.wynntils.modules.core.enums.UpdateStream;
import com.wynntils.webapi.WebManager;
import com.wynntils.webapi.downloader.DownloaderManager;
import com.wynntils.webapi.downloader.enums.DownloadAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;

public class UpdateOverlay extends Overlay {

    private static final int background = 0x333341;
    private static final int box = 0x434355;
    private static final int yes = 0x80fd80;
    private static final int no = 0xfd8080;

    public UpdateOverlay() {
        super("Update", 20, 20, true, 1f, 0f, 0, 0, null);
    }

    static boolean disappear = false;
    static boolean acceptYesOrNo = false;
    static boolean download = false;

    public static int size = 63;
    public static long timeout = 0;

    @Override
    public void render(RenderGameOverlayEvent.Post e, MatrixStack matrix) {
        if (e.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        if (Reference.developmentEnvironment || WebManager.getUpdate() == null || !WebManager.getUpdate().hasUpdate()) {
            return;
        }

        if (disappear) {
            return;
        }

        if (timeout == 0) {
            timeout = System.currentTimeMillis();
        }

        fill(matrix, background, -172, 0 - size, 0, 62 - size);
        fill(matrix, box, -170, 0 - size, 0, 60 - size);
        FontRenderer font = Minecraft.getInstance().font;

        drawString(matrix, font, new StringTextComponent("Wynntils ")
                .append(new StringTextComponent("v" + Reference.VERSION + " - ")
                        .withStyle(TextFormatting.GREEN))
                .append((((timeout + 35000) - System.currentTimeMillis()) / 1000) + "s left"), -165, 5 - size, 0x99ff00);
        if (WebManager.getUpdate().getLatestUpdate().startsWith("B")) {
            drawString(matrix, font, new StringTextComponent("")
                    .append(new StringTextComponent("Build " + WebManager.getUpdate().getLatestUpdate().replace("B", "")))
                    .append(" is available."), -165, 15 - size, 0xffffff);
        } else {
            drawString(matrix, font, new StringTextComponent("A new update is available ")
                    .append(new StringTextComponent("v" + WebManager.getUpdate().getLatestUpdate())
                            .withStyle(TextFormatting.YELLOW)),
                    -165, 15 - size, 0xffffff);
        }

        drawString(matrix, font, new StringTextComponent("Download automagically? ")
                .append(new StringTextComponent("(y/n)")
                        .withStyle(TextFormatting.GREEN)),
                -165, 25 - size, TextFormatting.GRAY.getColor());

        fill(matrix, yes, -155, 40 - size, -95, 55 - size);
        fill(matrix, no, -75, 40 - size, -15, 55 - size);

        drawCenteredString(matrix, font, "Yes (y)", -125, 44 - size, 0xffffff);
        drawCenteredString(matrix, font, "No (n)", -43, 44 - size, 0xffffff);

        if (size > 0 && System.currentTimeMillis() - timeout < 35000) {
            size--;
            if (size <= 0) {
                acceptYesOrNo = true;
            }
        } else if (size < 63 && System.currentTimeMillis() - timeout >= 35000) {
            size++;
            if (size >= 63) {
                disappear = true;
                acceptYesOrNo = false;
                download = false;
            }
        }
    }

    public static void reset() {
        disappear = false;
        acceptYesOrNo = false;
        download = false;
        size = 63;
        timeout = 0;
    }

    public static void forceDownload() {
        disappear = true;
        acceptYesOrNo = false;
        download = true;
    }

    public static void ignore() {
        reset();
        disappear = true;
    }

    @Override
    public void tick(TickEvent.ClientTickEvent event, long ticks) {
        if (download && disappear) {
            download = false;

            try {
                File directory = new File(Reference.MOD_STORAGE_ROOT, "updates");
                String url = getUpdateDownloadUrl();
                String jarName = getJarNameFromUrl(url);

                DownloadOverlay.size = 0;
                DownloaderManager.restartGameOnNextQueue();
                DownloaderManager.queueDownload("Updating to " + WebManager.getUpdate().getLatestUpdate(), url, directory, DownloadAction.SAVE, (x) -> {
                    if (x) {
                        try {
                            String message = TextFormatting.DARK_AQUA + "An update to Wynntils (";
                            message += CoreDBConfig.INSTANCE.updateStream == UpdateStream.STABLE ? "Version " + jarName.split("_")[0].split("-")[1] : "Build " + jarName.split("_")[1].replace(".jar", "");
                            message += ") has been downloaded, and will be applied when the game is restarted.";
                            McIf.sendMessage(new StringTextComponent(message));
                            scheduleCopyUpdateAtShutdown(jarName);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (acceptYesOrNo) {
            if (Utils.isKeyDown(GLFW.GLFW_KEY_Y)) {
                disappear = true;
                acceptYesOrNo = false;
                download = true;

                CoreDBConfig.INSTANCE.showChangelogs = true;
                CoreDBConfig.INSTANCE.lastVersion = Reference.VERSION;
                CoreDBConfig.INSTANCE.saveSettings(CoreModule.getModule());
            } else if (Utils.isKeyDown(GLFW.GLFW_KEY_N)) {
                timeout = 35000;
                acceptYesOrNo = false;
                download = false;
            }
        }
    }

    public static String getJarNameFromUrl(String url) {
        String[] sUrl = url.split("/");
        return sUrl[sUrl.length - 1];
    }

    public static String getUpdateDownloadUrl() throws IOException {
        if (CoreDBConfig.INSTANCE.updateStream == UpdateStream.CUTTING_EDGE) {
            return WebManager.getCuttingEdgeJarFileUrl();
        } else {
            return WebManager.getStableJarFileUrl();
        }
    }

    public static void scheduleCopyUpdateAtShutdown(String jarName) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Reference.LOGGER.info("Attempting to apply Wynntils update.");
                File oldJar = ModCore.jarFile;

                if (oldJar == null || !oldJar.exists() || oldJar.isDirectory()) {
                    Reference.LOGGER.warn("Old jar file not found.");
                    return;
                }

                File newJar = new File(new File(Reference.MOD_STORAGE_ROOT, "updates"), jarName);
                Utils.copyFile(newJar, oldJar);
                newJar.delete();
                Reference.LOGGER.info("Successfully applied Wynntils update.");
            } catch (IOException ex) {
                Reference.LOGGER.error("Unable to apply Wynntils update.", ex);
            }
        }, "wynntils-autoupdate-applier"));
        WebManager.getUpdate().updateDownloaded();
    }

    public static boolean isDownloading() {
        return download;
    }
}
