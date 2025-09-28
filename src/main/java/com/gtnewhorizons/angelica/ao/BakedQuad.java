package com.gtnewhorizons.angelica.ao;

import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexEncoder;

public class BakedQuad {
    public final float[] vertices = new float[4 * 3];

    public void fill(ChunkVertexEncoder.Vertex[] celeritasVertices, BlockPos pos) {
        int offsetX = pos.x % 16;
        int offsetY = pos.y % 16;
        int offsetZ = pos.z % 16;

        if (offsetX < 0) offsetX += 16;
        if (offsetY < 0) offsetY += 16;
        if (offsetZ < 0) offsetZ += 16;
        for (int i = 0; i < 4; i++) {
            vertices[i*3] = celeritasVertices[i].x - offsetX;
            vertices[i*3 + 1] = celeritasVertices[i].y - offsetY;
            vertices[i*3 + 2] = celeritasVertices[i].z - offsetZ;
        }
    }

    public boolean shade() {
        return true;
    }

    public float[] vertices() {
        return vertices;
    }
}
