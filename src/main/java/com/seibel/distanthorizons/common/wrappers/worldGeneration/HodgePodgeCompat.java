
package com.seibel.distanthorizons.common.wrappers.worldGeneration;

import com.mitchej123.hodgepodge.SimulationDistanceHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;

public class HodgePodgeCompat {
    public static void preventChunkSimulation(World world, long packedChunkPos, boolean prevent) {
        SimulationDistanceHelper.preventChunkSimulation(world, packedChunkPos, prevent);
    }
}
