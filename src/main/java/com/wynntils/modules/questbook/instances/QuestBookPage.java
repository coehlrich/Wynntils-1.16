/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.questbook.instances;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynntils.McIf;
import com.wynntils.Reference;
import com.wynntils.WynntilsSounds;
import com.wynntils.core.utils.StringUtils;
import com.wynntils.core.utils.reference.Easing;
import com.wynntils.modules.core.config.CoreDBConfig;
import com.wynntils.modules.core.enums.UpdateStream;
import com.wynntils.modules.questbook.configs.QuestBookConfig;
import com.wynntils.modules.questbook.enums.QuestBookPages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestBookPage extends Screen {

    private long time;
    private boolean open = false;

    // Page specific information
    private String title;
    private IconContainer icon;
    protected boolean showAnimation;
    protected List<ITextComponent> hoveredText = new ArrayList<>();

    private boolean showSearchBar;
    protected int currentPage;
    protected boolean acceptNext, acceptBack;
    protected int pages = 1;
    protected int selected;
    protected TextFieldWidget textField = null;
    protected Button rightPage = null;
    protected Button leftPage = null;

    // Animation
    protected long lastTick;
    protected boolean animationCompleted;

    private long delay = McIf.getSystemTime();

    protected List<ITextComponent> tooltip = null;

    // Colours
    protected static final int background_1 = 0x4c000000;
    protected static final int background_2 = 0x33000000;
    protected static final int background_3 = 0x4c00ff00;
    protected static final int background_4 = 0x33008f00;

    protected static final int unselected_cube = 0x33000000;
    protected static final int selected_cube = 0x4d000000;
    protected static final int selected_cube_2 = 0x4d11c920;

    public static final ResourceLocation QUESTBOOK = new ResourceLocation("wynntils", "textures/screens/quest_book/quest_book.png");
    public static final ResourceLocation PAGE_ICONS = new ResourceLocation("wynntils", "textures/screens/quest_book/icons.png");
    public static int WIDTH = 512;
    public static int HEIGHT = 512;

    /**
     * Base class for all questbook pages
     * @param title a string displayed on the left page
     * @param showSearchBar boolean of whether there is a searchbar needed for that page
     * @param icon the icon that corresponds to the page
     */
    public QuestBookPage(String title, boolean showSearchBar, IconContainer icon) {
        super(new StringTextComponent(title));
        this.title = title;
        this.showSearchBar = showSearchBar;
        this.icon = icon;
    }

    /**
     * Resets all basic information needed for various features on all pages
     */
    @Override
    public void init() {
        if (open) {
            updatePage();
            if (!showSearchBar) return;

            addWidget(textField);
            textField.x = width / 2 + 32;
            textField.y = height / 2 - 97;
            return;
        }

        open = true;
        currentPage = 1;
        selected = 0;
        searchUpdate("");
        time = McIf.getSystemTime();
        lastTick = McIf.getSystemTime();

        if (showSearchBar) {
            textField = new TextFieldWidget(McIf.mc().font, width / 2 + 32, height / 2 - 97, 113, 23, new StringTextComponent("Search"));
            textField.changeFocus(!QuestBookConfig.INSTANCE.searchBoxClickRequired);
            textField.setMaxLength(50);
            textField.setBordered(false);
            textField.setCanLoseFocus(QuestBookConfig.INSTANCE.searchBoxClickRequired);
            textField.setResponder(this::searchUpdate);
            addWidget(textField);
        }

    }

    public void addBackAndForwardButtons() {
        int middleX = width / 2;
        int middleY = height / 2;
        rightPage = addButton(new ImageButton(middleX + 128, middleY + 88, 18, 10, 100, 281, 10, QUESTBOOK, WIDTH, HEIGHT, button -> {
            goForward();
        }));

        leftPage = addButton(new ImageButton(middleX + 13, middleY + 88, 18, 10, 118, 281, 10, QUESTBOOK, WIDTH, HEIGHT, button -> {
            goBack();
        }));
    }

    public void addMenuButton() {
        int middleX = width / 2;
        int middleY = height / 2;
        addButton(new ImageButton(middleX - 90, middleY - 46, 16, 9, 118, 301, 9, QUESTBOOK, WIDTH, HEIGHT, button -> {
            Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(WynntilsSounds.QUESTBOOK_PAGE.get(), 1f));
            QuestBookPages.MAIN.getPage().open(false);
        }, (button, matrix, mouseX, mouseY) -> {
            tooltip = Arrays.asList(
                    new StringTextComponent("[>] ")
                            .withStyle(TextFormatting.GOLD)
                            .append(new StringTextComponent("Back to Menu")
                                    .withStyle(TextFormatting.BOLD)),
                    new StringTextComponent("Click here to go")
                            .withStyle(TextFormatting.GRAY),
                    new StringTextComponent("back to the main page")
                            .withStyle(TextFormatting.GRAY),
                    StringTextComponent.EMPTY,
                    new StringTextComponent("Left click to select")
                            .withStyle(TextFormatting.GREEN));
        }, new StringTextComponent("Back to Menu")));
    }

    public void renderQuestBook(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        int x = width / 2;
        int y = height / 2;

        matrix.pushPose();
        if (showAnimation) {
            float animationTick = Easing.BACK_IN.ease((McIf.getSystemTime() - time) + 1000, 1f, 1f, 600f);
            animationTick /= 10f;

            if (animationTick <= 1) {
                matrix.scale(animationTick, animationTick, animationTick);

                x = (int) (x / animationTick);
                y = (int) (y / animationTick);
            } else {
                showAnimation = false;
            }

        } else {
            x = width / 2;
            y = height / 2;
        }
        TextureManager manager = Minecraft.getInstance().getTextureManager();
        manager.bind(QUESTBOOK);

        FontRenderer font = Minecraft.getInstance().font;

        blit(matrix, x - (339 / 2), y - (220 / 2), 0, 0, 339, 220, WIDTH, HEIGHT);

        blit(matrix, x - 168, y - 81, 34, 222, 168, 33, WIDTH, HEIGHT);

        matrix.pushPose();
        matrix.scale(0.7f, 0.7f, 0.7f);
        drawCenteredStringNoShadow(matrix, font, CoreDBConfig.INSTANCE.updateStream == UpdateStream.STABLE ? "Stable v" + Reference.VERSION : "CE Build " + (Reference.BUILD_NUMBER == -1 ? "?" : Reference.BUILD_NUMBER), (int) ((x - 80) / 0.7f), (int) ((y + 86) / 0.7f), TextFormatting.YELLOW.getColor());
        matrix.popPose();

        matrix.pushPose();
        matrix.scale(2f, 2f, 2f);
        font.draw(matrix, title, (int) ((x - 158f) / 2.0f), (int) ((y - 74) / 2.0f), TextFormatting.YELLOW.getColor());
        matrix.popPose();

        /* Render search bar when needed */
        if (showSearchBar) {
            drawSearchBar(matrix, mouseX, mouseY, partialTicks);
        }

        matrix.popPose();
    }

    @Override
    public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        tooltip = null;
        super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
        if (tooltip != null) {
            this.renderComponentTooltip(p_230430_1_, tooltip, p_230430_2_, p_230430_3_);
        }
    }

    protected void drawSearchBar(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        Minecraft.getInstance().getTextureManager().bind(QUESTBOOK);
        blit(matrix, width / 2 + 13, height / 2 - 109, 52, 255, 133, 23, WIDTH, HEIGHT);
        textField.render(matrix, mouseX, mouseY, partialTicks);
    }

    public void drawCenteredStringNoShadow(MatrixStack p_238471_0_, FontRenderer p_238471_1_, String p_238471_2_, int p_238471_3_, int p_238471_4_, int p_238471_5_) {
        p_238471_1_.draw(p_238471_0_, p_238471_2_, (float) (p_238471_3_ - p_238471_1_.width(p_238471_2_) / 2), (float) p_238471_4_, p_238471_5_);
    }

    public void drawCenteredStringNoShadow(MatrixStack p_238472_0_, FontRenderer p_238472_1_, ITextComponent p_238472_2_, int p_238472_3_, int p_238472_4_, int p_238472_5_) {
        IReorderingProcessor ireorderingprocessor = p_238472_2_.getVisualOrderText();
        p_238472_1_.draw(p_238472_0_, ireorderingprocessor, (float) (p_238472_3_ - p_238472_1_.width(ireorderingprocessor) / 2), (float) p_238472_4_, p_238472_5_);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (showSearchBar && textField.mouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseScrolled(double double1, double double2, double scrollDelta) {
        int mDWheel = (int) (scrollDelta * CoreDBConfig.INSTANCE.scrollDirection.getScrollDirection());

        if (mDWheel <= -1 && (McIf.getSystemTime() - delay >= 15)) {
            if (acceptNext) {
                delay = McIf.getSystemTime();
                goForward();
                return true;
            }
        } else if (mDWheel >= 1 && (McIf.getSystemTime() - delay >= 15)) {
            if (acceptBack) {
                delay = McIf.getSystemTime();
                goBack();
                return true;
            }
        }
        return super.mouseScrolled(double1, double2, scrollDelta);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (showSearchBar) {
            boolean returnValue = textField.charTyped(typedChar, keyCode);
            currentPage = 1;
            updatePage();
            updateSearch();
            return returnValue;
        }
        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public void tick() {
        if (showSearchBar) {
            textField.tick();
        }
    }

    protected void searchUpdate(String currentText) { }

    protected boolean doesSearchMatch(String toCheck, String searchText) {
        return QuestBookConfig.INSTANCE.useFuzzySearch ? StringUtils.fuzzyMatch(toCheck, searchText) : toCheck.contains(searchText);
    }

    protected void goForward() {
        if (acceptNext) {
            Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(WynntilsSounds.QUESTBOOK_PAGE.get(), 1f));
            currentPage++;
            updatePage();
        }
    }

    protected void goBack() {
        if (acceptBack) {
            Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(WynntilsSounds.QUESTBOOK_PAGE.get(), 1f));
            currentPage--;
            updatePage();
        }
    }

    protected void updatePage() {
        acceptBack = currentPage > 1;
        acceptNext = currentPage < pages;

        if (leftPage != null) {
            leftPage.visible = acceptBack;
            rightPage.visible = acceptNext;
        }
    }

    public void open(boolean showAnimation) {
        this.showAnimation = showAnimation;

        if (showAnimation)
            Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(WynntilsSounds.QUESTBOOK_OPENING.get(), 1f)); // sfx
        McIf.mc().setScreen(this);
    }

    public void updateSearch() {
        if (showSearchBar && textField != null) {
            searchUpdate(textField.getValue());
        }
    }

    public IconContainer getIcon() {
        return icon;
    }

    /**
     * Can be null
     * @return a list of strings - each index representing a new line.
     */
    public List<ITextComponent> getHoveredDescription() {
        return hoveredText;
    }

    /**
     * Draws a list of text lines
     *
     * @param lines list of lines to be rendered
     * @param startX x to start rendering at
     * @param startY y to start rendering at
     */
    protected void drawTextLines(MatrixStack matrix, List<String> lines, int startX, int startY, int scale) {
        int currentY = startY;
        matrix.pushPose();
        matrix.scale(scale, scale, scale);
        FontRenderer font = Minecraft.getInstance().font;
        for (String line : lines) {
            font.draw(matrix, line, startX, currentY, 0x000000);
            currentY += 10 * scale;
        }
        matrix.popPose();
    }

    @Override
    public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_) {
        if (showSearchBar && textField.canConsumeInput() && textField.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_)) {
            return true;
        }
        return super.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_);
    }

}
