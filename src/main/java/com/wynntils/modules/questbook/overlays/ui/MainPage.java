/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.questbook.overlays.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynntils.McIf;
import com.wynntils.WynntilsSounds;
import com.wynntils.core.framework.instances.PlayerInfo;
import com.wynntils.core.framework.instances.data.CharacterData;
import com.wynntils.core.utils.StringUtils;
import com.wynntils.modules.questbook.enums.QuestBookPages;
import com.wynntils.modules.questbook.instances.QuestBookPage;
import com.wynntils.webapi.WebManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class MainPage extends QuestBookPage {

    MainPage.PageButton[][] pageButtons = new MainPage.PageButton[0][0];

    public MainPage() {
        super("User Profile", false, null);
    }

    @Override
    public void init() {
        super.init();
        pages = (int) Math.ceil(Arrays.stream(QuestBookPages.values()).max(Comparator.comparingInt(QuestBookPages::getSlotNb)).get().getSlotNb() / 4d);
        pageButtons = new MainPage.PageButton[pages][4];
        for (QuestBookPages pageEnum : QuestBookPages.values()) {
            if (pageEnum.getPage().getIcon() != null) {
                int slotNB = pageEnum.getSlotNb() - 1;
                pageButtons[slotNB / 4][slotNB % 4] = addButton(new MainPage.PageButton(pageEnum.getPage(), (width / 2) - 150 + 35 * ((pageEnum.getSlotNb() - 1) % 4)));
            }
        }
        int middleX = width / 2;
        int middleY = height / 2;
        rightPage = addButton(new ImageButton(middleX - 80, middleY + 15, 16, 9, 102, 301, 9, QUESTBOOK, WIDTH, HEIGHT, button -> {
            goForward();
        }));
        leftPage = addButton(new ImageButton(middleX - 101, middleY + 15, 16, 9, 118, 301, 9, QUESTBOOK, WIDTH, HEIGHT, button -> {
            goBack();
        }));
        updatePage();
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.renderQuestBook(matrix, mouseX, mouseY, partialTicks);
        int x = width / 2;
        int y = height / 2;
        int posX = (x - mouseX); int posY = (y - mouseY);
        hoveredText = new ArrayList<>();

        matrix.pushPose();
        int right = (posX + 80);
        if (posX >= 0)
            right = 80;

        int up = (posY) + 30;
        if (posY >= 109)
            up = 109;
        if (posY <= -109)
            up = -109;

        InventoryScreen.renderEntityInInventory(x + 80, y + 30, 30, right, up, McIf.player());
        matrix.popPose();

        matrix.pushPose();
        Minecraft.getInstance().getTextureManager().bind(new ResourceLocation("wynntils", "textures/screens/quest_book/quest_book.png"));
        for (int imageX = 0; imageX < 5; imageX++) {
            blit(matrix, x + 20 + imageX * 26, y - 90, 224, 253, 17, 18, WIDTH, HEIGHT);
        }

        String guild = WebManager.getPlayerProfile() != null ? WebManager.getPlayerProfile().getGuildRank() != null ? WebManager.getPlayerProfile().getGuildName() + " " + WebManager.getPlayerProfile().getGuildRank().getStars() : WebManager.getPlayerProfile().getGuildName() : "";

        drawCenteredStringNoShadow(matrix, font, guild, x + 80, y - 53, TextFormatting.AQUA.getColor());
        drawCenteredStringNoShadow(matrix, font, McIf.player().getName(), x + 80, y - 43, 0);
        drawCenteredStringNoShadow(matrix, font, PlayerInfo.get(CharacterData.class).getCurrentClass().toString() + " Level " + PlayerInfo.get(CharacterData.class).getLevel(), x + 80, y + 40, TextFormatting.DARK_PURPLE.getColor());
        drawCenteredStringNoShadow(matrix, font, "In Development", x + 80, y + 50, TextFormatting.RED.getColor());
        drawCenteredStringNoShadow(matrix, font, StringUtils.rainbow(WebManager.getCurrentSplash()), x + 82, y + 70, 0);

        drawCenteredStringNoShadow(matrix, font, "Select an option to continue", x - 81, y - 30, 0);

        matrix.pushPose();
        font.drawWordWrap(new StringTextComponent("Welcome to Wynntils. You can see your statistics on the right or select some of the options above for more features"),
                x - 150, y + 30, 155, 0);
        matrix.popPose();
        super.render(matrix, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (selected > 0) {
            Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(WynntilsSounds.QUESTBOOK_PAGE.get(), 1f));
            QuestBookPages.getPageBySlot(selected).open(false);
            return true;
        } else if (selected == -1) {
            goBack();
            return true;
        } else if (selected == -2) {
            goForward();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void updatePage() {
        super.updatePage();
        if (pageButtons.length > 0) {
            for (MainPage.PageButton[] page : pageButtons) {
                for (MainPage.PageButton button : page) {
                    if (button != null) {
                        button.visible = false;
                    }
                }
            }
            for (MainPage.PageButton button : pageButtons[this.currentPage - 1]) {
                if (button != null) {
                    button.visible = true;
                }
            }
        }
        if (currentPage == 1) {
            leftPage.visible = false;
        } else {
            leftPage.visible = true;
        }
        if (currentPage == pages) {
            rightPage.visible = false;
        } else {
            rightPage.visible = true;
        }
    }

    private class PageButton extends ImageButton {

        private QuestBookPage page;

        public PageButton(QuestBookPage page, int p_i244513_1_) {
            super(p_i244513_1_, Minecraft.getInstance().screen.height / 2 - 18, 30, 30, page.getIcon().getX() * 30, 0, page.getIcon().hasHighlight() ? 30 : 0, QuestBookPage.PAGE_ICONS, 256, 256, button -> page.open(false), (button, matrix, mouseX, mouseY) -> {
            }, page.getTitle());
            this.page = page;
        }

        @Override
        public void renderButton(MatrixStack matrix, int p_230431_2_, int p_230431_3_, float p_230431_4_) {
            if (this.isHovered()) {
                fill(matrix, this.x, this.y, this.x + this.width, this.y + this.height, selected_cube);
            } else {
                fill(matrix, this.x, this.y, this.x + this.width, this.y + this.height, unselected_cube);
            }
            super.renderButton(matrix, p_230431_2_, p_230431_3_, p_230431_4_);
        }

        @Override
        public void renderToolTip(MatrixStack p_230443_1_, int p_230443_2_, int p_230443_3_) {
            tooltip = page.getHoveredDescription();
        }

    }
}
