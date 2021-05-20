/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.framework.entities.interfaces;

import com.wynntils.core.framework.entities.instances.FakeEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Random;

public interface EntitySpawnCodition {

    boolean shouldSpawn(Vector3d pos, World world, ClientPlayerEntity player, Random random);

    FakeEntity createEntity(Vector3d location, World world, ClientPlayerEntity player, Random random);

}
