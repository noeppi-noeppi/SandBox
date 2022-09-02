package io.github.noeppi_noeppi.mods.sandbox.impl.level;

import io.github.noeppi_noeppi.mods.sandbox.biome.LayeredBiomeSource;
import io.github.noeppi_noeppi.mods.sandbox.gen.ExtendedNoiseGenerator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class ChunkLevelInitializer {
    
    public static void initChunkGenerator(ChunkGenerator generator, MinecraftServer server) {
        long seed = server.getWorldData().worldGenSettings().seed();
        if (generator.getBiomeSource() instanceof LayeredBiomeSource layered) {
            layered.init(seed);
        }
        if (generator instanceof ExtendedNoiseGenerator gen) {
            gen.init();
        }
    }
}
