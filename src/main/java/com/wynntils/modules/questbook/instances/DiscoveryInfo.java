/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.questbook.instances;

import java.util.ArrayList;
import java.util.List;

import com.wynntils.McIf;
import com.wynntils.core.framework.instances.PlayerInfo;
import com.wynntils.core.framework.instances.data.CharacterData;
import com.wynntils.core.utils.ItemUtils;
import com.wynntils.core.utils.StringUtils;
import com.wynntils.modules.questbook.enums.DiscoveryType;
import com.wynntils.webapi.WebManager;
import com.wynntils.webapi.profiles.TerritoryProfile;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class DiscoveryInfo {

    private ItemStack originalStack;

    private String name;
    private DiscoveryType type;
    private List<ITextComponent> lore;
    private String description;
    private int minLevel;
    private TerritoryProfile guildTerritory = null;

    private String friendlyName;

    boolean valid = false;
    boolean discovered = false;

    public DiscoveryInfo(ItemStack originalStack, boolean discovered) {
        this.originalStack = originalStack;

        lore = ItemUtils.getLore(originalStack);

        // simple parameters
        name = originalStack.getDisplayName().getString();
        name = StringUtils.normalizeBadString(name.substring(0, name.length() - 1));
        minLevel = Integer.parseInt(lore.get(0).getString().replace("✔ Combat Lv. Min: ", ""));

        // type
        type = null;
        if (name.charAt(1) == 'e') type = DiscoveryType.WORLD;
        else if (name.charAt(1) == 'f') type = DiscoveryType.TERRITORY;
        else if (name.charAt(1) == 'b') type = DiscoveryType.SECRET;
        else return;

        // flat description
        StringBuilder descriptionBuilder = new StringBuilder();
        for (int x = 2; x < lore.size(); x++) {
            descriptionBuilder.append(lore.get(x).getString());
        }
        description = descriptionBuilder.toString();

        friendlyName = name.substring(4);
        if (friendlyName.length() > 22) {
            friendlyName = friendlyName.substring(0, 19);
            friendlyName += "...";
        }

        // Guild territory profile
        if (type == DiscoveryType.TERRITORY || type == DiscoveryType.WORLD) {
            String apiName = McIf.getTextWithoutFormattingCodes(name);
            guildTerritory = WebManager.getTerritories().get(apiName);
            if (guildTerritory == null) {
                guildTerritory = WebManager.getTerritories().get(apiName.replace('\'', '’'));
            }
        }

        lore.add(0, new StringTextComponent(this.name));
        this.discovered = discovered;
        valid = true;
    }

    public DiscoveryInfo(String name, DiscoveryType type, int minLevel, boolean discovered) {
        this.name = name;
        this.friendlyName = name;
        if (friendlyName.length() > 22) {
            friendlyName = friendlyName.substring(0, 19);
            friendlyName += "...";
        }

        this.lore = new ArrayList<>();
        lore.add(new StringTextComponent(this.name)
        		.withStyle(type.getColour(), TextFormatting.BOLD));
        boolean leveled = minLevel <= PlayerInfo.get(CharacterData.class).getLevel();
        lore.add(new StringTextComponent(leveled ? "✔" : "✖")
        		.withStyle(leveled ? TextFormatting.GREEN : TextFormatting.RED)
        		.append(new StringTextComponent(" Combat Lv. Min: " + minLevel)
        				.withStyle(TextFormatting.GRAY)));
        lore.add(StringTextComponent.EMPTY);

        this.minLevel = minLevel;
        this.type = type;

        // Guild territory profile
        if (type == DiscoveryType.TERRITORY || type == DiscoveryType.WORLD) {
            String apiName = McIf.getTextWithoutFormattingCodes(name);
            guildTerritory = WebManager.getTerritories().get(apiName);
            if (guildTerritory == null) {
                guildTerritory = WebManager.getTerritories().get(apiName.replace('\'', '’'));
            }
        }

        this.originalStack = ItemStack.EMPTY;
        this.discovered = discovered;
    }

    public String getName() {
        return name;
    }

    public DiscoveryType getType() {
        return type;
    }

    public List<ITextComponent> getLore() {
        return lore;
    }

    public String getDescription() {
        return description;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public ItemStack getOriginalStack() {
        return originalStack;
    }

    public TerritoryProfile getGuildTerritoryProfile() {
        return guildTerritory;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean wasDiscovered() {
        return discovered;
    }

}
