package com.gtnewhorizons.angelica.ao;

import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
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
            case UNASSIGNED -> ForgeDirection.UP; // TODO
        };
    }
    private static final ThreadLocal<EnhancedAoRenderStorage> AORenderer = ThreadLocal.withInitial(EnhancedAoRenderStorage::new);
    private static final ThreadLocal<BakedQuad> QUAD = ThreadLocal.withInitial(BakedQuad::new);


    public static void updateAO(ChunkVertexEncoder.Vertex[] celeritasVertices, IBlockAccess world, BlockPos pos, ModelQuadFacing facing) {
        var ao = new EnhancedAoRenderStorage();
        var quad  = QUAD.get();
        quad.fill(celeritasVertices, pos);
        ForgeDirection direction = facingToDirection(facing);

        ao.calculateShape(world, new FakeBlockState(world, pos), pos, quad.vertices, direction);
        ao.captureQuad(quad);
        ao.calculate(world, new FakeBlockState(world, pos), pos, direction, true);

        Block block = world.getBlock(pos.x, pos.y, pos.z);

        for (int i = 0; i < 4; i++) {
            int l = block.colorMultiplier(world, pos.x, pos.y, pos.z);
            float r = (float)(l >> 16 & 255) / 255.0F;
            float g = (float)(l >> 8 & 255) / 255.0F;
            float b = (float)(l & 255) / 255.0F;
            int a = celeritasVertices[i].color & 0xFF000000;
            float brightness = ao.brightness[i];
            r *= brightness;
            g *= brightness;
            b *= brightness;

            int color = a |
                ((int)(r * 255) << 16) |
                ((int)(g * 255) << 8)  |
                ((int)(b * 255));

            celeritasVertices[i].color = color;
            celeritasVertices[i].light = ao.lightmap[i];
        }
    }
}
