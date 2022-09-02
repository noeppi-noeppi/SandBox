package io.github.noeppi_noeppi.mods.sandbox.surface;

import com.mojang.serialization.Codec;
import io.github.noeppi_noeppi.mods.sandbox.SandBox;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record BiomeSurface(SurfaceRules.RuleSource rule) {
    
    public static final Codec<BiomeSurface> DIRECT_CODEC = SurfaceRules.RuleSource.CODEC.xmap(BiomeSurface::new, BiomeSurface::rule);

    public static final Codec<Holder<BiomeSurface>> CODEC = RegistryFileCodec.create(SandBox.BIOME_SURFACE_REGISTRY, DIRECT_CODEC);

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
