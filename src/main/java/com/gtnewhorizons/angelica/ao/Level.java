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

    public static ForgeDirection getApproximateNearest(float x, float y, float z) {
        ForgeDirection direction = ForgeDirection.NORTH;
        float f = Float.MIN_VALUE;

        for(ForgeDirection direction1 : ForgeDirection.VALID_DIRECTIONS) {
            float f1 = x * (float)direction1.offsetX + y * (float)direction1.offsetY + z * (float)direction1.offsetZ;
            if (f1 > f) {
                f = f1;
                direction = direction1;
            }
        }

        return direction;
    }

    public static float getShade(float normalX, float normalY, float normalZ, boolean shade) {
        return getShade(getApproximateNearest(normalX, normalY, normalZ), shade);
    }
}
