package com.gtnewhorizons.angelica.ao;

import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import net.minecraftforge.common.util.ForgeDirection;

public class MutableBlockPos extends BlockPos {
    public MutableBlockPos setWithOffset(BlockPos pos, ForgeDirection direction) {
        set(pos.x + direction.offsetX, pos.y + direction.offsetY, pos.z + direction.offsetZ);
        return this;
    }

    public MutableBlockPos move(ForgeDirection direction) {
        set(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
        return this;
    }
}
