/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.managers;


import com.wynntils.core.utils.objects.Location;
import com.wynntils.modules.core.events.ServerEvents;
import net.minecraft.client.Minecraft;

public class CompassManager {

    private static Location compassLocation = null;

    public static Location getCompassLocation() {
        if (compassLocation != null) compassLocation.setY(Minecraft.getInstance().player.getY());
        return compassLocation;
    }

    public static void setCompassLocation(Location compassLocation) {
        CompassManager.compassLocation = compassLocation;

        Minecraft.getInstance().world.setSpawnPoint(compassLocation.toBlockPos());
    }

    public static void reset() {
        compassLocation = null;

        if (Minecraft.getInstance().world != null) {
            Minecraft.getInstance().world.setSpawnPoint(ServerEvents.getCurrentSpawnPosition());
        }
    }

}
