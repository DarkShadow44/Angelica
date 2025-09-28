package com.gtnewhorizons.angelica.ao;

import net.minecraftforge.common.util.ForgeDirection;

public class Level {
    public static float getShade(ForgeDirection direction, boolean shade) {
        boolean flag = false; // Only true in nether
        if (!shade) {
            return flag ? 0.9F : 1.0F;
        } else {
            switch (direction) {
                case DOWN:
                    return flag ? 0.9F : 0.5F;
                case UP:
                    return flag ? 0.9F : 1.0F;
                case NORTH:
                case SOUTH:
                    return 0.8F;
                case WEST:
                case EAST:
                    return 0.6F;
                default:
                    return 1.0F;
            }
        }
    }
}
