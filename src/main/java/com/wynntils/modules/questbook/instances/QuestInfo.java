/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.questbook.instances;

import com.wynntils.McIf;
import com.wynntils.core.utils.ItemUtils;
import com.wynntils.core.utils.StringUtils;
import com.wynntils.modules.core.managers.CompassManager;
import com.wynntils.modules.questbook.configs.QuestBookConfig;
import com.wynntils.modules.questbook.enums.QuestLevelType;
import com.wynntils.modules.questbook.enums.QuestSize;
import com.wynntils.modules.questbook.enums.QuestStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QuestInfo {

    private static final Pattern coordinatePattern = Pattern.compile("\\[(-?\\d+), ?(-?\\d+), ?(-?\\d+)\\]");

    private final ItemStack originalStack;

    private final String name;
    private final Map<QuestLevelType, Integer> minLevels = new HashMap<>();
    private QuestStatus status;
    private QuestSize size;
    private final List<ITextComponent> lore;
    private String description;

    private String friendlyName;
    private List<String> splittedDescription;
    private Vector3d targetLocation = null;

    private boolean valid = false;
    private final boolean isMiniQuest;

    public QuestInfo(ItemStack originalStack, boolean isMiniQuest) {
        this.originalStack = originalStack;
        this.isMiniQuest = isMiniQuest;

        lore = ItemUtils.getLore(originalStack);
        name = StringUtils.normalizeBadString(TextFormatting.stripFormatting(originalStack.getHoverName().getString())).replace(" [Tracked]", "");

        Iterator<ITextComponent> loreIterator = lore.iterator();

        ITextComponent statusString = loreIterator.next();
        //quest status
        if (statusString.getString().contains("Completed!"))
            status = QuestStatus.COMPLETED;
        else if (statusString.getString().contains("Started"))
            status = QuestStatus.STARTED;
        else if (statusString.getString().contains("Can start"))
            status = QuestStatus.CAN_START;
        else if (statusString.getString().contains("Cannot start"))
            status = QuestStatus.CANNOT_START;
        else return;
        loreIterator.next();

        ITextComponent levelTypes;
        while ((levelTypes = (loreIterator.next())).getString().contains("Lv. Min:")) {
            String[] parts = levelTypes.getString().split("\\s+");
            QuestLevelType levelType = QuestLevelType.valueOf(parts[1].toUpperCase(Locale.ROOT));
            int minLevel = Integer.parseInt(parts[parts.length - 1]);
            minLevels.put(levelType, minLevel);
        }
        size = QuestSize.valueOf(levelTypes.getString().replace("- Length: ", "").toUpperCase(Locale.ROOT));

        loreIterator.next();
        // flat description
        StringBuilder descriptionBuilder = new StringBuilder();
        while (loreIterator.hasNext()) {
            ITextComponent description = loreIterator.next();
            if (description.getString().equalsIgnoreCase("Right click to stop tracking") || description.getString().equalsIgnoreCase("RIGHT-CLICK TO TRACK")) {
                break;
            }

            if (descriptionBuilder.length() > 0 && !descriptionBuilder.substring(descriptionBuilder.length() - 1).equals(" ")) {
                descriptionBuilder.append(" ");
            }

            descriptionBuilder.append(description.getString());
        }

        description = descriptionBuilder.toString();

        // splitted description
        splittedDescription = Minecraft.getInstance().font.getSplitter().splitLines(description, 200, Style.EMPTY).stream().map(ITextProperties::getString).collect(Collectors.toList());

        // friendly name
        friendlyName = this.name.replace("Mini-Quest - ", "");
        if (McIf.mc().font.width(friendlyName) > 120) friendlyName += "...";
        while (McIf.mc().font.width(friendlyName) > 120) {
            friendlyName = friendlyName.substring(0, friendlyName.length() - 4).trim() + "...";
        }

        // location
        Matcher m = coordinatePattern.matcher(description);
        if (m.find()) {
            targetLocation = new Vector3d(
                    m.group(1) != null ? Integer.parseInt(m.group(1)) : 0,
                    m.group(2) != null ? Integer.parseInt(m.group(2)) : 0,
                    m.group(3) != null ? Integer.parseInt(m.group(3)) : 0);
        }

        lore.add(0, new StringTextComponent(name)
                .withStyle(TextFormatting.BOLD));
        valid = true;

        // translation (might replace splittedDescription)
        // TODO: uncomment
//        if (TranslationConfig.INSTANCE.enableTextTranslation && TranslationConfig.INSTANCE.translateTrackedQuest) {
//            TranslationManager.getTranslator().translate(description, TranslationConfig.INSTANCE.languageName, translatedMsg -> {
//                List<String> translatedSplitted = Stream.of(StringUtils.wrapText(TranslationManager.TRANSLATED_PREFIX + translatedMsg, 200)).collect(Collectors.toList());
//                if (TranslationConfig.INSTANCE.keepOriginal) {
//                    splittedDescription.addAll(translatedSplitted);
//                } else {
//                    splittedDescription = translatedSplitted;
//                }
//            });
//        }
    }

    public String getName() {
        return name;
    }

    public Map<QuestLevelType, Integer> getMinLevel() {
        return minLevels;
    }

    public List<ITextComponent> getLore() {
        return lore;
    }

    public QuestSize getSize() {
        return size;
    }

    public List<String> getSplittedDescription() {
        return splittedDescription;
    }

    public QuestStatus getStatus() {
        return status;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public Vector3d getTargetLocation() {
        return targetLocation;
    }

    public ItemStack getOriginalStack() {
        return originalStack;
    }

    public boolean hasTargetLocation() {
        return targetLocation != null;
    }

    public boolean isMiniQuest() {
        return isMiniQuest;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean equals(ItemStack stack) {
        return ItemUtils.getStringLore(originalStack).equals(ItemUtils.getStringLore(stack));
    }

    public void setAsCompleted() {
        status = QuestStatus.COMPLETED;

        lore.clear();
        lore.add(new StringTextComponent(name)
                .withStyle(TextFormatting.BOLD));
        lore.add(new StringTextComponent("Completed!")
                .withStyle(TextFormatting.GREEN));
        lore.add(new StringTextComponent(" "));
        for (Map.Entry<QuestLevelType, Integer> levels : minLevels.entrySet()) {
            lore.add(new StringTextComponent("✔ ")
                    .withStyle(TextFormatting.GREEN)
                    .append(new StringTextComponent(levels.getKey().name().toLowerCase() + " Lv. Min: ")
                            .withStyle(TextFormatting.GRAY))
                    .append(new StringTextComponent(levels.getValue().toString())
                            .withStyle(TextFormatting.WHITE)));
        }
        lore.add(new StringTextComponent("- ")
                .withStyle(TextFormatting.GREEN)
                .append(new StringTextComponent("Length: ")
                        .withStyle(TextFormatting.GRAY))
                .append(new StringTextComponent(StringUtils.capitalizeFirst(size.name().toLowerCase()))
                        .withStyle(TextFormatting.WHITE)));
    }

    public void updateAsTracked() {
        if (!hasTargetLocation() || !QuestBookConfig.INSTANCE.compassFollowQuests) return;

        CompassManager.setCompassLocation(getTargetLocation());
    }

    @Override
    public String toString() {
        return name + ":" + minLevels + ":" + size.toString() + ":" + status.toString() + ":" + description;
    }

}
