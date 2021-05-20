/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.questbook.overlays.ui;

import com.wynntils.McIf;
import com.wynntils.core.framework.settings.ui.OverlayPositionsUI;
import com.wynntils.core.framework.ui.UI;
import com.wynntils.modules.questbook.instances.IconContainer;
import com.wynntils.modules.questbook.instances.QuestBookPage;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.List;

public class HUDConfigPage extends QuestBookPage {
    public HUDConfigPage() {
        super("", false, IconContainer.hudConfigIcon);
    }

    @Override
    public void open(boolean showAnimation) {
        OverlayPositionsUI ui = new OverlayPositionsUI(McIf.mc().screen);
        UI.setupUI(ui);
        McIf.mc().setScreen(ui);
    }

    @Override
    public List<ITextComponent> getHoveredDescription() {
        return Arrays.asList(
                new StringTextComponent("[>] ")
                        .withStyle(TextFormatting.GOLD)
                        .append(new StringTextComponent("Overlay Configuration")
                                .withStyle(TextFormatting.BOLD)),
                new StringTextComponent("Change position")
                        .withStyle(TextFormatting.GRAY),
                new StringTextComponent("and enable/disable")
                        .withStyle(TextFormatting.GRAY),
                new StringTextComponent("the various")
                        .withStyle(TextFormatting.GRAY),
                new StringTextComponent("Wynntils overlays")
                        .withStyle(TextFormatting.GRAY),
                StringTextComponent.EMPTY,
                new StringTextComponent("Left click to select")
                        .withStyle(TextFormatting.GREEN));
    }
}
