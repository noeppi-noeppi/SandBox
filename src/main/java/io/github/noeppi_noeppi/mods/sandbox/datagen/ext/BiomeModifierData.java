package io.github.noeppi_noeppi.mods.sandbox.datagen.ext;

import io.github.noeppi_noeppi.mods.sandbox.datagen.ext.base.SingleWorldGenData;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BiomeModifierData extends SingleWorldGenData<BiomeModifier> {

    public BiomeModifierData(Properties properties) {
        super(properties, ForgeRegistries.Keys.BIOME_MODIFIERS, BiomeModifier.DIRECT_CODEC);
    }
    
    public Holder<BiomeModifier> modifier(BiomeModifier modifier) {
        return this.addToList(this.registries.holder(ForgeRegistries.Keys.BIOME_MODIFIERS, modifier));
    }
    
    public FeaturesBuilder addFeatures(TagKey<Biome> biomes, GenerationStep.Decoration step) {
        return new FeaturesBuilder(this.tag(biomes), Set.of(step), false);
    }
    
    public FeaturesBuilder removeFeatures(TagKey<Biome> biomes, GenerationStep.Decoration... steps) {
        return new FeaturesBuilder(this.tag(biomes), Set.of(steps), false);
    }
    
    public AddMobSpawnsBuilder addSpawns(TagKey<Biome> biomes) {
        return new AddMobSpawnsBuilder(this.tag(biomes));
    }
    
    public RemoveMobSpawnsBuilder removeSpawns(TagKey<Biome> biomes) {
        return new RemoveMobSpawnsBuilder(this.tag(biomes));
    }
    
    public class FeaturesBuilder {
        
        private final HolderSet<Biome> biomes;
        private final Set<GenerationStep.Decoration> steps;
        private final boolean remove;
        private final List<Holder<PlacedFeature>> features;

        private FeaturesBuilder(HolderSet<Biome> biomes, Set<GenerationStep.Decoration> steps, boolean remove) {
            this.biomes = biomes;
            this.steps = steps;
            this.remove = remove;
            this.features = new ArrayList<>();
        }
        
        public FeaturesBuilder feature(Holder<PlacedFeature> feature) {
            this.features.add(BiomeModifierData.this.registries.holder(Registry.PLACED_FEATURE_REGISTRY, feature));
            return this;
        }
        
        public Holder<BiomeModifier> build() {
            BiomeModifier modifier;
            if (this.remove) {
                modifier = new ForgeBiomeModifiers.RemoveFeaturesBiomeModifier(this.biomes, HolderSet.direct(List.copyOf(this.features)), Set.copyOf(this.steps));
            } else {
                modifier = new ForgeBiomeModifiers.AddFeaturesBiomeModifier(this.biomes, HolderSet.direct(List.copyOf(this.features)), this.steps.iterator().next());
            }
            return BiomeModifierData.this.addToList(BiomeModifierData.this.registries.holder(ForgeRegistries.Keys.BIOME_MODIFIERS, modifier));
        }
    }

    public class AddMobSpawnsBuilder {

        private final HolderSet<Biome> biomes;
        private final List<MobSpawnSettings.SpawnerData> spawns;

        private AddMobSpawnsBuilder(HolderSet<Biome> biomes) {
            this.biomes = biomes;
            this.spawns = new ArrayList<>();
        }

        public AddMobSpawnsBuilder spawn(EntityType<?> type, int weight, int min, int max) {
            return this.spawn(new MobSpawnSettings.SpawnerData(type, weight, min, max));
        }
        
        public AddMobSpawnsBuilder spawn(MobSpawnSettings.SpawnerData spawn) {
            this.spawns.add(spawn);
            return this;
        }

        public Holder<BiomeModifier> build() {
            BiomeModifier modifier = new ForgeBiomeModifiers.AddSpawnsBiomeModifier(this.biomes, List.copyOf(this.spawns));
            return BiomeModifierData.this.addToList(BiomeModifierData.this.registries.holder(ForgeRegistries.Keys.BIOME_MODIFIERS, modifier));
        }
    }

    public class RemoveMobSpawnsBuilder {

        private final HolderSet<Biome> biomes;
        private final List<Holder<EntityType<?>>> entities;

        private RemoveMobSpawnsBuilder(HolderSet<Biome> biomes) {
            this.biomes = biomes;
            this.entities = new ArrayList<>();
        }

        public RemoveMobSpawnsBuilder entity(EntityType<?> type) {
            this.entities.add(BiomeModifierData.this.holder(ForgeRegistries.ENTITY_TYPES.getResourceKey(type).orElseThrow()));
            return this;
        }

        public Holder<BiomeModifier> build() {
            BiomeModifier modifier = new ForgeBiomeModifiers.RemoveSpawnsBiomeModifier(this.biomes, HolderSet.direct(List.copyOf(this.entities)));
            return BiomeModifierData.this.addToList(BiomeModifierData.this.registries.holder(ForgeRegistries.Keys.BIOME_MODIFIERS, modifier));
        }
    }
}
