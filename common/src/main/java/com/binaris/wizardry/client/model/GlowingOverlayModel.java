package com.binaris.wizardry.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GlowingOverlayModel implements BakedModel {
    public static final int FULL_BRIGHT = 0xF000F0;
    public static final int VERTEX_INT_SIZE = 8;
    public static final int LIGHTMAP_INDEX = 6;
    public static final int VERTICES_PER_QUAD = 4;

    private final BakedModel delegate;
    private final String suffix;

    public GlowingOverlayModel(BakedModel delegate, String suffix) {
        this.delegate = delegate;
        this.suffix = suffix;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource random) {
        List<BakedQuad> original = delegate.getQuads(state, side, random);
        if (state == null || original.isEmpty()) return original;

        return transformQuads(original, suffix);
    }

    private static List<BakedQuad> transformQuads(List<BakedQuad> oldQuads, String suffix) {
        List<BakedQuad> result = new ArrayList<>(oldQuads.size() * 2);
        for (BakedQuad quad : oldQuads) {
            result.add(quad);
            if (matchesSuffix(quad, suffix)) {
                result.add(makeFullBright(quad));
            }
        }
        return result;
    }

    public static boolean matchesSuffix(BakedQuad quad, String suffix) {
        return quad.getSprite().contents().name().getPath().endsWith(suffix);
    }

    private static BakedQuad makeFullBright(BakedQuad quad) {
        int[] vertices = quad.getVertices().clone();
        for (int i = 0; i < VERTICES_PER_QUAD; i++) {
            vertices[i * VERTEX_INT_SIZE + LIGHTMAP_INDEX] = FULL_BRIGHT;
        }
        return new BakedQuad(
                vertices,
                quad.getTintIndex(),
                quad.getDirection(),
                quad.getSprite(),
                false
        );
    }

    @Override
    public boolean useAmbientOcclusion() {
        return delegate.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return delegate.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return delegate.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return delegate.isCustomRenderer();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return delegate.getParticleIcon();
    }

    @Override
    public @NotNull ItemTransforms getTransforms() {
        return delegate.getTransforms();
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return delegate.getOverrides();
    }

    public BakedModel getDelegate() {
        return delegate;
    }

    public String getSuffix() {
        return suffix;
    }
}
