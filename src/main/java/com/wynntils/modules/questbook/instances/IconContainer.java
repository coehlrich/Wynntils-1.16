/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.questbook.instances;

public enum IconContainer {

    questPageIcon(0, true),
    settingsPageIcon(1, true),
    itemGuideIcon(2, true),
    hudConfigIcon(3, true),
    discoveriesIcon(4, true),
    lootrunIcon(5, true);

    private int x;
    private boolean highlightVariant;

    IconContainer(int x, boolean highlightVariant) {
        this.x = x;
        this.highlightVariant = highlightVariant;
    }

    public int getX() {
        return x;
    }

    public boolean hasHighlight() {
        return this.highlightVariant;
    }
}
