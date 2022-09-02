package io.github.noeppi_noeppi.mods.sandbox.datagen.ext;

import io.github.noeppi_noeppi.mods.sandbox.SandBox;
import io.github.noeppi_noeppi.mods.sandbox.datagen.ext.base.BaseWorldGenData;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.ArrayList;
import java.util.List;

public class NoiseData extends BaseWorldGenData {

    private final List<Holder<NoiseGeneratorSettings>> generatorSettings;
    private final List<Holder<NormalNoise.NoiseParameters>> noiseParamters;
    private final List<Holder<DensityFunction>> densityFunctions;
    
    public NoiseData(Properties properties) {
        super(properties);
        this.generatorSettings = new ArrayList<>();
        this.noiseParamters = new ArrayList<>();
        this.densityFunctions = new ArrayList<>();
    }
    
    public GeneratorSettingsBuilder generator() {
        return new GeneratorSettingsBuilder();
    }
    
    public Holder<NormalNoise.NoiseParameters> noise(int firstOctave, double... amplitudes) {
        return this.addToList(this.noiseParamters, this.registries.holder(Registry.NOISE_REGISTRY, new NormalNoise.NoiseParameters(firstOctave, DoubleList.of(amplitudes))));
    }
    
    public Holder<DensityFunction> density(DensityFunction function) {
        return this.addToList(this.densityFunctions, this.registries.holder(Registry.DENSITY_FUNCTION_REGISTRY, function));
    }

    @Override
    public List<Result<?>> results() {
        return List.of(
                this.createResult(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, NoiseGeneratorSettings.DIRECT_CODEC, this.generatorSettings),
                this.createResult(Registry.NOISE_REGISTRY, NormalNoise.NoiseParameters.DIRECT_CODEC, this.noiseParamters),
                this.createResult(Registry.DENSITY_FUNCTION_REGISTRY, DensityFunction.DIRECT_CODEC, this.densityFunctions)
        );
    }
    
    public class GeneratorSettingsBuilder {
        
        private NoiseSettings noise;
        private BlockState defaultBlock;
        private BlockState defaultFluid;
        private final RouterBuilder router;
        private SurfaceRules.RuleSource surface;
        private final List<Climate.ParameterPoint> spawnTargets;
        private final List<Climate.ParameterPoint> defaultSpawnTargets;
        private int seaLevel;
        private boolean disableMobGeneration;
        private boolean aquifersEnabled;
        private boolean oreVeinsEnabled;
        private boolean useLegacyRandomSource;
        
        private GeneratorSettingsBuilder() {
            NoiseGeneratorSettings defaultSettings = NoiseGeneratorSettings.overworld(false, false);
            this.noise = defaultSettings.noiseSettings();
            this.defaultBlock = Blocks.STONE.defaultBlockState();
            this.defaultFluid = Blocks.WATER.defaultBlockState();
            this.router = new RouterBuilder(defaultSettings.noiseRouter());
            this.surface = SandBox.emptySurface();
            this.spawnTargets = new ArrayList<>();
            this.defaultSpawnTargets = List.copyOf(defaultSettings.spawnTarget());
            this.seaLevel = 64;
            this.disableMobGeneration = false;
            this.aquifersEnabled = true;
            this.oreVeinsEnabled = true;
            this.useLegacyRandomSource = false;
        }
        
        public GeneratorSettingsBuilder noise(int minY, int height, int noiseSizeHorizontal, int noiseSizeVertical) {
            return this.noise(new NoiseSettings(minY, height, noiseSizeHorizontal, noiseSizeVertical));
        }
        
        public GeneratorSettingsBuilder noise(NoiseSettings noise) {
            this.noise = noise;
            return this;
        }

        public GeneratorSettingsBuilder defaultBlock(Block block) {
            return this.defaultBlock(block.defaultBlockState());
        }
        
        public GeneratorSettingsBuilder defaultBlock(BlockState state) {
            this.defaultBlock = state;
            return this;
        }

        public GeneratorSettingsBuilder defaultFluid(Block block) {
            return this.defaultFluid(block.defaultBlockState());
        }
        
        public GeneratorSettingsBuilder defaultFluid(BlockState state) {
            this.defaultFluid = state;
            return this;
        }
        
        public GeneratorSettingsBuilder router(NoiseRouter router) {
            this.router.fromRouter(router);
            return this;
        }
        
        public RouterBuilder router() {
            return this.router;
        }
        
        public GeneratorSettingsBuilder surface(SurfaceRules.RuleSource surface) {
            this.surface = surface;
            return this;
        }
        
        public GeneratorSettingsBuilder addSpawnTarget(Climate.ParameterPoint target) {
            this.spawnTargets.add(target);
            return this;
        }
        
        public GeneratorSettingsBuilder seaLevel(int seaLevel) {
            this.seaLevel = seaLevel;
            return this;
        }
        
        public GeneratorSettingsBuilder disableMobGeneration() {
            this.disableMobGeneration = true;
            return this;
        }
        
        public GeneratorSettingsBuilder disableAquifers() {
            this.aquifersEnabled = false;
            return this;
        }
        
        public GeneratorSettingsBuilder disableOreVeins() {
            this.oreVeinsEnabled = false;
            return this;
        }
        
        public GeneratorSettingsBuilder useLegacyRandomSource() {
            this.useLegacyRandomSource = true;
            return this;
        }
        
        public Holder<NoiseGeneratorSettings> build() {
            NoiseGeneratorSettings settings = new NoiseGeneratorSettings(
                    this.noise, this.defaultBlock, this.defaultFluid, this.router.build(), this.surface,
                    this.spawnTargets.isEmpty() ? this.defaultSpawnTargets : this.spawnTargets,
                    this.seaLevel, this.disableMobGeneration, this.aquifersEnabled, this.oreVeinsEnabled,
                    this.useLegacyRandomSource
            );
            return NoiseData.this.addToList(NoiseData.this.generatorSettings, NoiseData.this.registries.holder(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, settings));
        }

        public class RouterBuilder {

            private DensityFunction barrierNoise;
            private DensityFunction fluidLevelFloodednessNoise;
            private DensityFunction fluidLevelSpreadNoise;
            private DensityFunction lavaNoise;
            private DensityFunction temperature;
            private DensityFunction vegetation;
            private DensityFunction continents;
            private DensityFunction erosion;
            private DensityFunction depth;
            private DensityFunction ridges;
            private DensityFunction initialDensityWithoutJaggedness;
            private DensityFunction finalDensity;
            private DensityFunction veinToggle;
            private DensityFunction veinRidged;
            private DensityFunction veinGap;
            
            private RouterBuilder(NoiseRouter initial) {
                this.fromRouter(initial);
            }

            public GeneratorSettingsBuilder barrierNoise(Holder<DensityFunction> function) {
                return this.barrierNoise(new DensityFunctions.HolderHolder(NoiseData.this.registries.holder(Registry.DENSITY_FUNCTION_REGISTRY, function)));
            }
            
            public GeneratorSettingsBuilder barrierNoise(DensityFunction function) {
                this.barrierNoise = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder fluidLevelFloodednessNoise(Holder<DensityFunction> function) {
                return this.fluidLevelFloodednessNoise(new DensityFunctions.HolderHolder(NoiseData.this.registries.holder(Registry.DENSITY_FUNCTION_REGISTRY, function)));
            }
            
            public GeneratorSettingsBuilder fluidLevelFloodednessNoise(DensityFunction function) {
                this.fluidLevelFloodednessNoise = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder fluidLevelSpreadNoise(Holder<DensityFunction> function) {
                return this.fluidLevelSpreadNoise(new DensityFunctions.HolderHolder(NoiseData.this.registries.holder(Registry.DENSITY_FUNCTION_REGISTRY, function)));
            }
            
            public GeneratorSettingsBuilder fluidLevelSpreadNoise(DensityFunction function) {
                this.fluidLevelSpreadNoise = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder lavaNoise(Holder<DensityFunction> function) {
                return this.lavaNoise(new DensityFunctions.HolderHolder(NoiseData.this.registries.holder(Registry.DENSITY_FUNCTION_REGISTRY, function)));
            }
            
            public GeneratorSettingsBuilder lavaNoise(DensityFunction function) {
                this.lavaNoise = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder temperature(Holder<DensityFunction> function) {
                return this.temperature(new DensityFunctions.HolderHolder(NoiseData.this.registries.holder(Registry.DENSITY_FUNCTION_REGISTRY, function)));
            }
            
            public GeneratorSettingsBuilder temperature(DensityFunction function) {
                this.temperature = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder vegetation(Holder<DensityFunction> function) {
                return this.vegetation(new DensityFunctions.HolderHolder(NoiseData.this.registries.holder(Registry.DENSITY_FUNCTION_REGISTRY, function)));
            }
            
            public GeneratorSettingsBuilder vegetation(DensityFunction function) {
                this.vegetation = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder continents(Holder<DensityFunction> function) {
                return this.continents(new DensityFunctions.HolderHolder(NoiseData.this.registries.holder(Registry.DENSITY_FUNCTION_REGISTRY, function)));
            }
            
            public GeneratorSettingsBuilder continents(DensityFunction function) {
                this.continents = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder erosion(Holder<DensityFunction> function) {
                return this.erosion(new DensityFunctions.HolderHolder(NoiseData.this.registries.holder(Registry.DENSITY_FUNCTION_REGISTRY, function)));
            }
            
            public GeneratorSettingsBuilder erosion(DensityFunction function) {
                this.erosion = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder depth(Holder<DensityFunction> function) {
                return this.depth(new DensityFunctions.HolderHolder(NoiseData.this.registries.holder(Registry.DENSITY_FUNCTION_REGISTRY, function)));
            }
            
            public GeneratorSettingsBuilder depth(DensityFunction function) {
                this.depth = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder ridges(Holder<DensityFunction> function) {
                return this.ridges(new DensityFunctions.HolderHolder(NoiseData.this.registries.holder(Registry.DENSITY_FUNCTION_REGISTRY, function)));
            }
            
            public GeneratorSettingsBuilder ridges(DensityFunction function) {
                this.ridges = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder initialDensityWithoutJaggedness(Holder<DensityFunction> function) {
                return this.initialDensityWithoutJaggedness(new DensityFunctions.HolderHolder(NoiseData.this.registries.holder(Registry.DENSITY_FUNCTION_REGISTRY, function)));
            }
            
            public GeneratorSettingsBuilder initialDensityWithoutJaggedness(DensityFunction function) {
                this.initialDensityWithoutJaggedness = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder finalDensity(Holder<DensityFunction> function) {
                return this.finalDensity(new DensityFunctions.HolderHolder(NoiseData.this.registries.holder(Registry.DENSITY_FUNCTION_REGISTRY, function)));
            }
            
            public GeneratorSettingsBuilder finalDensity(DensityFunction function) {
                this.finalDensity = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder veinToggle(Holder<DensityFunction> function) {
                return this.veinToggle(new DensityFunctions.HolderHolder(NoiseData.this.registries.holder(Registry.DENSITY_FUNCTION_REGISTRY, function)));
            }
            
            public GeneratorSettingsBuilder veinToggle(DensityFunction function) {
                this.veinToggle = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder veinRidged(Holder<DensityFunction> function) {
                return this.veinRidged(new DensityFunctions.HolderHolder(NoiseData.this.registries.holder(Registry.DENSITY_FUNCTION_REGISTRY, function)));
            }
            
            public GeneratorSettingsBuilder veinRidged(DensityFunction function) {
                this.veinRidged = function;
                return GeneratorSettingsBuilder.this;
            }

            public GeneratorSettingsBuilder veinGap(Holder<DensityFunction> function) {
                return this.veinGap(new DensityFunctions.HolderHolder(NoiseData.this.registries.holder(Registry.DENSITY_FUNCTION_REGISTRY, function)));
            }
            
            public GeneratorSettingsBuilder veinGap(DensityFunction function) {
                this.veinGap = function;
                return GeneratorSettingsBuilder.this;
            }

            private void fromRouter(NoiseRouter router) {
                this.barrierNoise = router.barrierNoise();
                this.fluidLevelFloodednessNoise = router.fluidLevelFloodednessNoise();
                this.fluidLevelSpreadNoise = router.fluidLevelSpreadNoise();
                this.lavaNoise = router.lavaNoise();
                this.temperature = router.temperature();
                this.vegetation = router.vegetation();
                this.continents = router.continents();
                this.erosion = router.erosion();
                this.depth = router.depth();
                this.ridges = router.ridges();
                this.initialDensityWithoutJaggedness = router.initialDensityWithoutJaggedness();
                this.finalDensity = router.finalDensity();
                this.veinToggle = router.veinToggle();
                this.veinRidged = router.veinRidged();
                this.veinGap = router.veinGap();
            }

            private NoiseRouter build() {
                return new NoiseRouter(
                        this.wrap(this.barrierNoise),
                        this.wrap(this.fluidLevelFloodednessNoise),
                        this.wrap(this.fluidLevelSpreadNoise),
                        this.wrap(this.lavaNoise),
                        this.wrap(this.temperature),
                        this.wrap(this.vegetation),
                        this.wrap(this.continents),
                        this.wrap(this.erosion),
                        this.wrap(this.depth),
                        this.wrap(this.ridges),
                        this.wrap(this.initialDensityWithoutJaggedness),
                        this.wrap(this.finalDensity),
                        this.wrap(this.veinToggle),
                        this.wrap(this.veinRidged),
                        this.wrap(this.veinGap)
                );
            }
            
            private DensityFunction wrap(DensityFunction function) {
                if (function instanceof DensityFunctions.HolderHolder holder) {
                    // Density function holders are handled weirdly
                    return new DensityFunctions.HolderHolder(NoiseData.this.registries.holder(Registry.DENSITY_FUNCTION_REGISTRY, holder.function()));
                } else if (function instanceof DensityFunctions.Noise noiseFunc) {
                    // Needed to make the default functions work
                    //noinspection deprecation
                    return new DensityFunctions.Noise(
                            new DensityFunction.NoiseHolder(NoiseData.this.registries.holder(Registry.NOISE_REGISTRY, noiseFunc.noise().noiseData()), noiseFunc.noise().noise()),
                            noiseFunc.xzScale(), noiseFunc.yScale()
                    );
                } else {
                    return function;
                }
            }
        }
    }
}
