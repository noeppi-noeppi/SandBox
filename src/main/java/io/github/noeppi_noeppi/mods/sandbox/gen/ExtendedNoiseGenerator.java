package io.github.noeppi_noeppi.mods.sandbox.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.noeppi_noeppi.mods.sandbox.SandBox;
import io.github.noeppi_noeppi.mods.sandbox.impl.DelegateHolder;
import io.github.noeppi_noeppi.mods.sandbox.surface.BiomeSurface;
import io.github.noeppi_noeppi.mods.sandbox.surface.SurfaceRuleSet;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ExtendedNoiseGenerator extends NoiseBasedChunkGenerator {

    public static final Codec<ExtendedNoiseGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(gen -> gen.biomeRegistry),
            RegistryOps.retrieveRegistry(SandBox.BIOME_SURFACE_REGISTRY).forGetter(gen -> gen.surfaceRegistry),
            RegistryOps.retrieveRegistry(Registry.STRUCTURE_SET_REGISTRY).forGetter(gen -> gen.structureSets),
            RegistryOps.retrieveRegistry(Registry.NOISE_REGISTRY).forGetter(gen -> gen.noiseRegistry),
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(gen -> gen.biomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(gen -> gen.actualSettings),
            SurfaceRuleSet.CODEC.optionalFieldOf("surface_override").forGetter(gen -> gen.surfaceOverride)
    ).apply(instance, ExtendedNoiseGenerator::new));

    private final Registry<Biome> biomeRegistry;
    private final Registry<BiomeSurface> surfaceRegistry;
    private final Registry<NormalNoise.NoiseParameters> noiseRegistry;
    private final Optional<Holder<SurfaceRuleSet>> surfaceOverride;
    private final Holder<NoiseGeneratorSettings> actualSettings;
    private final DelegateHolder<NoiseGeneratorSettings> fakeSettings;

    public ExtendedNoiseGenerator(Registry<Biome> biomeRegistry, Registry<BiomeSurface> surfaceRegistry, Registry<StructureSet> structureRegistry, Registry<NormalNoise.NoiseParameters> noiseRegistry, BiomeSource biomes, Holder<NoiseGeneratorSettings> settings, Optional<Holder<SurfaceRuleSet>> surfaceOverride) {
        this(biomeRegistry, surfaceRegistry, structureRegistry, noiseRegistry, biomes, settings, new DelegateHolder<>(settings, true), surfaceOverride);
    }
    
    private ExtendedNoiseGenerator(Registry<Biome> biomeRegistry, Registry<BiomeSurface> surfaceRegistry, Registry<StructureSet> structureRegistry, Registry<NormalNoise.NoiseParameters> noiseRegistry, BiomeSource biomes, Holder<NoiseGeneratorSettings> settings, DelegateHolder<NoiseGeneratorSettings> delegate, Optional<Holder<SurfaceRuleSet>> surfaceOverride) {
        super(structureRegistry, noiseRegistry, biomes, delegate);
        this.biomeRegistry = biomeRegistry;
        this.surfaceRegistry = surfaceRegistry;
        this.noiseRegistry = noiseRegistry;
        this.surfaceOverride = surfaceOverride;
        this.actualSettings = settings;
        this.fakeSettings = delegate;
    }

    public void init() {
        if (this.surfaceOverride.isPresent()) {
            NoiseGeneratorSettings settings = this.actualSettings.get();
            SurfaceRuleSet set = this.surfaceOverride.get().get();
            Set<Holder<Biome>> biomes = this.biomeSource.possibleBiomes();
            SurfaceRules.RuleSource surfaceRule = set.build(this.biomeRegistry, this.surfaceRegistry, biomes, this.settings.get());
            this.fakeSettings.set(Holder.direct(withSurface(settings, surfaceRule)));
        }
    }

    @Nonnull
    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Nonnull
    @Override
    public Holder<NoiseGeneratorSettings> generatorSettings() {
        return this.actualSettings;
    }

    @Override
    public boolean stable(@Nonnull ResourceKey<NoiseGeneratorSettings> settings) {
        return this.actualSettings.is(settings);
    }
    
    @SuppressWarnings("deprecation")
    private static NoiseGeneratorSettings withSurface(NoiseGeneratorSettings settings, SurfaceRules.RuleSource surfaceRule) {
        return new NoiseGeneratorSettings(
                 settings.noiseSettings(),
                 settings.defaultBlock(),
                 settings.defaultFluid(),
                 settings.noiseRouter(),
                 surfaceRule,
                 settings.spawnTarget(),
                 settings.seaLevel(),
                 settings.disableMobGeneration(),
                 settings.aquifersEnabled(),
                 settings.oreVeinsEnabled(),
                 settings.useLegacyRandomSource()
        );
    }
}
