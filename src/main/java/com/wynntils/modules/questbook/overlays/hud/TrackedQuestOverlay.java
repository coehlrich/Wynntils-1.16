/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.questbook.overlays.hud;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynntils.core.framework.overlays.Overlay;
import com.wynntils.modules.questbook.instances.QuestInfo;
import com.wynntils.modules.questbook.managers.QuestManager;
import com.wynntils.modules.utilities.configs.OverlayConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class TrackedQuestOverlay extends Overlay {

    public TrackedQuestOverlay() {
        super("Tracked Quest", 215, 70, true, 0.0f, 0.0f, 120, 10, OverlayGrowFrom.TOP_LEFT);
    }


    @Override
    public void render(RenderGameOverlayEvent.Pre e, MatrixStack matrix) {
        if (e.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE && e.getType() != RenderGameOverlayEvent.ElementType.JUMPBAR)
            return;

        QuestInfo trackedQuest = QuestManager.getTrackedQuest();
        OverlayConfig.TrackedQuestInfo config = OverlayConfig.TrackedQuestInfo.INSTANCE;

        if (trackedQuest == null || trackedQuest.getSplittedDescription() == null || trackedQuest.getSplittedDescription().size() == 0)
            return;

        FontRenderer font = Minecraft.getInstance().font;
        String name = trackedQuest.isMiniQuest() ? "Mini-Quest" : "Quest";
        drawString(matrix, font, "Tracked " + name + " Info: ", 0, 0, TextFormatting.DARK_GREEN.getColor());

        int currentY = 0;
        // TODO: uncomment
//        if (config.displayQuestName) {
//            drawString(matrix, font, trackedQuest.getName(), 0, 10 + currentY, TextFormatting.GREEN.getColor());
//            currentY += 10;
//        }
        for (String message : trackedQuest.getSplittedDescription()) {
            drawString(matrix, font, message, 0, 10 + currentY, 0xffffff);
            currentY += 10;
        }

        if (!QuestManager.hasInterrupted()) return;

        drawString(matrix, font, "(Open your book to update)", 0, 20 + currentY, 0xffffff);
    }

}
