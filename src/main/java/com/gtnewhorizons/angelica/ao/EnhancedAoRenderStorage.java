/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.gtnewhorizons.angelica.ao;

import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Entrypoint and main class of our enhanced AO pipeline.
 *
 * <p>Vanilla's AO logic works well for faces that are axis-aligned.
 * That computation is replicated in {@link FullFaceCalculator}, with some bug fixes.
 * The job of the enhanced pipeline is to handle faces that are more complicated,
 * by combining multiple full faces as needed using interpolation.
 *
 * <p>Compared to vanilla, we also remove any assumption about vertex order in the quad.
 */
public class EnhancedAoRenderStorage extends AmbientOcclusionRenderStorage {

    /**
     * "Enhanced" flat shading logic.
     */
    public static void applyFlatQuadBrightness(IBlockAccess level, BakedQuad quad, AmbientOcclusionRenderStorage storage) {
        for (int vertex = 0; vertex < 4; ++vertex) {
            // Handle each vertex separately to apply vertex normals.

            int normal = ClientHooks.computeQuadNormal(quad.vertices());

            storage.brightness[vertex] = Level.getShade(
                normalComponent(normal, 0),
                normalComponent(normal, 1),
                normalComponent(normal, 2),
                quad.shade());
        }
    }

    /**
     * Cache these objects so that they don't need to be reallocated for every {@link EnhancedAoRenderStorage}.
     */
    private record AoObjectCache(FullFaceCalculator calculator, float[] weights) {}

    private static final ThreadLocal<AoObjectCache> AO_OBJECT_CACHE = ThreadLocal.withInitial(() -> new AoObjectCache(
        new FullFaceCalculator(),
        new float[4]));

    /**
     * Calculator for full faces.
     */
    private final FullFaceCalculator calculator;
    // Avoid repeated allocations of this array
    private final float[] weights;

    private BakedQuad currentQuad;

    protected static final ThreadLocal<ModelBlockRendererCache> CACHE = ThreadLocal.withInitial(ModelBlockRendererCache::new);

    public EnhancedAoRenderStorage() {
        var cache = AO_OBJECT_CACHE.get();
        this.calculator = cache.calculator;
        this.weights = cache.weights;
        // Reset AO Face cache

        this.calculator.startBlock(CACHE.get());
    }

    public void captureQuad(BakedQuad quad) {
        this.currentQuad = quad;
    }

    public void calculate(IBlockAccess level, FakeBlockState state, BlockPos pos, ForgeDirection direction, boolean shade) {
        if (this.currentQuad == null) {
            throw new IllegalStateException("Make sure to pass the quad via captureQuad before calling calculate.");
        }

        // Enhanced calculation
        // Vanilla uses ==. We could add an epsilon to use the cheaper axis-aligned logic for almost axis-aligned faces.
        boolean isAxisAligned = switch (direction) {
            case DOWN, UP -> faceShape[SizeInfo.DOWN.index] == faceShape[SizeInfo.UP.index];
            case NORTH, SOUTH -> faceShape[SizeInfo.NORTH.index] == faceShape[SizeInfo.SOUTH.index];
            case WEST, EAST -> faceShape[SizeInfo.WEST.index] == faceShape[SizeInfo.EAST.index];
            case UNKNOWN -> throw new RuntimeException();
        };

        if (isAxisAligned) {
            calculateAxisAligned(level, state, pos, direction, shade);
        } else {
            calculateIrregular(level, state, pos, shade);
        }
    }

    /**
     * Computes AO for an axis-aligned quad.
     *
     * <p>This is similar to vanilla in how we select whether to use the inside or outside light.
     * However, we still use our own interpolation logic which does not make any assumption about vertex winding order.
     */
    private void calculateAxisAligned(IBlockAccess level, FakeBlockState state, BlockPos pos, ForgeDirection direction, boolean shade) {
        // Same logic as vanilla: sample outside if the depth is small, or force outside if we are a full block.
        // This is already stored in the faceCubic field.
        var fullFace = this.calculator.calculateFace(level, state, pos, direction, shade, this.faceCubic);

        // Perform bilinear interpolation to map a full AO face to actual vertex brightness and lightmap.
        // This will work regardless of the vertex order or position
        AoFace aoFace = AoFace.fromDirection(direction);
        float[] vertices = this.currentQuad.vertices();
        float[] weights = this.weights;
        for (int vertex = 0; vertex < 4; ++vertex) {
            aoFace.computeCornerWeights(weights, vertices[3 * vertex], vertices[3 * vertex + 1], vertices[3 * vertex + 2]);
            brightness[vertex] = interpolateBrightness(fullFace, weights);
            lightmap[vertex] = interpolateLightmap(fullFace, weights);
        }
    }

    private static final float AO_EPS = 1e-4f;
    private static final float AVERAGE_WEIGHT = 0.75f;
    private static final float MAX_WEIGHT = 1 - AVERAGE_WEIGHT;

    /**
     * Computes AO for a general quad.
     * Projects onto each axis, computes the AO, then combines proportionally to the square of each normal component.
     */
    private void calculateIrregular(IBlockAccess level, FakeBlockState state, BlockPos pos, boolean shade) {
        float[] vertices = currentQuad.vertices();

        for (int vertex = 0; vertex < 4; ++vertex) {
            // Handle each vertex separately to apply vertex normals.

            int normal = ClientHooks.computeQuadNormal(vertices);

            float weightedBrightness = 0;
            int weightedLightmap = 0;
            float maxBrightness = 0;
            int maxLightmap = 0;

            for (int axis = 0; axis < 3; ++axis) {
                float normalComponent = normalComponent(normal, axis);
                if (normalComponent == 0) {
                    continue;
                }

                // Choose AO face based on normal sign
                ForgeDirection direction = switch (axis) {
                    case 0 -> normalComponent > 0 ? ForgeDirection.EAST : ForgeDirection.WEST;
                    case 1 -> normalComponent > 0 ? ForgeDirection.UP : ForgeDirection.DOWN;
                    case 2 -> normalComponent > 0 ? ForgeDirection.SOUTH : ForgeDirection.NORTH;
                    default -> throw new AssertionError();
                };

                // Compute full face
                AoFace aoFace = AoFace.fromDirection(direction);
                float depth = aoFace.computeDepth(vertices[3 * vertex], vertices[3 * vertex + 1], vertices[3 * vertex + 2]);
                // Same logic as vanilla: sample outside if the depth is small, or force outside if we are a full block.
                boolean sampleOutside = depth < AO_EPS || state.isCollisionShapeFullBlock(level, pos);
                AoCalculatedFace fullFace = this.calculator.calculateFace(level, state, pos, direction, shade, sampleOutside);

                // Perform bilinear interpolation to map full AO face to this vertex.
                float[] weights = this.weights;
                aoFace.computeCornerWeights(weights, vertices[3 * vertex], vertices[3 * vertex + 1], vertices[3 * vertex + 2]);
                float brightness = interpolateBrightness(fullFace, weights);
                int lightmap = interpolateLightmap(fullFace, weights);

                // Blend proportionally to the square of the normal component
                float axisWeight = normalComponent * normalComponent;
                weightedBrightness += brightness * axisWeight;
                weightedLightmap = lerpLightmap(weightedLightmap, 1, lightmap, axisWeight);

                // Also keep track of the max, which will be used later
                // to make sure the quad does not get too dark.
                maxBrightness = Math.max(maxBrightness, brightness);
                maxLightmap = maxLightmap(maxLightmap, lightmap);
            }

            // Do an average between the max and the weighted average.
            // Using only the weighted average looks a bit too dark.
            brightness[vertex] = Math.clamp(weightedBrightness * AVERAGE_WEIGHT + maxBrightness * MAX_WEIGHT, 0.0F, 1.0F);
            lightmap[vertex] = lerpLightmap(weightedLightmap, AVERAGE_WEIGHT, maxLightmap, MAX_WEIGHT);
        }
    }

    /**
     * Unpacks a normal component.
     */
    private static float normalComponent(int normal, int axis) {
        int encodedNormalComponent = (normal >> (axis * 8)) & 0xFF;
        // Casting to byte will cast to a signed int.
        // This is really important, otherwise negative values will lead to above 1.0 normal components.
        return ((byte) encodedNormalComponent) / 127.0f;
    }

    /**
     * Interpolates brightness from the 4 corners of a face.
     */
    private static float interpolateBrightness(AoCalculatedFace in, float[] weights) {
        return Math.clamp(in.brightness0 * weights[0] + in.brightness1 * weights[1] + in.brightness2 * weights[2] + in.brightness3 * weights[3], 0.0F, 1.0F);
    }

    /**
     * Interpolates lightmap from the 4 corners of a face.
     */
    private static int interpolateLightmap(AoCalculatedFace in, float[] weights) {
        return blend(in.lightmap0, in.lightmap1, in.lightmap2, in.lightmap3, weights[0], weights[1], weights[2], weights[3]);
    }

    /**
     * Interpolates two lightmaps linearly.
     */
    private static int lerpLightmap(int lightmap1, float w1, int lightmap2, float w2) {
        // Interpolate the two components separately
        int block1 = LightTexture.blockWithFraction(lightmap1);
        int block2 = LightTexture.blockWithFraction(lightmap2);
        int block = 0xFF & Math.round(block1 * w1 + block2 * w2);

        int sky1 = LightTexture.skyWithFraction(lightmap1);
        int sky2 = LightTexture.skyWithFraction(lightmap2);
        int sky = 0xFF & Math.round(sky1 * w1 + sky2 * w2);

        return LightTexture.packWithFraction(block, sky);
    }

    static int maxLightmap(int lightmap1, int lightmap2) {
        return LightTexture.packWithFraction(
            Math.max(LightTexture.blockWithFraction(lightmap1), LightTexture.blockWithFraction(lightmap2)),
            Math.max(LightTexture.skyWithFraction(lightmap1), LightTexture.skyWithFraction(lightmap2)));
    }
}

