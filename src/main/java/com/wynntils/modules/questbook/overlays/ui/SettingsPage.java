/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.questbook.overlays.ui;

import com.wynntils.McIf;
import com.wynntils.core.framework.settings.ui.SettingsUI;
import com.wynntils.modules.questbook.instances.IconContainer;
import com.wynntils.modules.questbook.instances.QuestBookPage;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.List;

public class SettingsPage extends QuestBookPage {
    public SettingsPage() {
        super("", false, IconContainer.settingsPageIcon);
    }

    @Override
    public void open(boolean showAnimation) {
        McIf.mc().setScreen(SettingsUI.getInstance(McIf.mc().screen));
    }

    @Override
    public List<ITextComponent> getHoveredDescription() {
        return Arrays.asList(
                new StringTextComponent("[>] ")
                        .withStyle(TextFormatting.GOLD)
                        .append(new StringTextComponent("Configuration")
                                .withStyle(TextFormatting.BOLD)),
                new StringTextComponent("Change the settings")
                        .withStyle(TextFormatting.GRAY),
                new StringTextComponent("to the way you want.")
                        .withStyle(TextFormatting.GRAY),
                StringTextComponent.EMPTY,
                new StringTextComponent("Left click to select")
                        .withStyle(TextFormatting.GREEN));
    }
}
