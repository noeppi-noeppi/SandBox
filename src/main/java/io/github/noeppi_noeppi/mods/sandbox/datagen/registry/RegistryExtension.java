package io.github.noeppi_noeppi.mods.sandbox.datagen.registry;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * A registry is said to extend another registries if their elements are looked up by the id of elements in
 * another registry. This is a helper class for {@link WorldGenRegistries}.
 */
public class RegistryExtension<T, E> {
    
    public final ResourceKey<? extends Registry<T>> registry;
    public final ResourceKey<? extends Registry<E>> extendedRegistry;
    private final Map<T, Holder<E>> extendedElements;

    public RegistryExtension(ResourceKey<? extends Registry<T>> registry, ResourceKey<? extends Registry<E>> extendedRegistry) {
        this.registry = registry;
        this.extendedRegistry = extendedRegistry;
        this.extendedElements = new HashMap<>();
    }
    
    public void extendsWith(T element, Holder<E> extendedFrom) {
        if (this.extendedElements.containsKey(element)) {
            throw new IllegalArgumentException("Element was assigned an extension source twice: " + element);
        } else {
            this.extendedElements.put(element, extendedFrom);
        }
    }
    
    public boolean hasElement(T element) {
        return this.extendedElements.containsKey(element);
    }
    
    public void loadIds(WritableRegistry<T> registry, Registry<E> sourceRegistry) {
        for (Map.Entry<T, Holder<E>> entry : this.extendedElements.entrySet()) {
            T element = entry.getKey();
            Holder<E> extendedFrom = entry.getValue();
            ResourceLocation id = switch (extendedFrom.kind()) {
                case DIRECT -> sourceRegistry.getKey(extendedFrom.value());
                case REFERENCE -> ((Holder.Reference<E>) extendedFrom).key().location();
            };
            if (id == null) {
                throw new IllegalStateException("Can't assign id to extended registry element: Source not present in registry: " + extendedFrom);
            }
            ResourceKey<T> key = ResourceKey.create(registry.key(),id);
            if (registry.getKey(element) != null) {
                throw new IllegalStateException("Can't assign id to extended registry element: Already registered as " + registry.getKey(element) + ": " + element);
            }
            registry.register(key, element, Lifecycle.stable());
        }
    }
}
