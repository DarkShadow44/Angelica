package com.gtnewhorizons.angelica.ao;

import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class AmbientOcclusionRenderStorage {
    public final float[] brightness = new float[4];
    public final int[] lightmap = new int[4];
    protected boolean faceCubic;

    protected final float[] faceShape = new float[SizeInfo.COUNT];

    protected static int blend(int color1, int color2, int color3, int blockLight, float color1Weight, float color2Weight, float color3Weight, float blockLightWeight) {
        int i = (int)((float)(color1 >> 16 & 255) * color1Weight + (float)(color2 >> 16 & 255) * color2Weight + (float)(color3 >> 16 & 255) * color3Weight + (float)(blockLight >> 16 & 255) * blockLightWeight) & 255;
        int j = (int)((float)(color1 & 255) * color1Weight + (float)(color2 & 255) * color2Weight + (float)(color3 & 255) * color3Weight + (float)(blockLight & 255) * blockLightWeight) & 255;
        return i << 16 | j;
    }

    public void calculateShape(IBlockAccess level, FakeBlockState state, BlockPos pos, float[] vertices, ForgeDirection direction) {
        float f = 32.0F;
        float f1 = 32.0F;
        float f2 = 32.0F;
        float f3 = -32.0F;
        float f4 = -32.0F;
        float f5 = -32.0F;

        for(int i = 0; i < 4; ++i) {
            float f6 = vertices[i*3];
            float f7 = vertices[i*3 + 1];
            float f8 = vertices[i*3 + 2];
            f = Math.min(f, f6);
            f1 = Math.min(f1, f7);
            f2 = Math.min(f2, f8);
            f3 = Math.max(f3, f6);
            f4 = Math.max(f4, f7);
            f5 = Math.max(f5, f8);
        }

        faceShape[SizeInfo.WEST.index] = f;
        faceShape[SizeInfo.EAST.index] = f3;
        faceShape[SizeInfo.DOWN.index] = f1;
        faceShape[SizeInfo.UP.index] = f4;
        faceShape[SizeInfo.NORTH.index] = f2;
        faceShape[SizeInfo.SOUTH.index] = f5;
        faceShape[SizeInfo.FLIP_WEST.index] = 1.0F - f;
        faceShape[SizeInfo.FLIP_EAST.index] = 1.0F - f3;
        faceShape[SizeInfo.FLIP_DOWN.index] = 1.0F - f1;
        faceShape[SizeInfo.FLIP_UP.index] = 1.0F - f4;
        faceShape[SizeInfo.FLIP_NORTH.index] = 1.0F - f2;
        faceShape[SizeInfo.FLIP_SOUTH.index] = 1.0F - f5;

        float f9 = 1.0E-4F;
        float f10 = 0.9999F;
        boolean var10001;
        switch (direction) {
            case DOWN:
            case UP:
                var10001 = f >= 1.0E-4F || f2 >= 1.0E-4F || f3 <= 0.9999F || f5 <= 0.9999F;
                break;
            case NORTH:
            case SOUTH:
                var10001 = f >= 1.0E-4F || f1 >= 1.0E-4F || f3 <= 0.9999F || f4 <= 0.9999F;
                break;
            case WEST:
            case EAST:
                var10001 = f1 >= 1.0E-4F || f2 >= 1.0E-4F || f4 <= 0.9999F || f5 <= 0.9999F;
                break;
            default:
                throw new MatchException((String)null, (Throwable)null);
        }

        //renderStorage.facePartial = var10001;
        switch (direction) {
            case DOWN -> var10001 = f1 == f4 && (f1 < 1.0E-4F || state.isCollisionShapeFullBlock(level, pos));
            case UP -> var10001 = f1 == f4 && (f4 > 0.9999F || state.isCollisionShapeFullBlock(level, pos));
            case NORTH -> var10001 = f2 == f5 && (f2 < 1.0E-4F || state.isCollisionShapeFullBlock(level, pos));
            case SOUTH -> var10001 = f2 == f5 && (f5 > 0.9999F || state.isCollisionShapeFullBlock(level, pos));
            case WEST -> var10001 = f == f3 && (f < 1.0E-4F || state.isCollisionShapeFullBlock(level, pos));
            case EAST -> var10001 = f == f3 && (f3 > 0.9999F || state.isCollisionShapeFullBlock(level, pos));
            default -> throw new MatchException((String)null, (Throwable)null);
        }

        faceCubic = var10001;
    }
}
