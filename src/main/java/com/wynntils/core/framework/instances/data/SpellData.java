/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.core.framework.instances.data;

import com.wynntils.McIf;
import com.wynntils.core.framework.instances.containers.PlayerData;
import com.wynntils.core.utils.reflections.ReflectionFields;
import net.minecraft.util.text.TextFormatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpellData extends PlayerData {

    private static final Pattern LEVEL_1_SPELL_PATTERN = Pattern.compile("^(Left|Right|\\?)-(Left|Right|\\?)-(Left|Right|\\?)$");
    private static final Pattern LOW_LEVEL_SPELL_PATTERN = Pattern.compile("^([LR?])-([LR?])-([LR?])$");
    private static final boolean[] NO_SPELL = new boolean[0];

    /** Represents `L` in the currently casting spell */
    public static final boolean SPELL_LEFT = false;
    /** Represents `R` in the currently casting spell */
    public static final boolean SPELL_RIGHT = true;

    private String lastParsedTitle = null;
    private boolean[] lastSpell = NO_SPELL;

    public SpellData() { }

    public boolean[] parseSpellFromTitle(String subtitle) {
        // Level 1: Left-Right-? in subtitle
        // Level 2-11: L-R-? in subtitle
        if (subtitle.equals(lastParsedTitle)) {
            return lastSpell;
        }

        lastParsedTitle = subtitle;
        if (subtitle.isEmpty()) {
            return (lastSpell = NO_SPELL);
        }

        CharacterData data = get(CharacterData.class);
        String right = data.getLevel() == 1 ? "Right" : "R";
        Matcher m = (data.getLevel() == 1 ? LEVEL_1_SPELL_PATTERN : LOW_LEVEL_SPELL_PATTERN).matcher(McIf.getTextWithoutFormattingCodes(subtitle));
        if (!m.matches() || m.group(1).equals("?")) return (lastSpell = NO_SPELL);

        boolean spell1 = m.group(1).equals(right) ? SPELL_RIGHT : SPELL_LEFT;
        if (m.group(2).equals("?")) return (lastSpell = new boolean[]{ spell1 });

        boolean spell2 = m.group(2).equals(right) ? SPELL_RIGHT : SPELL_LEFT;
        if (m.group(3).equals("?")) return (lastSpell = new boolean[]{ spell1, spell2 });

        boolean spell3 = m.group(3).equals(right) ? SPELL_RIGHT : SPELL_LEFT;
        return (lastSpell = new boolean[]{ spell1, spell2, spell3 });
    }

    /**
     * Return an array of the last spell in the action bar.
     * Each value will be {@link #SPELL_LEFT} or {@link #SPELL_RIGHT}.
     *
     * @return A boolean[] whose length is 0, 1, 2 or 3.
     */
    public boolean[] getLastSpell() {
        if (!get(CharacterData.class).isLoaded()) {
            return NO_SPELL;
        }

        int level = get(CharacterData.class).getLevel();
        if (level <= 11) {
            String subtitle = ReflectionFields.IngameGui_displayedSubTitle.getValue(McIf.mc().gui);
            return parseSpellFromTitle(subtitle);
        }

        return lastSpell;
    }

    public void setLastSpell(boolean[] lastSpell) {
        this.lastSpell = lastSpell;
    }

}
