package com.gtnewhorizons.angelica.ao;

import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;

public class FakeBlockState {
    public FakeBlockState(IBlockAccess level, BlockPos pos) {
    }

    public boolean isViewBlocking(IBlockAccess level, BlockPos pos) {
        return false;
    }

    public int getLightBlock() {
        return 0;
    }

    public Block getBlock() {
        return null;
    }

    public boolean isCollisionShapeFullBlock(IBlockAccess level, BlockPos pos) {
        return true;
    }
}
