package com.gtnewhorizons.angelica.ao;

import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelBlockRendererCache {
    public float getShadeBrightness(FakeBlockState state, IBlockAccess level, BlockPos pos) {
        return state.isCollisionShapeFullBlock(level, pos) ? 0.2F : 1.0F;
    }

    public int getLightColor(FakeBlockState state, IBlockAccess level, BlockPos pos) {
        return level.getLightBrightnessForSkyBlocks(pos.x, pos.y, pos.z, 0);
    }
}
