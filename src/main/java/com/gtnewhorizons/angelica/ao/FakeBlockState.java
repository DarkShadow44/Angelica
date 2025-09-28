package com.gtnewhorizons.angelica.ao;

import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;

public class FakeBlockState {
    Block block;
    public FakeBlockState(IBlockAccess level, BlockPos pos) {
        block = level.getBlock(pos.getX(), pos.getY(), pos.getZ());
    }

    public boolean isViewBlocking(IBlockAccess level, BlockPos pos) {
        return block.isOpaqueCube();
    }

    public int getLightBlock() {
        return block.getLightValue();
    }

    public Block getBlock() {
        return block;
    }

    public boolean isCollisionShapeFullBlock(IBlockAccess level, BlockPos pos) {
        return block.isNormalCube();
    }
}
