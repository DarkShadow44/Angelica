package com.gtnewhorizons.angelica.ao;

import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.embeddedt.embeddium.impl.model.quad.properties.ModelQuadFacing;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexEncoder;

public class AOHelper {

    private static ForgeDirection facingToDirection(ModelQuadFacing facing) {
        return switch (facing) {
            case NEG_X -> ForgeDirection.WEST;
            case POS_X -> ForgeDirection.EAST;
            case POS_Y -> ForgeDirection.UP;
            case NEG_Y -> ForgeDirection.DOWN;
            case POS_Z -> ForgeDirection.SOUTH;
            case NEG_Z -> ForgeDirection.NORTH;
            case UNASSIGNED -> ForgeDirection.UNKNOWN;
        };
    }
    private static final ThreadLocal<EnhancedAoRenderStorage> AORenderer = ThreadLocal.withInitial(EnhancedAoRenderStorage::new);
    private static final ThreadLocal<BakedQuad> QUAD = ThreadLocal.withInitial(BakedQuad::new);


    public static void updateAO(ChunkVertexEncoder.Vertex[] celeritasVertices, IBlockAccess world, BlockPos pos, ModelQuadFacing facing) {
        var ao = AORenderer.get();
        var quad  = QUAD.get();
        quad.fill(celeritasVertices);
        ForgeDirection direction = facingToDirection(facing);

        ao.calculateShape(world, new FakeBlockState(world, pos), pos, quad.vertices, direction);
        ao.captureQuad(quad);
        ao.calculate(world, new FakeBlockState(world, pos), pos, direction, true);

        for (int i = 0; i < 4; i++) {
            celeritasVertices[i].light = ao.lightmap[i];
        }
    }
}
