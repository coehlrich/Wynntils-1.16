/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.questbook.events;

import com.wynntils.McIf;
import com.wynntils.Reference;
import com.wynntils.core.events.custom.GameEvent;
import com.wynntils.core.events.custom.PacketEvent;
import com.wynntils.core.events.custom.WynnClassChangeEvent;
import com.wynntils.core.events.custom.WynnWorldEvent;
import com.wynntils.core.framework.enums.ClassType;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.core.utils.helpers.Delay;
import com.wynntils.modules.core.enums.ToggleSetting;
import com.wynntils.modules.questbook.configs.QuestBookConfig;
import com.wynntils.modules.questbook.enums.AnalysePosition;
import com.wynntils.modules.questbook.enums.QuestBookPages;
import com.wynntils.modules.questbook.events.custom.QuestBookUpdateEvent;
import com.wynntils.modules.questbook.instances.QuestBookPage;
import com.wynntils.modules.questbook.managers.QuestManager;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;

import java.util.Arrays;

public class ClientEvents implements Listener {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChat(GameEvent e)  {
        AnalysePosition position;
        boolean fullRead = false;
        boolean readImmediately = false;

        if (e instanceof GameEvent.LevelUp) {
            if (e instanceof GameEvent.LevelUp.Profession) position = AnalysePosition.MINIQUESTS;
            else position = AnalysePosition.QUESTS;

            fullRead = true;
        } else if (e instanceof GameEvent.QuestCompleted.MiniQuest) {
            QuestManager.completeQuest(((GameEvent.QuestCompleted.MiniQuest) e).getQuestName(), true);
            return;
        }
        else if (e instanceof GameEvent.QuestCompleted) {
            QuestManager.completeQuest(((GameEvent.QuestCompleted) e).getQuestName(), false);
            return;
        }
        else if (e instanceof GameEvent.QuestStarted.MiniQuest) {
            position = AnalysePosition.MINIQUESTS;
            readImmediately = ((GameEvent.QuestStarted.MiniQuest) e).getQuest().equalsIgnoreCase(QuestManager.getTrackedQuestName());
        } else if (e instanceof GameEvent.QuestStarted) {
            position = AnalysePosition.QUESTS;
            // Update immediately if started the tracked quest
            readImmediately = ((GameEvent.QuestStarted) e).getQuest().equalsIgnoreCase(QuestManager.getTrackedQuestName());
        } else if (e instanceof GameEvent.QuestUpdated) {
            position = AnalysePosition.QUESTS;
            // Update immediately because the tracked quest may have updated
            readImmediately = QuestManager.hasTrackedQuest();
        } else if (e instanceof GameEvent.DiscoveryFound.Secret)
            position = AnalysePosition.SECRET_DISCOVERIES;
        else if (e instanceof GameEvent.DiscoveryFound)
            position = AnalysePosition.DISCOVERIES;
        else return;

        QuestManager.updateAnalysis(position, fullRead, readImmediately && !QuestBookConfig.INSTANCE.updateWhenOpen);
    }

    @SubscribeEvent
    public void onUpdate(QuestBookUpdateEvent.Partial e) {
        onUpdate();
    }

    @SubscribeEvent
    public void onUpdate(QuestBookUpdateEvent.Full e) {
        onUpdate();
    }

    private static void onUpdate() {
        Arrays.stream(QuestBookPages.values()).map(QuestBookPages::getPage).forEach(QuestBookPage::updateSearch);
    }

    @SubscribeEvent
    public void onClassChange(WynnClassChangeEvent e) {
        if (e.getNewClass() == ClassType.NONE) return;

        if (QuestBookConfig.INSTANCE.allowCustomQuestbook) {
            new Delay(() -> ToggleSetting.QUEST_TRACKER.set(false), 20);
        }
        QuestManager.clearData();
    }

    @SubscribeEvent
    public void startReading(WynnWorldEvent.Leave e) {
        QuestManager.clearData();
    }

    boolean openQuestBook = false;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void clickOnQuestBookItem(PacketEvent<CPlayerTryUseItemPacket> e) {
        if (!QuestBookConfig.INSTANCE.allowCustomQuestbook
                || !Reference.onWorld || Reference.onNether || Reference.onWars
                || McIf.player().inventory.selected != 7) return;

        openQuestBook = true;
        e.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void clickOnQuestBookItemOnBlock(PacketEvent<CPlayerTryUseItemOnBlockPacket> e) {
        if (!QuestBookConfig.INSTANCE.allowCustomQuestbook
                || !Reference.onWorld || Reference.onNether || Reference.onWars
                || McIf.player().inventory.selected != 7) return;

        openQuestBook = true;
        e.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void clickOnQuestBookEntity(PacketEvent<CUseEntityPacket> e) {
        if (!QuestBookConfig.INSTANCE.allowCustomQuestbook
                || !Reference.onWorld || Reference.onNether || Reference.onWars
                || McIf.player().inventory.selected != 7) return;

        openQuestBook = true;
        e.setCanceled(true);
    }

    @SubscribeEvent
    public void updateQuestBook(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START || !Reference.onWorld || Reference.onNether || Reference.onWars || McIf.player().inventory == null) return;
        if (McIf.player().inventory.getItem(7).isEmpty() || McIf.player().inventory.getItem(7).getItem() != Items.WRITTEN_BOOK) return;

        if (!openQuestBook) return;
        openQuestBook = false;

        QuestBookPages.MAIN.getPage().open(true);

        QuestManager.readQuestBook();
    }

}
