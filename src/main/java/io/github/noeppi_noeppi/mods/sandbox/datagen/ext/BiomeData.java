package io.github.noeppi_noeppi.mods.sandbox.datagen.ext;

import io.github.noeppi_noeppi.mods.sandbox.datagen.ext.base.SingleWorldGenData;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BiomeData extends SingleWorldGenData<Biome> {

    public BiomeData(Properties properties) {
        super(properties, Registry.BIOME_REGISTRY, Biome.DIRECT_CODEC);
    }
    
    public BiomeBuilder biome(float temperature, float downfall) {
        return this.biome(null, temperature, downfall);
    }
    
    public BiomeBuilder biome(ResourceKey<Biome> key, float temperature, float downfall) {
        return new BiomeBuilder(key, temperature, downfall);
    }
    
    public BiomeSpecialEffects.Builder effects() {
        return new BiomeEffectsBuilder();
    }
    
    public MobSpawnSettings.Builder spawns() {
        return new BiomeSpawnsBuilder();
    }
    
    public BiomeGenerationSettings.Builder generation() {
        return new BiomeGenerationBuilder();
    }
    
    // Custom builds as we need to build the holder in the end
    public class BiomeBuilder {

        @Nullable
        private final ResourceKey<Biome> key;
        private final float temperature;
        private final Biome.BiomeBuilder builder;

        private BiomeBuilder(@Nullable ResourceKey<Biome> key, float temperature, float downfall) {
            this.temperature = temperature;
            this.key = key;
            this.builder = new Biome.BiomeBuilder();
            this.builder.temperature(temperature);
            this.builder.downfall(downfall);
            this.builder.temperatureAdjustment(Biome.TemperatureModifier.NONE);
            this.builder.precipitation(downfall == 0 ? Biome.Precipitation.NONE : Biome.Precipitation.RAIN);
            this.effects(BiomeData.this.effects());
        }

        public BiomeBuilder precipitation(Biome.Precipitation precipitation) {
            this.builder.precipitation(precipitation);
            return this;
        }
        
        public BiomeBuilder frozen() {
            this.builder.temperatureAdjustment(Biome.TemperatureModifier.FROZEN);
            this.builder.precipitation(Biome.Precipitation.SNOW);
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public BiomeBuilder effects(BiomeSpecialEffects.Builder builder) {
            if (!(builder instanceof BiomeEffectsBuilder effectBuilder)) {
                throw new IllegalArgumentException("Use BiomeData#effects to create a BiomeSpecialEffects.Builder instance.");
            }
            effectBuilder.setDefaultSkyColor(this.temperature);
            this.builder.specialEffects(builder.build());
            return this;
        }

        public BiomeBuilder mobSpawns(MobSpawnSettings.Builder builder) {
            if (!(builder instanceof BiomeSpawnsBuilder)) {
                throw new IllegalArgumentException("Use BiomeData#spawns to create a MobSpawnSettings.Builder instance.");
            }
            this.builder.mobSpawnSettings(builder.build());
            return this;
        }

        public BiomeBuilder generation(BiomeGenerationSettings.Builder builder) {
            if (!(builder instanceof BiomeGenerationBuilder)) {
                throw new IllegalArgumentException("Use BiomeData#generation to create a BiomeGenerationSettings.Builder instance.");
            }
            this.builder.generationSettings(builder.build());
            return this;
        }
        
        public Holder<Biome> build() {
            Biome biome = this.builder.build();
            if (this.key != null) {
                BiomeData.this.registries.register(Registry.BIOME_REGISTRY, this.key.location(), biome);
            }
            return BiomeData.this.addToList(BiomeData.this.registries.holder(Registry.BIOME_REGISTRY, biome));
        }
    }
    
    // Custom implementations of sub-builders to wrap the holders into the worldgen registry set
    public static class BiomeEffectsBuilder extends BiomeSpecialEffects.Builder {
        
        private BiomeEffectsBuilder() {
            this.fogColor(0xc0d8ff);
            this.waterColor(0x3f76e4);
            this.waterFogColor(0x050533);
        }
        
        private void setDefaultSkyColor(float temperature) {
            if (this.skyColor.isEmpty()) {
                this.skyColor(OverworldBiomes.calculateSkyColor(temperature));
            }
        }
    }

    public static class BiomeSpawnsBuilder extends MobSpawnSettings.Builder {
        
        private BiomeSpawnsBuilder() {
            
        }
    }

    public class BiomeGenerationBuilder extends BiomeGenerationSettings.Builder {
        
        private BiomeGenerationBuilder() {
            
        }

        @Nonnull
        @Override
        public BiomeGenerationSettings.Builder addFeature(int step, @Nonnull Holder<PlacedFeature> feature) {
            return super.addFeature(step, BiomeData.this.registries.holder(Registry.PLACED_FEATURE_REGISTRY, feature));
        }

        @Nonnull
        @Override
        public BiomeGenerationSettings.Builder addCarver(@Nonnull GenerationStep.Carving step, @Nonnull Holder<? extends ConfiguredWorldCarver<?>> carver) {
            //noinspection unchecked
            return super.addCarver(step, BiomeData.this.registries.holder(Registry.CONFIGURED_CARVER_REGISTRY, (Holder<ConfiguredWorldCarver<?>>) carver));
        }
    }
}
