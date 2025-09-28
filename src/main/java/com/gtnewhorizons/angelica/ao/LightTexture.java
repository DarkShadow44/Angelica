package com.gtnewhorizons.angelica.ao;

public class LightTexture{
    public static int packWithFraction(int block, int sky) {
        return block | sky << 16;
    }

    public static int blockWithFraction(int light) {
        return light & 255;
    }

    public static int skyWithFraction(int light) {
        return light >>> 16 & 255;
    }
}

