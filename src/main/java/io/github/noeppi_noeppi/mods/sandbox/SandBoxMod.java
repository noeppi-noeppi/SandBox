package io.github.noeppi_noeppi.mods.sandbox;

import io.github.noeppi_noeppi.mods.sandbox.biome.BiomeLayer;
import io.github.noeppi_noeppi.mods.sandbox.biome.LayeredBiomeSource;
import io.github.noeppi_noeppi.mods.sandbox.gen.ExtendedNoiseGenerator;
import io.github.noeppi_noeppi.mods.sandbox.impl.EmptySurfaceRule;
import io.github.noeppi_noeppi.mods.sandbox.surface.BiomeSurface;
import io.github.noeppi_noeppi.mods.sandbox.surface.SurfaceRuleSet;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;
import org.moddingx.libx.mod.ModX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

@Mod("sandbox")
public final class SandBoxMod extends ModX {
    
    public static final Logger logger = LoggerFactory.getLogger("sandbox");
    
    private static SandBoxMod instance;
    
    public SandBoxMod() {
        instance = this;

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registries);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::register);
    }

    @Nonnull
    public static SandBoxMod getInstance() {
        return instance;
    }

    private void registries(NewRegistryEvent event) {
        event.create(new RegistryBuilder<BiomeLayer>()
                .setName(SandBox.BIOME_LAYER_REGISTRY.location())
                .dataPackRegistry(BiomeLayer.DIRECT_CODEC)
        );
        
        event.create(new RegistryBuilder<SurfaceRuleSet>()
                .setName(SandBox.SURFACE_RULE_SET_REGISTRY.location())
                .dataPackRegistry(SurfaceRuleSet.DIRECT_CODEC)
        );
        
        event.create(new RegistryBuilder<BiomeSurface>()
                .setName(SandBox.BIOME_SURFACE_REGISTRY.location())
                .dataPackRegistry(BiomeSurface.DIRECT_CODEC)
        );
    }
    
    @SuppressWarnings("deprecation")
    private void register(RegisterEvent event) {
        event.register(Registry.RULE_REGISTRY, this.resource("empty"), () -> EmptySurfaceRule.CODEC);

        event.register(Registry.CHUNK_GENERATOR_REGISTRY, this.resource("noise"), () -> ExtendedNoiseGenerator.CODEC);
        event.register(Registry.BIOME_SOURCE_REGISTRY, this.resource("layered"), () -> LayeredBiomeSource.CODEC);
        
        event.register(SandBox.BIOME_LAYER_REGISTRY, BiomeLayer.OVERWORLD.location(), () -> new BiomeLayer(MultiNoiseBiomeSource.Preset.OVERWORLD.parameterSource.apply(BuiltinRegistries.BIOME)));
        event.register(SandBox.BIOME_LAYER_REGISTRY, BiomeLayer.NETHER.location(), () -> new BiomeLayer(MultiNoiseBiomeSource.Preset.NETHER.parameterSource.apply(BuiltinRegistries.BIOME)));
    }
    
    @Override
    protected void setup(FMLCommonSetupEvent event) {
        
    }

    @Override
    protected void clientSetup(FMLClientSetupEvent event) {
        
    }
}
