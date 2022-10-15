package io.github.noeppi_noeppi.mods.sandbox;

import io.github.noeppi_noeppi.mods.sandbox.biome.BiomeLayer;
import io.github.noeppi_noeppi.mods.sandbox.impl.EmptySurfaceRule;
import io.github.noeppi_noeppi.mods.sandbox.surface.BiomeSurface;
import io.github.noeppi_noeppi.mods.sandbox.surface.SurfaceRuleSet;
import io.github.noeppi_noeppi.mods.sandbox.template.PoolExtension;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.SurfaceRules;

public class SandBox {
    
    public static final ResourceKey<? extends Registry<BiomeLayer>> BIOME_LAYER_REGISTRY = ResourceKey.createRegistryKey(SandBoxMod.getInstance().resource("biome_layer"));
    
    public static final ResourceKey<? extends Registry<SurfaceRuleSet>> SURFACE_RULE_SET_REGISTRY = ResourceKey.createRegistryKey(SandBoxMod.getInstance().resource("surface_rule_set"));
    public static final ResourceKey<? extends Registry<BiomeSurface>> BIOME_SURFACE_REGISTRY = ResourceKey.createRegistryKey(SandBoxMod.getInstance().resource("biome_surface"));
    
    public static final ResourceKey<? extends Registry<PoolExtension>> TEMPLATE_POOL_EXTENSION_REGISTRY = ResourceKey.createRegistryKey(SandBoxMod.getInstance().resource("template_pool_extension"));
    
    public static SurfaceRules.RuleSource emptySurface() {
        return EmptySurfaceRule.INSTANCE;
    }
}
