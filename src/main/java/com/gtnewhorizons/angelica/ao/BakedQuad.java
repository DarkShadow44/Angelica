package com.gtnewhorizons.angelica.ao;

import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexEncoder;

public class BakedQuad {
    public final float[] vertices = new float[4 * 3];

    public void fill(ChunkVertexEncoder.Vertex[] celeritasVertices) {
        for (int i = 0; i < 4; i++) {
            vertices[i*3] = celeritasVertices[i].x;
            vertices[i*3 + 1] = celeritasVertices[i].y;
            vertices[i*3 + 2] = celeritasVertices[i].z;
        }
    }

    public boolean shade() {
        return true;
    }

    public float[] vertices() {
        return vertices;
    }
}
