package io.github.noeppi_noeppi.mods.sandbox.datagen.ext;

import io.github.noeppi_noeppi.mods.sandbox.SandBox;
import io.github.noeppi_noeppi.mods.sandbox.datagen.ext.base.BaseWorldGenData;
import io.github.noeppi_noeppi.mods.sandbox.surface.BiomeSurface;
import io.github.noeppi_noeppi.mods.sandbox.surface.SurfaceRuleSet;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.ArrayList;
import java.util.List;

public class SurfaceData extends BaseWorldGenData {
    
    private final List<Holder<SurfaceRuleSet>> ruleSets;
    private final List<Holder<BiomeSurface>> biomeSurfaces;
    
    public SurfaceData(Properties properties) {
        super(properties);
        this.ruleSets = new ArrayList<>();
        this.biomeSurfaces = new ArrayList<>();
    }
    
    public RuleSetBuilder ruleSet() {
        return this.ruleSet(false);
    }
    
    public RuleSetBuilder ruleSet(boolean defaultNoiseSurface) {
        return new RuleSetBuilder(defaultNoiseSurface);
    }
    
    public Holder<BiomeSurface> biome(ResourceKey<Biome> biome, SurfaceRules.RuleSource... rules) {
        return this.biome(this.holder(biome), rules);
    }
    
    public Holder<BiomeSurface> biome(Holder<Biome> biome, SurfaceRules.RuleSource... rules) {
        Holder<Biome> resolved = this.registries.holder(Registry.BIOME_REGISTRY, biome);
        BiomeSurface surface = new BiomeSurface(of(rules));
        this.registries.registerExtension(SandBox.BIOME_SURFACE_REGISTRY, Registry.BIOME_REGISTRY, surface, resolved);
        return this.addToList(this.biomeSurfaces, this.registries.holder(SandBox.BIOME_SURFACE_REGISTRY, surface));
    }
    
    private static SurfaceRules.RuleSource of(SurfaceRules.RuleSource[] rules) {
        if (rules.length == 0) {
            return SandBox.emptySurface();
        } else if (rules.length == 1) {
            return rules[0];
        } else {
            return SurfaceRules.sequence(rules);
        }
    }
    
    @Override
    public List<Result<?>> results() {
        return List.of(
                this.createResult(SandBox.SURFACE_RULE_SET_REGISTRY, SurfaceRuleSet.DIRECT_CODEC, this.ruleSets),
                this.createResult(SandBox.BIOME_SURFACE_REGISTRY, BiomeSurface.DIRECT_CODEC, this.biomeSurfaces)
        );
    }
    
    public class RuleSetBuilder {
        
        private final boolean defaultNoiseSurface;
        private SurfaceRules.RuleSource beforeBiomes;
        private SurfaceRules.RuleSource afterBiomes;
        private SurfaceRules.RuleSource defaultBiomeSurface;

        private RuleSetBuilder(boolean defaultNoiseSurface) {
            this.defaultNoiseSurface = defaultNoiseSurface;
            this.beforeBiomes = SandBox.emptySurface();
            this.afterBiomes = SandBox.emptySurface();
            this.defaultBiomeSurface = SandBox.emptySurface();
        }

        public RuleSetBuilder beforeBiomes(SurfaceRules.RuleSource... rules) {
            this.beforeBiomes = of(rules);
            return this;
        }

        public RuleSetBuilder afterBiomes(SurfaceRules.RuleSource... rules) {
            this.afterBiomes = of(rules);
            return this;
        }
        
        public RuleSetBuilder defaultBiomeSurface(SurfaceRules.RuleSource... rules) {
            this.defaultBiomeSurface = of(rules);
            return this;
        }
        
        public Holder<SurfaceRuleSet> build() {
            return SurfaceData.this.addToList(SurfaceData.this.ruleSets, SurfaceData.this.registries.holder(SandBox.SURFACE_RULE_SET_REGISTRY, new SurfaceRuleSet(this.defaultNoiseSurface, this.beforeBiomes, this.afterBiomes, this.defaultBiomeSurface)));
        }
    }
}
