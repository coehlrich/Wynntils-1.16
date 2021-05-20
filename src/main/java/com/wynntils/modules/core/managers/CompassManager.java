/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.managers;


import com.wynntils.McIf;
import com.wynntils.modules.core.events.ServerEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class CompassManager {

    private static Vector3d compassLocation = null;

    public static Vector3d getCompassLocation() {
        if (compassLocation != null)
            compassLocation = new Vector3d(0, McIf.player().getY(), 0);
        return compassLocation;
    }

    public static void setCompassLocation(Vector3d compassLocation) {
        CompassManager.compassLocation = compassLocation;

        McIf.world().getLevelData().setSpawn(new BlockPos(compassLocation), 0);
    }

    public static void reset() {
        compassLocation = null;

        if (McIf.world() != null) {
            McIf.world().getLevelData().setSpawn(ServerEvents.getCurrentSpawnPosition(), 0);
        }
    }

}
