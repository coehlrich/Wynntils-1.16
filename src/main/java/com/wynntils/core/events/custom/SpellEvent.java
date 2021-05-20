/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.events.custom;

import com.wynntils.modules.core.instances.TotemTracker;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.eventbus.api.Event;


public class SpellEvent extends Event {

    public static class Cast extends SpellEvent {

        private final String spell;
        private final int manaCost;

        public Cast(String spell, int manaCost) {
            this.spell = spell;
            this.manaCost = manaCost;
        }

        public String getSpell() {
            return spell;
        }

        public int getManaCost() {
            return manaCost;
        }
    }

    public static class Totem extends SpellEvent {
    }

    public static class TotemSummoned extends Totem {
    }

    public static class TotemActivated extends Totem {
        private final int time;
        private final Vector3d location;

        public TotemActivated(int time, Vector3d location) {
            this.time = time;
            this.location = location;
        }

        public int getTime() {
            return time;
        }

        public Vector3d getLocation() {
            return location;
        }
    }

    public static class TotemRenewed extends TotemActivated {
        public TotemRenewed(int time, Vector3d location) {
            super(time, location);
        }
    }

    public static class TotemRemoved extends Totem {
        private final boolean forcefullyRemoved;

        public TotemRemoved(boolean forcefullyRemoved) {
            this.forcefullyRemoved = forcefullyRemoved;
        }

        public boolean isForcefullyRemoved() {
            return forcefullyRemoved;
        }
    }

    public static class MobTotem extends SpellEvent {
        private final TotemTracker.MobTotem mobTotem;

        public MobTotem(TotemTracker.MobTotem mobTotem) {
            this.mobTotem = mobTotem;
        }

        public TotemTracker.MobTotem getMobTotem() {
            return mobTotem;
        }
    }

    public static class MobTotemActivated extends MobTotem {
        private final int time;

        public MobTotemActivated(TotemTracker.MobTotem mobTotem, int time) {
            super(mobTotem);
            this.time = time;
        }

        public int getTime() {
            return time;
        }
    }

    public static class MobTotemRemoved extends MobTotem {
        public MobTotemRemoved(TotemTracker.MobTotem mobTotem) {
            super(mobTotem);
        }
    }

}
