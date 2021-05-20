/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.questbook;

import com.wynntils.core.framework.enums.Priority;
import com.wynntils.core.framework.instances.Module;
import com.wynntils.core.framework.interfaces.annotations.ModuleInfo;
import com.wynntils.modules.questbook.configs.QuestBookConfig;
import com.wynntils.modules.questbook.enums.QuestBookPages;
import com.wynntils.modules.questbook.events.ClientEvents;
import com.wynntils.modules.questbook.managers.QuestManager;
import com.wynntils.modules.questbook.overlays.hud.TrackedQuestOverlay;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

@ModuleInfo(name = "quest_book", displayName = "Quest Book")
public class QuestBookModule extends Module {

    private static QuestBookModule module;

    public void onEnable() {
        module = this;

        registerEvents(new ClientEvents());

        registerSettings(QuestBookConfig.class);
        registerOverlay(new TrackedQuestOverlay(), Priority.HIGHEST);

        // TODO: command
//        registerCommand(new CommandExportDiscoveries());

        registerKeyBinding("Open Quest Book", GLFW.GLFW_KEY_K, "Wynntils", KeyConflictContext.IN_GAME, true, () -> QuestBookPages.QUESTS.getPage().open(true));
        registerKeyBinding("Open Discoveries", GLFW.GLFW_KEY_U, "Wynntils", KeyConflictContext.IN_GAME, true, () -> QuestBookPages.DISCOVERIES.getPage().open(true));
        registerKeyBinding("Open Item Guide", GLFW.GLFW_KEY_UNKNOWN, "Wynntils", KeyConflictContext.IN_GAME, true, () -> QuestBookPages.ITEMGUIDE.getPage().open(true));
        registerKeyBinding("Open Lootrun List", GLFW.GLFW_KEY_UNKNOWN, "Wynntils", KeyConflictContext.IN_GAME, true, () -> QuestBookPages.LOOTRUNS.getPage().open(true));
        registerKeyBinding("Open HUD configuration", GLFW.GLFW_KEY_UNKNOWN, "Wynntils", KeyConflictContext.IN_GAME, true, () -> QuestBookPages.HUDCONFIG.getPage().open(true));
        registerKeyBinding("Open Menu", GLFW.GLFW_KEY_I, "Wynntils", KeyConflictContext.IN_GAME, true, () -> {
            QuestBookPages.MAIN.getPage().open(true);
            QuestManager.readQuestBook();
        });
    }

    public static QuestBookModule getModule() {
        return module;
    }

}
