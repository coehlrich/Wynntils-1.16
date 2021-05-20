/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.visual.entities.conditions;

import com.wynntils.core.framework.entities.instances.FakeEntity;
import com.wynntils.core.framework.entities.interfaces.EntitySpawnCodition;
import com.wynntils.modules.visual.configs.VisualConfig;
import com.wynntils.modules.visual.entities.EntityFirefly;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.ObjectHolder;

import java.util.Random;

public class FireflySpawnCondition implements EntitySpawnCodition {

    private static final AxisAlignedBB LIGHT_FOREST = new AxisAlignedBB(-1304, 0, -5088, -560, 255, -4426);
    private static final AxisAlignedBB DARK_FOREST = new AxisAlignedBB(-1433, 0, -5613, -938, 255, -5099);

    @ObjectHolder("minecraft:forest")
    public static final Biome forest = null;

    @ObjectHolder("minecraft:swamp")
    public static final Biome swamp = null;

    @Override
    public boolean shouldSpawn(Vector3d pos, World world, ClientPlayerEntity player, Random random) {
        if (!VisualConfig.Fireflies.INSTANCE.enabled) return false;

        BlockPos block = new BlockPos(pos);
        GameRegistry.findRegistry(Biome.class);
        if (world.getBiome(block) != forest && world.getBiome(block) != swamp)
            return false;
        if (!LIGHT_FOREST.contains(pos) && !DARK_FOREST.contains(pos))
            return false;

        // Night starts at 12542 and ends at 23031
        long worldTime = world.getDayTime() % 24000;
        if (worldTime < 12542 || worldTime > 23031) return false;

        return EntityFirefly.fireflies.get() < VisualConfig.Fireflies.INSTANCE.spawnLimit
                && random.nextInt(VisualConfig.Fireflies.INSTANCE.spawnRate) == 0;
    }

    @Override
    public FakeEntity createEntity(Vector3d location, World world, ClientPlayerEntity player, Random random) {
        float r, g, b;
        if (world.getBiome(new BlockPos(location)) == swamp) {
            r = 0.29f; g = 0f; b = 0.5f; // dark firefly
        } else {
            r = 1f; g = 1f; b = 0f; // light firefly
        }

        return new EntityFirefly(location, r, g, b);
    }

}
