/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.core.events.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.eventbus.api.Event;

public class LocationEvent extends Event {

    /**
     * Emitted when a static label is encountered in the map.
     */
    public static class LabelFoundEvent extends LocationEvent {
        private final ITextComponent label;
        private final Vector3d location;
        private final Entity entity;

        public LabelFoundEvent(ITextComponent label, Vector3d location, Entity entity) {
            this.label = label;
            this.location = location;
            this.entity = entity;
        }

        public ITextComponent getLabel() {
            return label;
        }

        public Vector3d getLocation() {
            return location;
        }

        /**
         * @return the entity this label is associated with
         */
        public Entity getEntity() {
            return entity;
        }
    }

    /**
     * Emitted when a labeled LivingEntity is encountered in the map.
     */
    public static class EntityLabelFoundEvent extends LocationEvent {
        private final ITextComponent label;
        private final Vector3d location;
        private final LivingEntity entity;

        public EntityLabelFoundEvent(ITextComponent label, Vector3d location, LivingEntity entity) {
            this.label = label;
            this.location = location;
            this.entity = entity;
        }

        public ITextComponent getLabel() {
            return label;
        }

        public Vector3d getLocation() {
            return location;
        }

        public LivingEntity getEntity() {
            return entity;
        }
    }
}
