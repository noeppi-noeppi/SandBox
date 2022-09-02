package io.github.noeppi_noeppi.mods.sandbox.datagen.ext;

import io.github.noeppi_noeppi.mods.sandbox.datagen.ext.base.BaseWorldGenData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Provider for {@link ConfiguredFeature configured features}, {@link ConfiguredWorldCarver configured carvers} and
 * {@link PlacedFeature feature placements}.
 */
public class FeatureData extends BaseWorldGenData {

    private final List<Holder<ConfiguredFeature<?, ?>>> features;
    private final List<Holder<ConfiguredWorldCarver<?>>> carvers;
    private final List<Holder<PlacedFeature>> placements;
    
    public FeatureData(Properties properties) {
        super(properties);
        this.features = new ArrayList<>();
        this.carvers = new ArrayList<>();
        this.placements = new ArrayList<>();
    }
    
    public Holder<ConfiguredFeature<?, ?>> feature(Feature<NoneFeatureConfiguration> feature) {
        return this.feature(feature, NoneFeatureConfiguration.INSTANCE);
    }
    
    public <C extends FeatureConfiguration> Holder<ConfiguredFeature<?, ?>> feature(Feature<C> feature, C config) {
        return this.addToList(this.features, this.registries.holder(Registry.CONFIGURED_FEATURE_REGISTRY, new ConfiguredFeature<>(feature, config)));
    }

    public <C extends CarverConfiguration> Holder<ConfiguredWorldCarver<?>> carver(WorldCarver<C> carver, C config) {
        return this.addToList(this.carvers, this.registries.holder(Registry.CONFIGURED_CARVER_REGISTRY, new ConfiguredWorldCarver<>(carver, config)));
    }

    public PlacementBuilder placement(ResourceKey<ConfiguredFeature<?, ?>> feature) {
        return new PlacementBuilder(this.registries.holder(Registry.CONFIGURED_FEATURE_REGISTRY, feature.location()));
    }
    
    public PlacementBuilder placement(Holder<ConfiguredFeature<?, ?>> feature) {
        return new PlacementBuilder(this.registries.holder(Registry.CONFIGURED_FEATURE_REGISTRY, feature));
    }
    
    public ModifierBuilder modifiers() {
        return new ModifierBuilder();
    }

    @Override
    public List<Result<?>> results() {
        return List.of(
                this.createResult(Registry.CONFIGURED_FEATURE_REGISTRY, ConfiguredFeature.DIRECT_CODEC, this.features),
                this.createResult(Registry.CONFIGURED_CARVER_REGISTRY, ConfiguredWorldCarver.DIRECT_CODEC, this.carvers),
                this.createResult(Registry.PLACED_FEATURE_REGISTRY, PlacedFeature.DIRECT_CODEC, this.placements)
        );
    }

    public abstract static class AnyPlacementBuilder<T> {
        
        protected final List<PlacementModifier> modifiers;

        private AnyPlacementBuilder() {
            this.modifiers = new ArrayList<>();
        }
        
        public AnyPlacementBuilder<T> count(int count) {
            return this.count(ConstantInt.of(count));
        }
        
        public AnyPlacementBuilder<T> count(int min, int max) {
            return this.count(UniformInt.of(min, max));
        }
        
        public AnyPlacementBuilder<T> count(IntProvider count) {
            return this.add(CountPlacement.of(count));
        }
        
        public AnyPlacementBuilder<T> countExtra(int base, float chance, int extra) {
            return this.add(PlacementUtils.countExtra(base, chance, extra));
        }

        public AnyPlacementBuilder<T> rarity(int avgOnceEveryChunk) {
            return this.add(RarityFilter.onAverageOnceEvery(avgOnceEveryChunk));
        }
        
        public AnyPlacementBuilder<T> noiseCount(int noiseToCount, double factor, double offset) {
            return this.add(NoiseBasedCountPlacement.of(noiseToCount, factor, offset));
        }

        public AnyPlacementBuilder<T> noiseThresholdCount(double noiseLevel, int above, int below) {
            return this.add(NoiseThresholdCountPlacement.of(noiseLevel, above, below));
        }
        
        public AnyPlacementBuilder<T> spread() {
            return this.add(InSquarePlacement.spread());
        }
        
        public AnyPlacementBuilder<T> height(VerticalAnchor bottom, VerticalAnchor top) {
            return this.add(HeightRangePlacement.uniform(bottom, top));
        }
        
        public AnyPlacementBuilder<T> heightTriangle(VerticalAnchor bottom, VerticalAnchor top) {
            return this.add(HeightRangePlacement.triangle(bottom, top));
        }
        
        public AnyPlacementBuilder<T> heightmap(Heightmap.Types type) {
            return this.add(HeightmapPlacement.onHeightmap(type));
        }
        
        public AnyPlacementBuilder<T> biomeFilter() {
            return this.add(BiomeFilter.biome());
        }

        public AnyPlacementBuilder<T> validGround(TagKey<Block> tag) {
            return this.add(BlockPredicateFilter.forPredicate(BlockPredicate.allOf(
                    BlockPredicate.matchesTag(new Vec3i(0, -1, 0), tag),
                    BlockPredicate.matchesFluids(Fluids.EMPTY)
            )));
        }
        
        public AnyPlacementBuilder<T> validGround(Block block) {
            return this.validGround(block.defaultBlockState());
        }
        
        public AnyPlacementBuilder<T> validGround(BlockState state) {
            return this.add(BlockPredicateFilter.forPredicate(BlockPredicate.allOf(
                    BlockPredicate.wouldSurvive(state, BlockPos.ZERO),
                    BlockPredicate.matchesFluids(Fluids.EMPTY)
            )));
        }
        
        public AnyPlacementBuilder<T> waterDepth(int maxDepth) {
            return this.add(SurfaceWaterDepthFilter.forMaxDepth(maxDepth));
        }
        
        public AnyPlacementBuilder<T> inAir() {
            return this.add(PlacementUtils.isEmpty());
        }
        
        public AnyPlacementBuilder<T> add(PlacementModifiers modifiers) {
            return this.addAll(modifiers.modifiers);
        }
        
        public AnyPlacementBuilder<T> add(PlacementModifier... modifiers) {
            this.modifiers.addAll(Arrays.asList(modifiers));
            return this;
        }
        
        public AnyPlacementBuilder<T> addAll(Collection<PlacementModifier> modifiers) {
            this.modifiers.addAll(modifiers);
            return this;
        }
        
        public abstract T build();
    }
    
    public class PlacementBuilder extends AnyPlacementBuilder<Holder<PlacedFeature>> {

        private final Holder<ConfiguredFeature<?, ?>> feature;

        private PlacementBuilder(Holder<ConfiguredFeature<?, ?>> feature) {
            this.feature = feature;
        }

        @Override
        public Holder<PlacedFeature> build() {
            return FeatureData.this.addToList(FeatureData.this.placements, FeatureData.this.registries.holder(Registry.PLACED_FEATURE_REGISTRY, new PlacedFeature(this.feature, List.copyOf(this.modifiers))));
        }
    }
    
    public static class ModifierBuilder extends AnyPlacementBuilder<PlacementModifiers> {
        
        private ModifierBuilder() {
            
        }

        @Override
        public PlacementModifiers build() {
            return new PlacementModifiers(List.copyOf(this.modifiers));
        }
    }
    
    public static class PlacementModifiers {
        
        private final List<PlacementModifier> modifiers;

        public PlacementModifiers(List<PlacementModifier> modifiers) {
            this.modifiers = List.copyOf(modifiers);
        }
    }
}
