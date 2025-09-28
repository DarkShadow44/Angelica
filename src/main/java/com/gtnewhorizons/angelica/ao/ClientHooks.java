package com.gtnewhorizons.angelica.ao;

public class ClientHooks {
    public static int computeQuadNormal(float[] vertices) {
        float x0 = vertices[0];
        float y0 = vertices[1];
        float z0 = vertices[2];
        float x1 = vertices[3];
        float y1 = vertices[4];
        float z1 = vertices[5];
        float x2 = vertices[6];
        float y2 = vertices[7];
        float z2 = vertices[8];
        float x3 = vertices[9];
        float y3 = vertices[10];
        float z3 = vertices[11];
        float dx0 = x3 - x1;
        float dy0 = y3 - y1;
        float dz0 = z3 - z1;
        float dx1 = x2 - x0;
        float dy1 = y2 - y0;
        float dz1 = z2 - z0;
        float nx = dy1 * dz0 - dz1 * dy0;
        float ny = dz1 * dx0 - dx1 * dz0;
        float nz = dx1 * dy0 - dy1 * dx0;
        float length = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (length > 0.0F) {
            nx /= length;
            ny /= length;
            nz /= length;
        }

        int packedx = (byte)Math.round(nx * 127.0F) & 255;
        int packedy = (byte)Math.round(ny * 127.0F) & 255;
        int packedz = (byte)Math.round(nz * 127.0F) & 255;
        return packedx | packedy << 8 | packedz << 16;
    }
}
