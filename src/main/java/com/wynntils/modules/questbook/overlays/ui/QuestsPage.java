/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.questbook.overlays.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.wynntils.McIf;
import com.wynntils.core.framework.ui.elements.ImageSwitcherButton;
import com.wynntils.core.utils.Utils;
import com.wynntils.modules.questbook.enums.QuestLevelType;
import com.wynntils.modules.questbook.enums.QuestStatus;
import com.wynntils.modules.questbook.instances.IconContainer;
import com.wynntils.modules.questbook.instances.QuestBookPage;
import com.wynntils.modules.questbook.instances.QuestInfo;
import com.wynntils.modules.questbook.managers.QuestManager;
import com.wynntils.webapi.WebManager;
import com.wynntils.webapi.request.Request;
import com.wynntils.webapi.request.RequestHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.ConfirmOpenLinkScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class QuestsPage extends QuestBookPage {

    private List<QuestInfo> questSearch;
    private SortMethod sort = SortMethod.LEVEL;
    private boolean showingMiniQuests = false;
    private QuestButton[] questButtons = new QuestButton[13];
    final static List<String> textLines = Arrays.asList("Here you can see all quests", "available for you. You can", "also search for a specific", "quest just by typing its name.", "You can go to the next page", "by clicking on the two buttons", "or by scrolling your mouse.", "", "You can pin/unpin a quest", "by clicking on it.");

    public QuestsPage() {
        super("Quests", true, IconContainer.questPageIcon);
    }

    @Override
    public void init() {
        int middleX = width / 2;
        int middleY = height / 2;
        for (int i = 0; i < 13; i++) {
            questButtons[i] = addButton(new QuestButton(middleX + 13, (middleY - 79) + i * 12));
            questButtons[i].visible = false;
        }
        super.init();
        this.addMenuButton();

        this.addButton(new ImageButton(middleX + 147, middleY - 99, 22 / 2, 22 / 2, 262 / 2, 258 / 2, 22 / 2, QUESTBOOK, WIDTH / 2, HEIGHT / 2, button -> {
            McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
            QuestManager.updateAllAnalyses(true);
        }, (button, matrix, mouseX, mouseY) -> {
            tooltip = Arrays.asList(
                    new StringTextComponent("Reload Button!"),
                    new StringTextComponent("Reloads all quest data.")
                            .withStyle(TextFormatting.GRAY));
        }, new StringTextComponent("Reload Button!")));

        this.addButton(new ImageSwitcherButton(middleX - 87, middleY - 100, 0, 222, 16, 16, 1f, 2, 0, QUESTBOOK, WIDTH, HEIGHT, button -> {
            McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
            showingMiniQuests = ((ImageSwitcherButton) button).index() == 1;
            textField.setValue("");
            updateSearch();
        }, (button, matrix, mouseX, mouseY) -> {
            List<ITextComponent> tooltip = new ArrayList<>(showingMiniQuests ? QuestManager.getMiniQuestsLore() : QuestManager.getQuestsLore());

            if (!tooltip.isEmpty()) {
                tooltip.set(0, new StringTextComponent(showingMiniQuests ? "Mini-Quests:" : "Quests:"));
                tooltip.add(StringTextComponent.EMPTY);
                tooltip.add(new StringTextComponent("Click to see " + (showingMiniQuests ? "Quests" : "Mini-Quests"))
                        .withStyle(TextFormatting.GREEN));
            }
            this.tooltip = tooltip;
        }, new StringTextComponent("Switch between Mini-Quests and Quests")));

        this.addButton(new ImageSwitcherButton(middleX + 1, middleY - 99, 47, 278, 22, 22, 0.5f, 2, 22, QUESTBOOK, WIDTH, HEIGHT, button -> {
            McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
            sort = SortMethod.values()[((ImageSwitcherButton) button).index()];
            updateSearch();
        }, (button, matrix, mouseX, mouseY) -> {
            this.tooltip = sort.hoverText.stream().map(TranslationTextComponent::new).collect(Collectors.toList());
        }, new StringTextComponent("Change sort method")));

        this.addBackAndForwardButtons();
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.renderQuestBook(matrix, mouseX, mouseY, partialTicks);

        int x = width / 2;
        int y = height / 2;
        hoveredText = new ArrayList<>();

        matrix.pushPose();
        // Explanatory Text
        drawTextLines(matrix, textLines, x - 154, y - 30, 1);

        // Page Text
        drawCenteredStringNoShadow(matrix, font, currentPage + " / " + pages, x + 80, y + 88, 0);

        if (questSearch.isEmpty()) {

            String textToDisplay;
            if (QuestManager.getCurrentQuests().size() == 0 || textField.getValue().equals("") ||
                    (showingMiniQuests && QuestManager.getCurrentQuests().stream().noneMatch(QuestInfo::isMiniQuest))) {
                textToDisplay = String.format("Loading %s...\nIf nothing appears soon, try pressing the reload button.", showingMiniQuests ? "Mini-Quests" : "Quests");
            } else {
                textToDisplay = String.format("No %s found!\nTry searching for something else.", showingMiniQuests ? "mini-quests" : "quests");
            }

            font.drawWordWrap(new StringTextComponent(textToDisplay), x + 26, y - 83, 120, 0);

            updateSearch();
        }
        matrix.popPose();
        super.render(matrix, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void searchUpdate(String currentText) {
        if (showingMiniQuests) questSearch = new ArrayList<>(QuestManager.getCurrentMiniQuests());
        else questSearch = new ArrayList<>(QuestManager.getCurrentQuests());

        if (currentText != null && !currentText.isEmpty()) {
            String lowerCase = currentText.toLowerCase();
            questSearch.removeIf(c -> !doesSearchMatch(c.getName().toLowerCase(), lowerCase));
        }

        questSearch.sort(sort.comparator);

        pages = questSearch.size() <= 13 ? 1 : (int) Math.ceil(questSearch.size() / 13d);
        currentPage = Math.min(currentPage, pages);
        updatePage();
    }

    @Override
    protected void updatePage() {
        super.updatePage();
        for (QuestButton button : questButtons) {
            button.visible = false;
        }
        for (int i = (currentPage - 1) * 13; i < Math.min(currentPage * 13, questSearch.size()); i++) {
            questButtons[i % 13].visible = true;
            questButtons[i % 13].quest = questSearch.get(i);
        }
    }

    @Override
    public List<ITextComponent> getHoveredDescription() {
        return Arrays.asList(
                new StringTextComponent("[>] ")
                        .withStyle(TextFormatting.GOLD)
                        .append(new StringTextComponent("Quest Book")
                                .withStyle(TextFormatting.BOLD)),
                new StringTextComponent("See and pin all your")
                        .withStyle(TextFormatting.GRAY),
                new StringTextComponent("currently available")
                        .withStyle(TextFormatting.GRAY),
                new StringTextComponent("quests.")
                        .withStyle(TextFormatting.GRAY),
                StringTextComponent.EMPTY,
                new StringTextComponent("Left click to select")
                        .withStyle(TextFormatting.GREEN));
    }

    @Override
    public void open(boolean showAnimation) {
        super.open(showAnimation);

        QuestManager.readQuestBook();
    }

    private enum SortMethod {
        LEVEL(
            Comparator.comparing(QuestInfo::getStatus)
                        .thenComparing(q -> !q.getMinLevel().containsKey(QuestLevelType.COMBAT) && !q.getMinLevel().isEmpty()).thenComparingInt((q) -> {
                            if (q.getMinLevel().containsKey(QuestLevelType.COMBAT)) {
                                return q.getMinLevel().get(QuestLevelType.COMBAT);
                            } else if (!q.getMinLevel().isEmpty()) {
                                return q.getMinLevel().values().iterator().next();
                            } else {
                                return 1;
                            }
                        }),
            Arrays.asList(
                "Sort by Level", // Replace with translation keys during l10n
                "Lowest level quests first")),
        DISTANCE(Comparator.comparing(QuestInfo::getStatus).thenComparingLong(q -> {
            ClientPlayerEntity player = McIf.player();
            if (player == null || !q.hasTargetLocation()) {
                return 0;
            }

            return (long) player.position().distanceTo(q.getTargetLocation());
        }).thenComparing(q -> !q.getMinLevel().containsKey(QuestLevelType.COMBAT) && !q.getMinLevel().isEmpty()).thenComparingInt((q) -> {
            if (q.getMinLevel().containsKey(QuestLevelType.COMBAT)) {
                return q.getMinLevel().get(QuestLevelType.COMBAT);
            } else if (!q.getMinLevel().isEmpty()) {
                return q.getMinLevel().values().iterator().next();
            } else {
                return 1;
            }
        }), 
            Arrays.asList(
                "Sort by Distance",
                "Closest quests first"));

        SortMethod(Comparator<QuestInfo> comparator, List<String> hoverText) {
            this.comparator = comparator;
            this.hoverText = hoverText;
        }

        Comparator<QuestInfo> comparator;
        List<String> hoverText;
    }

    private class QuestButton extends Widget {

        private QuestInfo quest = null;
        boolean animationCompleted = false;
        long lastTick = 0;

        public QuestButton(int x, int y) {
            super(x, y, 133, 9, StringTextComponent.EMPTY);
        }

        @Override
        public void renderButton(MatrixStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_) {
            p_230431_1_.pushPose();
            p_230431_1_.translate(0, 0, 100);
            int animationTick = -1;
            if (this.isHovered() && !showAnimation) {
                if (!animationCompleted) {
                    if (lastTick == 0) {
                        lastTick = Util.getMillis();
                    }

                    animationTick = (int) (Util.getMillis() - lastTick) / 2;

                    if (animationTick >= 133 && quest.getFriendlyName().equals(quest.getName())) {
                        animationCompleted = true;
                        animationTick = 133;
                    }
                } else {
                    if (!quest.getFriendlyName().equals(quest.getName())) {
                        animationCompleted = false;
                        lastTick = Util.getMillis() - 133 * 2;
                    }

                    animationTick = 133;
                }

                int width = Math.min(animationTick, 133);

                boolean tracked = QuestManager.getTrackedQuest() != null && QuestManager.getTrackedQuest().getName().equalsIgnoreCase(quest.getName());
                fill(p_230431_1_, this.x, this.y, this.x + width, this.y + height, tracked ? background_3 : background_1);
                fill(p_230431_1_, this.x, this.y, this.x + this.width, this.y + height, tracked ? background_4 : background_2);
            } else {
                if (lastTick != 0) {
                    animationCompleted = false;

                    if (!showAnimation) {
                        lastTick = 0;
                    }
                }

                boolean tracked = QuestManager.getTrackedQuest() != null && QuestManager.getTrackedQuest().getName().equalsIgnoreCase(quest.getName());
                fill(p_230431_1_, this.x, this.y, this.x + this.width, this.y + height, tracked ? background_4 : background_2);
            }

            Minecraft.getInstance().getTextureManager().bind(QUESTBOOK);
            if (quest.getStatus() == QuestStatus.COMPLETED) {
                blit(p_230431_1_, this.x + 1, this.y + 1, 223, 245, 11, 7, WIDTH, HEIGHT);
            } else if (quest.getStatus() == QuestStatus.CANNOT_START) {
                blit(p_230431_1_, this.x + 1, this.y + 1, 235, 245, 7, 7, WIDTH, HEIGHT);
            } else if (quest.getStatus() == QuestStatus.CAN_START) {
                if (quest.isMiniQuest()) {
                    blit(p_230431_1_, this.x + 1, this.y + 1, 272, 245, 11, 7, WIDTH, HEIGHT);
                } else {
                    blit(p_230431_1_, this.x + 1, this.y + 1, 254, 245, 11, 7, WIDTH, HEIGHT);
                }
            } else {
                blit(p_230431_1_, this.x + 1, this.y + 1, 245, 245, 8, 7, WIDTH, HEIGHT);
            }
            
            String name = quest.getFriendlyName();
            if (!name.equals(quest.getName()) && animationTick > 0) {
                name = quest.getName();
                int maxScroll = font.width(name) - (120 - 10);
                int scrollAmount = (animationTick / 20) % (maxScroll + 60);

                if (maxScroll <= scrollAmount && scrollAmount <= maxScroll + 40) {
                    // Stay on max scroll for 20 * 40 animation ticks after reaching the end
                    scrollAmount = maxScroll;
                } else if (maxScroll <= scrollAmount) {
                    // And stay on minimum scroll for 20 * 20 animation ticks after looping back to
                    // the start
                    scrollAmount = 0;
                }

                double scale = Minecraft.getInstance().getWindow().getGuiScale();
                RenderSystem.enableScissor((int) ((this.x + 13) * scale), 0, (int) ((133 - 13 - 2) * scale), (int) (QuestsPage.this.height * scale));
                font.draw(p_230431_1_, name, this.x + 13 - scrollAmount, this.y + 1, 0);
                RenderSystem.disableScissor();
            } else {
                font.draw(p_230431_1_, name, this.x + 13, this.y + 1, 0);
            }
            p_230431_1_.popPose();

            if (this.isHovered()) {
                List<ITextComponent> lore = new ArrayList<>(quest.getLore());
                lore.add(StringTextComponent.EMPTY);

                boolean tracked = QuestManager.getTrackedQuest() != null && QuestManager.getTrackedQuest().getName().equalsIgnoreCase(quest.getName());
                if (quest.getStatus() == QuestStatus.COMPLETED) {
                    lore = new ArrayList<>(lore.subList(0, lore.size() - 3));
                } else if (quest.getStatus() == QuestStatus.CANNOT_START) {
                    lore = new ArrayList<>(lore.subList(0, lore.size() - 2));
                } else if (quest.getStatus() == QuestStatus.CAN_START || quest.getStatus() == QuestStatus.STARTED) {
                    lore.remove(lore.size() - 2);
                    if (!lore.remove(lore.size() - 2).getString().isEmpty()) {
                        lore.remove(lore.size() - 2);
                    }

                    lore.add(new StringTextComponent("Left click to " + (tracked ? "un" : "") + "pin it!")
                            .withStyle(tracked ? TextFormatting.RED : TextFormatting.GREEN, TextFormatting.BOLD));
                }

                if (quest.hasTargetLocation()) {
                    lore.add(new StringTextComponent("Middle click to view on map!")
                            .withStyle(TextFormatting.YELLOW, TextFormatting.BOLD));
                }

                lore.add(new StringTextComponent("Right click to open on the wiki!")
                        .withStyle(TextFormatting.GOLD, TextFormatting.BOLD));
                tooltip = lore;
            }
        }

        @Override
        public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
            if (this.clicked(p_231044_1_, p_231044_3_)) {
                if (p_231044_5_ == 0) {
                    if (quest.getStatus() == QuestStatus.COMPLETED || quest.getStatus() == QuestStatus.CANNOT_START) {
                        return false;
                    }

                    if (QuestManager.getTrackedQuest() != null && QuestManager.getTrackedQuest().getName().equals(quest.getName())) {
                        QuestManager.setTrackedQuest(null);
                        McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.IRON_GOLEM_HURT, 1f));
                        return true;
                    }
                    McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.ANVIL_PLACE, 1f));
                    QuestManager.setTrackedQuest(quest);
                    return true;
                } else if (p_231044_5_ == 1) {
                    McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));

                    final String baseUrl = "https://wynncraft.fandom.com/";

                    if (quest.isMiniQuest()) {
                        String type = quest.getFriendlyName().split(" ")[0];

                        String wikiName = "Quests#" + type + "ing_Posts"; // Don't encode #

                        Minecraft.getInstance().setScreen(new ConfirmOpenLinkScreen((answer) -> {
                            if (answer) {
                                Util.getPlatform().openUri(baseUrl + wikiName);
                            }

                            minecraft.setScreen(QuestsPage.this);
                        }, baseUrl + wikiName, true));
                    } else {
                        String name = quest.getName();
                        String wikiQuestPageNameQuery = WebManager.getApiUrl("WikiQuestQuery");
                        String url = wikiQuestPageNameQuery + Utils.encodeForCargoQuery(name);
                        Request req = new Request(url, "WikiQuestQuery");

                        RequestHandler handler = new RequestHandler();

                        handler.addAndDispatch(req.handleJsonArray(jsonOutput -> {
                            String pageTitle = jsonOutput.get(0).getAsJsonObject().get("_pageTitle").getAsString();
                            String newUrl = baseUrl + Utils.encodeForWikiTitle(pageTitle);
                            Minecraft.getInstance().setScreen(new ConfirmOpenLinkScreen((answer) -> {
                                if (answer) {
                                    Util.getPlatform().openUri(newUrl);
                                }

                                minecraft.setScreen(QuestsPage.this);
                            }, newUrl, true));
                            return true;
                        }), true);
                        return true;
                    }
                } else if (p_231044_5_ == 2) {
                    if (!quest.hasTargetLocation()) {
                        return false;
                    }

                    Vector3d loc = quest.getTargetLocation();
                    // TODO: uncomment
//                    Minecraft.getInstance().setScreen(new MainWorldMapUI((float) loc.x, (float) loc.z));
                    return true;
                }
                System.out.println(p_231044_5_);
            }
            return false;
        }

    }

}
