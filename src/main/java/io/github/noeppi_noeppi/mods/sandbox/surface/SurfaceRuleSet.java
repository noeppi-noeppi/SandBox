package io.github.noeppi_noeppi.mods.sandbox.surface;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.noeppi_noeppi.mods.sandbox.SandBox;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public record SurfaceRuleSet(boolean defaultNoiseSurface, SurfaceRules.RuleSource beforeBiomes, SurfaceRules.RuleSource afterBiomes, SurfaceRules.RuleSource defaultBiomeSurface) {

    public static final Codec<SurfaceRuleSet> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("default_noise_surface").orElse(false).forGetter(SurfaceRuleSet::defaultNoiseSurface),
            SurfaceRules.RuleSource.CODEC.fieldOf("before_biomes").forGetter(SurfaceRuleSet::beforeBiomes),
            SurfaceRules.RuleSource.CODEC.fieldOf("after_biomes").forGetter(SurfaceRuleSet::afterBiomes),
            SurfaceRules.RuleSource.CODEC.fieldOf("default_biome_surface").forGetter(SurfaceRuleSet::defaultBiomeSurface)
    ).apply(instance, SurfaceRuleSet::new));

    public static final Codec<Holder<SurfaceRuleSet>> CODEC = RegistryFileCodec.create(SandBox.SURFACE_RULE_SET_REGISTRY, DIRECT_CODEC);

    public SurfaceRules.RuleSource build(Registry<Biome> biomeRegistry, Registry<BiomeSurface> surfaceRegistry, Set<Holder<Biome>> biomes, NoiseGeneratorSettings settings) {
        List<SurfaceRules.RuleSource> rules = new ArrayList<>();
        rules.add(this.beforeBiomes());
        ArrayList<ResourceKey<Biome>> biomesWithRules = new ArrayList<>(); 
        for (Holder<Biome> biome : biomes) {
            @Nullable
            ResourceLocation id = switch (biome.kind()) {
                case REFERENCE -> ((Holder.Reference<Biome>) biome).key().location();
                case DIRECT -> biomeRegistry.getKey(biome.value());
            };
            if (id != null) {
                BiomeSurface biomeSurface = surfaceRegistry.get(id);
                if (biomeSurface != null) {
                    ResourceKey<Biome> biomeKey = ResourceKey.create(Registry.BIOME_REGISTRY, id);
                    if (!biomesWithRules.contains(biomeKey)) {
                        biomesWithRules.add(biomeKey);
                        rules.add(SurfaceRules.ifTrue(
                                SurfaceRules.isBiome(biomeKey),
                                biomeSurface.rule()
                        ));
                    }
                }
            }
        }
        if (biomesWithRules.isEmpty()) {
            rules.add(this.defaultBiomeSurface());
        } else {
            //noinspection unchecked
            rules.add(SurfaceRules.ifTrue(
                    SurfaceRules.not(SurfaceRules.isBiome(biomesWithRules.toArray(ResourceKey[]::new))),
                    this.defaultBiomeSurface()
            ));
        }
        rules.add(this.afterBiomes());
        if (this.defaultNoiseSurface()) {
            rules.add(settings.surfaceRule());
        }
        return SurfaceRules.sequence(rules.toArray(SurfaceRules.RuleSource[]::new));
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
