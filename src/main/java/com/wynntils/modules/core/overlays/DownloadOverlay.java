/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.overlays;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynntils.McIf;
import com.wynntils.core.framework.overlays.Overlay;
import com.wynntils.webapi.downloader.DownloadProfile;
import com.wynntils.webapi.downloader.DownloaderManager;
import com.wynntils.webapi.downloader.enums.DownloadPhase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class DownloadOverlay extends Overlay {

    private static final int background = 0x333341;
    private static final int box = 0x434355;
    private static final int progress = 0x80fd80;
    private static final int back = 0xececec;

    private static final int backgroundRed = 0x6e3737;
    private static final int boxRed = 0xfd8080;

    static int lastPercent = 0;
    static DownloadPhase lastPhase;
    static String lastTitle = "";

    static long timeToRestart = 0;

    public static int size = 53;

    public DownloadOverlay() {
        super("Downloading", 20, 20, true, 1.0f, 0.0f, 0, 0, null);
    }

    @Override
    public void render(RenderGameOverlayEvent.Post e, MatrixStack matrix) {
        if (e.isCanceled() || e.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        FontRenderer font = Minecraft.getInstance().font;
        if (DownloaderManager.currentPhase != DownloadPhase.WAITING || size < 53) {
            if (DownloaderManager.restartOnQueueFinish && DownloaderManager.currentPhase == DownloadPhase.WAITING) {
                if (timeToRestart == 0) {
                    timeToRestart = System.currentTimeMillis() + 10000;
                }
                if (timeToRestart - System.currentTimeMillis() <= 0) {
                    McIf.mc().stop();
                    return;
                }

                fill(matrix, -172, 0 - size, 0, 52 - size, backgroundRed);
                fill(matrix, -170, 0 - size, 0, 50 - size, boxRed);
                drawCenteredString(matrix, font, "Your game will be closed in", -84, 15 - size, 0xffffff);
                drawCenteredString(matrix, font, ((timeToRestart - System.currentTimeMillis()) / 1000) + " seconds", -84, 25 - size, TextFormatting.RED.getColor());
                return;
            }

            DownloadProfile df = DownloaderManager.getCurrentDownload();

            if (df != null) {
                lastPercent = DownloaderManager.progression;
                lastTitle = df.getTitle();
                lastPhase = DownloaderManager.currentPhase;
            }

            fill(matrix, -172, 0 - size, 0, 52 - size, background);
            fill(matrix, box, -170, 0 - size, 0, 50 - size);
            drawCenteredString(matrix, font, lastTitle, -85, 5 - size, 0xffffff);

            fill(matrix, -160, 20 - size, -10, 36 - size, back);

            fill(matrix, -160, 20 - size, ((lastPercent * (-10 + 160)) + 100 * -160) / 100, 36 - size, progress);
            drawCenteredString(matrix, font, lastPercent + "%", -84, 25 - size, TextFormatting.GRAY.getColor());

            drawCenteredString(matrix, font, (DownloaderManager.getQueueSizeLeft()) + " files left", -84, 40 - size, 0xffffff);
        }

        if (size > 0 && DownloaderManager.currentPhase != DownloadPhase.WAITING) {
            size--;
        } else if (size < 53 && DownloaderManager.currentPhase == DownloadPhase.WAITING && !DownloaderManager.restartOnQueueFinish) {
            size++;
        }
    }

}
