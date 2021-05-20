/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.visual.entities.conditions;

import com.wynntils.core.framework.entities.instances.FakeEntity;
import com.wynntils.core.framework.entities.interfaces.EntitySpawnCodition;
import com.wynntils.core.utils.objects.Location;
import com.wynntils.core.utils.objects.SquareRegion;
import com.wynntils.modules.visual.configs.VisualConfig;
import com.wynntils.modules.visual.entities.EntitySnowFlake;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ObjectHolder;

import java.util.Random;

public class SnowFlakesSpawnCondition implements EntitySpawnCodition {

    private static final SquareRegion NESAAK = new SquareRegion(-458, -976, 343, -635);
    private static final SquareRegion LUSUCO = new SquareRegion(-492, -550, -66, -127);

    @ObjectHolder("minecraft:taiga")
    public static final Biome taiga = null;

    @ObjectHolder("minecraft:plains")
    public static final Biome plains = null;

    @Override
    public boolean shouldSpawn(Location pos, World world, ClientPlayerEntity player, Random random) {
        if (!VisualConfig.Snowflakes.INSTANCE.enabled) return false;

        Biome biome = world.getBiome(pos.toBlockPos());

        // biome check
        // nesaak is TAIGA and lusuco is PLAINS
        boolean nesaak = NESAAK.isInside(pos);
        if (!nesaak && !LUSUCO.isInside(pos)) return false;
        if (biome != (nesaak ? taiga : plains))
            return false;

        // max distance
        double yDistance = Math.abs(pos.clone().subtract(new Location(player)).getY());
        if (yDistance < 3) return false;

        return EntitySnowFlake.snowflakes.get() < VisualConfig.Snowflakes.INSTANCE.spawnLimit
                && random.nextInt(VisualConfig.Snowflakes.INSTANCE.spawnRate) == 0;
    }

    @Override
    public FakeEntity createEntity(Location location, World world, ClientPlayerEntity player, Random random) {
        return new EntitySnowFlake(location, random);
    }

}
