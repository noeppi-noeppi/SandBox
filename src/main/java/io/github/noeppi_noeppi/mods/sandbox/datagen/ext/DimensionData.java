package io.github.noeppi_noeppi.mods.sandbox.datagen.ext;

import com.mojang.datafixers.util.Pair;
import io.github.noeppi_noeppi.mods.sandbox.SandBox;
import io.github.noeppi_noeppi.mods.sandbox.biome.BiomeLayer;
import io.github.noeppi_noeppi.mods.sandbox.biome.LayeredBiomeSource;
import io.github.noeppi_noeppi.mods.sandbox.datagen.ext.base.SingleWorldGenData;
import io.github.noeppi_noeppi.mods.sandbox.gen.ExtendedNoiseGenerator;
import io.github.noeppi_noeppi.mods.sandbox.surface.SurfaceRuleSet;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DimensionData extends SingleWorldGenData<LevelStem> {

    public DimensionData(Properties properties) {
        super(properties, Registry.LEVEL_STEM_REGISTRY, LevelStem.CODEC);
    }
    
    public BiomeSourceBuilder dimension(Holder<DimensionType> dimensionType) {
        return this.dimension(null, dimensionType);
    }
    
    public BiomeSourceBuilder dimension(ResourceKey<Level> key, Holder<DimensionType> dimensionType) {
        return new BiomeSourceBuilder(key, this.registries.holder(Registry.DIMENSION_TYPE_REGISTRY, dimensionType));
    }
    
    public Holder<LevelStem> dimension(Holder<DimensionType> dimensionType, ChunkGenerator generator) {
        return this.dimension(null, dimensionType, generator);
    }
    
    public Holder<LevelStem> dimension(ResourceKey<Level> key, Holder<DimensionType> dimensionType, ChunkGenerator generator) {
        LevelStem stem = new LevelStem(this.registries.holder(Registry.DIMENSION_TYPE_REGISTRY, dimensionType), generator);
        if (key != null) {
            this.registries.register(Registry.LEVEL_STEM_REGISTRY, key.location(), stem);
        }
        return this.addToList(this.registries.holder(Registry.LEVEL_STEM_REGISTRY, stem));
    }
    
    public class BiomeSourceBuilder {
        
        @Nullable
        private final ResourceKey<Level> key;
        private final Holder<DimensionType> dimensionType;
        
        private BiomeSourceBuilder(@Nullable ResourceKey<Level> key, Holder<DimensionType> dimensionType) {
            this.key = key;
            this.dimensionType = dimensionType;
        }
        
        public ChunkGeneratorBuilder fixedBiome(ResourceKey<Biome> biome) {
            return this.fixedBiome(DimensionData.this.holder(biome));
        }
        
        public ChunkGeneratorBuilder fixedBiome(Holder<Biome> biome) {
            return new ChunkGeneratorBuilder(this.key, this.dimensionType, new FixedBiomeSource(DimensionData.this.registries.holder(Registry.BIOME_REGISTRY, biome)));
        }
        
        public ChunkGeneratorBuilder multiNoiseBiome(Climate.ParameterList<Holder<Biome>> climate) {
            Climate.ParameterList<Holder<Biome>> cleanedClimate = new Climate.ParameterList<>(climate.values().stream()
                    .map(p -> Pair.of(p.getFirst(), DimensionData.this.registries.holder(Registry.BIOME_REGISTRY, p.getSecond())))
                    .toList()
            );
            return new ChunkGeneratorBuilder(this.key, this.dimensionType, new MultiNoiseBiomeSource(cleanedClimate));
        }
        
        public ChunkGeneratorBuilder layeredBiome(double horizontalScale, double verticalScale, TagKey<BiomeLayer> layers) {
            return new ChunkGeneratorBuilder(this.key, this.dimensionType, new LayeredBiomeSource(horizontalScale, verticalScale, DimensionData.this.tag(layers)));
        }

        @SafeVarargs
        public final ChunkGeneratorBuilder layeredBiome(double horizontalScale, double verticalScale, Holder<BiomeLayer>... layers) {
            return new ChunkGeneratorBuilder(this.key, this.dimensionType, new LayeredBiomeSource(horizontalScale, verticalScale, HolderSet.direct(layers)));
        }
    }
    
    public class ChunkGeneratorBuilder {
        
        @Nullable
        private final ResourceKey<Level> key;
        private final Holder<DimensionType> dimensionType;
        private final BiomeSource biomes;

        private ChunkGeneratorBuilder(@Nullable ResourceKey<Level> key, Holder<DimensionType> dimensionType, BiomeSource biomes) {
            this.key = key;
            this.dimensionType = dimensionType;
            this.biomes = biomes;
        }
        
        public FlatGeneratorBuilder flatGenerator() {
            return new FlatGeneratorBuilder(this.key, this.dimensionType, this.biomes);
        }
        
        public NoiseGeneratorBuilder noiseGenerator(Holder<NoiseGeneratorSettings> settings) {
            return new NoiseGeneratorBuilder(this.key, this.dimensionType, this.biomes, DimensionData.this.registries.holder(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, settings));
        }
    }
    
    public class FlatGeneratorBuilder {
        
        @Nullable
        private final ResourceKey<Level> key;
        private final Holder<DimensionType> dimensionType;
        private final List<Holder<StructureSet>> structures;
        private final List<FlatLayerInfo> layers;
        private final Holder<Biome> biome;
        private boolean lakes;
        private boolean decoration;

        private FlatGeneratorBuilder(@Nullable ResourceKey<Level> key, Holder<DimensionType> dimensionType, BiomeSource biomes) {
            this.key = key;
            this.dimensionType = dimensionType;
            this.structures = new ArrayList<>();
            if (biomes instanceof FixedBiomeSource source) {
                this.biome = DimensionData.this.registries.holder(Registry.BIOME_REGISTRY, source.biome);
            } else {
                throw new IllegalArgumentException("Flat generator can only be used with fixed biome source");
            }
            this.layers = new ArrayList<>();
            this.decoration = false;
        }

        public FlatGeneratorBuilder structures(Holder<StructureSet> structures) {
            this.structures.add(DimensionData.this.registries.holder(Registry.STRUCTURE_SET_REGISTRY, structures));
            return this;
        }

        public FlatGeneratorBuilder layer(Block block, int height) {
            if (height > 0) this.layers.add(new FlatLayerInfo(height, block));
            return this;
        }
        
        public FlatGeneratorBuilder withLakes() {
            this.lakes = true;
            return this;
        }
        
        public FlatGeneratorBuilder withDecoration() {
            this.decoration = true;
            return this;
        }

        public Holder<LevelStem> build() {
            if (this.layers.isEmpty()) this.layers.add(new FlatLayerInfo(1, Blocks.AIR));
            Optional<HolderSet<StructureSet>> structures = this.structures.isEmpty() ? Optional.empty() : Optional.of(HolderSet.direct(List.copyOf(this.structures)));
            FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(DimensionData.this.registries.registry(Registry.BIOME_REGISTRY), structures, List.copyOf(this.layers), this.lakes, this.decoration, Optional.of(this.biome));
            FlatLevelSource generator = new FlatLevelSource(DimensionData.this.registries.registry(Registry.STRUCTURE_SET_REGISTRY), settings);
            LevelStem stem =  new LevelStem(this.dimensionType, generator);
            if (this.key != null) {
                DimensionData.this.registries.register(Registry.LEVEL_STEM_REGISTRY, this.key.location(), stem);
            }
            return DimensionData.this.addToList(DimensionData.this.registries.holder(Registry.LEVEL_STEM_REGISTRY, stem));
        }
    }
    
    public class NoiseGeneratorBuilder {
        
        @Nullable
        private final ResourceKey<Level> key;
        private final Holder<DimensionType> dimensionType;
        private final BiomeSource biomes;
        private final Holder<NoiseGeneratorSettings> settings;
        
        @Nullable
        private Holder<SurfaceRuleSet> surfaceOverride;
        
        private NoiseGeneratorBuilder(@Nullable ResourceKey<Level> key, Holder<DimensionType> dimensionType, BiomeSource biomes, Holder<NoiseGeneratorSettings> settings) {
            this.key = key;
            this.dimensionType = dimensionType;
            this.biomes = biomes;
            this.settings = settings;
            this.surfaceOverride = null;
        }
        
        public NoiseGeneratorBuilder surfaceOverride(Holder<SurfaceRuleSet> surface) {
            this.surfaceOverride = DimensionData.this.registries.holder(SandBox.SURFACE_RULE_SET_REGISTRY, surface);
            return this;
        }
        
        public Holder<LevelStem> build() {
            NoiseBasedChunkGenerator generator;
            if (this.surfaceOverride != null) {
                generator = new ExtendedNoiseGenerator(
                        DimensionData.this.registries.registry(Registry.BIOME_REGISTRY),
                        DimensionData.this.registries.registry(SandBox.BIOME_SURFACE_REGISTRY),
                        DimensionData.this.registries.registry(Registry.STRUCTURE_SET_REGISTRY),
                        DimensionData.this.registries.registry(Registry.NOISE_REGISTRY),
                        this.biomes, this.settings, Optional.of(this.surfaceOverride)
                );
            } else {
                generator = new NoiseBasedChunkGenerator(
                        DimensionData.this.registries.registry(Registry.STRUCTURE_SET_REGISTRY),
                        DimensionData.this.registries.registry(Registry.NOISE_REGISTRY),
                        this.biomes, this.settings
                );
            }
            LevelStem stem = new LevelStem(this.dimensionType, generator);
            if (this.key != null) {
                DimensionData.this.registries.register(Registry.LEVEL_STEM_REGISTRY, this.key.location(), stem);
            }
            return DimensionData.this.addToList(DimensionData.this.registries.holder(Registry.LEVEL_STEM_REGISTRY, stem));
        }
    }
}
