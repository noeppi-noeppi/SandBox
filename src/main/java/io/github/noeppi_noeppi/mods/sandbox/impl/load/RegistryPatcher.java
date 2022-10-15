package io.github.noeppi_noeppi.mods.sandbox.impl.load;

import com.mojang.datafixers.util.Pair;
import io.github.noeppi_noeppi.mods.sandbox.SandBox;
import io.github.noeppi_noeppi.mods.sandbox.template.PoolExtension;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.Map;

public class RegistryPatcher {
    
    @Deprecated // Used by coremod
    public static void patchRegistries(Pair<Void, RegistryAccess.Frozen> pair) {
        patchRegistries(pair.getSecond());
    }
    
    public static void patchRegistries(RegistryAccess registries) {
        Registry<StructureTemplatePool> poolRegistry = registries.registry(Registry.TEMPLATE_POOL_REGISTRY).orElse(null);
        Registry<PoolExtension> extRegistry = registries.registry(SandBox.TEMPLATE_POOL_EXTENSION_REGISTRY).orElse(null);
        if (poolRegistry != null && extRegistry != null) {
            for (PoolExtension ext : extRegistry.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).toList()) {
                StructureTemplatePool pool = poolRegistry.getOptional(ext.poolId()).orElse(null);
                if (pool != null) {
                    for (Pair<StructurePoolElement, Integer> entry : ext.elements()) {
                        for (int i = 0; i < entry.getSecond(); i++) {
                            pool.templates.add(entry.getFirst());
                        }
                    }
                } else if (ext.required()) {
                    throw new IllegalStateException("Failed to apply template pool extension: " + extRegistry.getKey(ext));
                }
                
            }
        }
    }
}
