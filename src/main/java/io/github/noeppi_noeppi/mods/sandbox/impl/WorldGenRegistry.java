package io.github.noeppi_noeppi.mods.sandbox.impl;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import javax.annotation.Nullable;
import java.util.function.Function;

// Marker class for coremod
public class WorldGenRegistry<T> extends MappedRegistry<T> {

    public WorldGenRegistry(ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle, @Nullable Function<T, Holder.Reference<T>> holderProvider) {
        super(key, lifecycle, holderProvider);
    }
    
    public static boolean forceValid(Holder.Reference<?> holder, Registry<?> registry) {
        // Because density functions don't use holders, we need to make them always valid in the worldgen registry set.
        if (holder.getType() == Holder.Reference.Type.INTRUSIVE && !holder.isBound()) return false;
        return registry.key() == Registry.NOISE_REGISTRY || registry.key() == Registry.DENSITY_FUNCTION_REGISTRY;
    }
}
