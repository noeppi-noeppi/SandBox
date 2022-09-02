package io.github.noeppi_noeppi.mods.sandbox.datagen.registry;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import io.github.noeppi_noeppi.mods.sandbox.impl.WorldGenRegistry;
import net.minecraft.core.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;

/**
 * World gen registries that should be used during data gen. This handles creation of holders by key and value
 * and binding them to correct names, so they can be used for serialisation.
 */
public class WorldGenRegistries {

    private final Map<ResourceKey<? extends Registry<?>>, WritableRegistry<?>> registries;
    private final Set<ResourceKey<? extends Registry<?>>> frozenRegistries;
    private final Map<ResourceKey<? extends Registry<?>>, Set<Holder.Reference<?>>> intrusiveHolders;
    private final Map<ResourceKey<? extends Registry<?>>, RegistryExtension<?, ?>> extensions;
    private final RegistryAccess registryAccess;
    private boolean frozen;

    public WorldGenRegistries() {
        this.registries = new HashMap<>();
        this.frozenRegistries = new HashSet<>();
        this.intrusiveHolders = new HashMap<>();
        this.extensions = new HashMap<>();
        this.registryAccess = new WorldGenRegistryAccess();
        this.frozen = false;
    }

    private static boolean isDatapackRegistry(ResourceKey<? extends Registry<?>> key) {
        //noinspection unchecked
        return !((Registry<Object>) (Registry<?>) Registry.REGISTRY).containsKey((ResourceKey<Object>) (ResourceKey<?>) key);
    }
    
    private <T> WritableRegistry<T> writableRegistry(ResourceKey<? extends Registry<T>> key) {
        if (!isDatapackRegistry(key)) {
            throw new IllegalArgumentException("Can't write to non-datapack registry: " + key);
        }
        //noinspection unchecked
        WritableRegistry<T> registry = (WritableRegistry<T>) this.registries.computeIfAbsent(key, k -> {
            WritableRegistry<T> newReg = new WorldGenRegistry<>(key, Lifecycle.stable(), null);
            // Add elements from builtin registry. This is required for density functions to allow
            // using builtin functions referenced by name.
            // Add elements from builtin registry
            RegistryAccess.BUILTIN.get().registry(key).ifPresent(builtin -> builtin.holders().forEach(builtinValue -> {
                if (builtinValue.isBound()) {
                    newReg.register(builtinValue.key(), builtinValue.value(), Lifecycle.stable());
                }
            }));
            return newReg;
        });
        if (this.frozen && !this.frozenRegistries.contains(key)) {
            this.freeze(key, registry);
        }
        return registry;
    }

    private <T, E> RegistryExtension<T, E> extension(ResourceKey<? extends Registry<T>> registry, ResourceKey<? extends Registry<E>> extendedRegistry) {
        if (!this.extensions.containsKey(registry)) {
            RegistryExtension<T, E> ext = new RegistryExtension<>(registry, extendedRegistry);
            this.extensions.put(registry, ext);
            return ext;
        } else {
            //noinspection unchecked
            RegistryExtension<T, ?> ext = (RegistryExtension<T, ?>) this.extensions.get(registry);
            if (!Objects.equals(ext.extendedRegistry, extendedRegistry)) {
                throw new IllegalArgumentException("Can't initialise registry extension on " + registry + " with " + extendedRegistry + ", already extends " + ext.extendedRegistry);
            } else {
                //noinspection unchecked
                return (RegistryExtension<T, E>) ext;
            }
        }
    }
    
    /**
     * Gets a {@link Registry} by a given {@link ResourceKey registry key}.
     */
    public <T> Registry<T> registry(ResourceKey<? extends Registry<T>> key) {
        if (isDatapackRegistry(key)) {
            return this.writableRegistry(key);
        } else {
            //noinspection unchecked,RedundantCast
            return (Registry<T>) ((Registry<Registry<?>>) Registry.REGISTRY).get((ResourceKey<Registry<?>>) (ResourceKey<?>) key);
        }
    }
    
    /**
     * Assigns a key to an object in a registry. This is used to match ids of intrusive holders when serializing.
     */
    public <T> void register(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation id, T value) {
        this.writableRegistry(registryKey).register(ResourceKey.create(registryKey, id), value, Lifecycle.stable());
    }

    /**
     * Assigns an id to a registry element from the id of a (possibly not yet bound) {@link Holder} from another registry.
     * This initialises an {@link RegistryExtension extension relationship} between both registries. The element is added to
     * the registry on registry freeze.
     */
    public <T, E> void registerExtension(ResourceKey<? extends Registry<T>> registry, ResourceKey<? extends Registry<E>> sourceRegistry, T value, Holder<E> source) {
        this.extension(registry, sourceRegistry).extendsWith(value, source);
    }

    /**
     * Same as {@link #register(ResourceKey, ResourceLocation, Object)} but will only register the value, if the value
     * is not yet present in the registry nor set through an {@link RegistryExtension extension relationship}.
     */
    public <T> void assignId(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation id, T value) {
        if (this.registry(registryKey).getKey(value) != null) {
            return;
        }
        if (this.extensions.containsKey(registryKey)) {
            //noinspection unchecked
            RegistryExtension<T, ?> ext = (RegistryExtension<T, ?>) this.extensions.get(registryKey);
            if (ext.hasElement(value)) return;
        }
        this.register(registryKey, id, value);
    }
    
    /**
     * Assigns a key to a direct holder in a registry. This is used to match ids of intrusive holders when serializing.
     * If the given holder is a reference holder, this method does nothing.
     */
    public <T> void register(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation id, Holder<T> value) {
        switch (value.kind()) {
            case REFERENCE -> {}
            case DIRECT -> this.register(registryKey, id, value.value());
        }
    }
    
    /**
     * Creates a new reference holder from an id.
     */
    public <T> Holder<T> holder(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation id) {
        return this.registry(registryKey).getOrCreateHolderOrThrow(ResourceKey.create(registryKey, id));
    }
    
    /**
     * Creates a new holder set from a tag id.
     */
    public <T> HolderSet<T> tag(TagKey<T> key) {
        return this.registry(key.registry()).getOrCreateTag(key);
    }
    
    /**
     * Creates a new reference holder from an object. The returned holder is intrusive and will be bound, when the registry
     * is frozen in {@link #freeze(ResourceKey)}.
     */
    public <T> Holder<T> holder(ResourceKey<? extends Registry<T>> registryKey, T value) {
        if (this.frozenRegistries.contains(registryKey)) {
            throw new IllegalStateException("Registry is already frozen (trying to add value " + value + ")");
        }
        @SuppressWarnings("deprecation")
        Holder.Reference<T> holder = Holder.Reference.createIntrusive(this.registry(registryKey), value);
        this.intrusiveHolders.computeIfAbsent(registryKey, k -> new HashSet<>()).add(holder);
        return holder;
    }

    /**
     * Creates a new reference holder from another holder. Note that direct holders will be transformed into intrusive
     * holders by this, so they also need to be registered using {@link #register(ResourceKey, ResourceLocation, Holder)}.
     */
    public <T> Holder<T> holder(ResourceKey<? extends Registry<T>> registryKey, Holder<T> value) {
        return switch (value.kind()) {
            case DIRECT -> this.holder(registryKey, value.value());
            case REFERENCE -> {
                if (((Holder.Reference<T>) value).getType() == Holder.Reference.Type.STAND_ALONE || value.isBound()) {
                    yield this.holder(registryKey, ((Holder.Reference<T>) value).key().location());
                } else {
                    yield this.holder(registryKey, value.value());
                }
            }
        };
    }

    /**
     * Freezes all current registries and causes all new registries that re created to be frozen immediately.
     * 
     * @see #freeze(ResourceKey) 
     */
    public void freezeAll() {
        for (ResourceKey<? extends Registry<?>> key : this.registries.keySet()) {
            //noinspection unchecked
            this.freeze((ResourceKey<? extends Registry<Object>>) key);
        }
        this.frozen = true;
    }
    
    /**
     * Freezes a registry. After that, all holders that have been created through this {@link WorldGenRegistries} object
     * that belong to the given registry will be bound and ready for serialisation.
     * <p>
     * Note that this method may freeze additional registries, depending on the registries {@link RegistryExtension extension relationship}.
     */
    public <T> void freeze(ResourceKey<? extends Registry<T>> key) {
        this.freeze(key, this.writableRegistry(key));
    }
    
    private <T> void freeze(ResourceKey<? extends Registry<T>> key, WritableRegistry<T> registry) {
        if (!this.frozenRegistries.contains(key)) {
            if (this.extensions.containsKey(key)) {
                //noinspection unchecked
                RegistryExtension<T, Object> ext = (RegistryExtension<T, Object>) this.extensions.get(key);
                this.freeze(ext.extendedRegistry);
                ext.loadIds(registry, this.registry(ext.extendedRegistry));
            }
            if (this.intrusiveHolders.containsKey(key)) {
                //noinspection unchecked
                for (Holder.Reference<T> holder : (Set<Holder.Reference<T>>) (Set<?>) this.intrusiveHolders.get(key)) {
                    if (holder.getType() == Holder.Reference.Type.INTRUSIVE) {
                        Optional<ResourceKey<T>> elemKey = registry.getResourceKey(holder.value());
                        if (elemKey.isPresent()) {
                            holder.bind(elemKey.get(), holder.value());
                        } else {
                            throw new IllegalStateException("Element not added to registry: " + key + ": " + holder.value());
                        }
                    }
                    if (!holder.isBound()) {
                        throw new IllegalStateException("Unbound holder: " + key + ": " + holder);
                    }
                }
            }
            this.frozenRegistries.add(key);
            try {
                registry.freeze();
            } catch (IllegalStateException e) {
                // The registry complains about unbound values (foreign holder, like vanilla or other mods)
                // However, the frozen flag in the registry is set at this point and we don't care about unbounds.
            }
        }
    }

    /**
     * Gets a {@link RegistryAccess} for all <b>frozen</b> registries that this {@link WorldGenRegistries} hold.
     */
    public RegistryAccess registryAccess() {
        return this.registryAccess;
    }

    /**
     * Wraps the given {@link DynamicOps} so they can use the <b>frozen</b> registries from this  {@link WorldGenRegistries}
     * for serialisation. Decoding registry entries with these {@link DynamicOps} is not supported.
     */
    public <T> DynamicOps<T> dynamicOps(DynamicOps<T> ops) {
        return RegistryOps.create(ops, this.registryAccess());
    }
    
    private class WorldGenRegistryAccess implements RegistryAccess {

        @Nonnull
        @Override
        public <E> Optional<Registry<E>> ownedRegistry(@Nonnull ResourceKey<? extends Registry<? extends E>> key) {
            if (!isDatapackRegistry(key)) {
                return Optional.empty();
            } else if (WorldGenRegistries.this.frozen || WorldGenRegistries.this.frozenRegistries.contains(key)) {
                //noinspection unchecked
                return Optional.of((Registry<E>) WorldGenRegistries.this.registry((ResourceKey<? extends Registry<Object>>) key));
            } else {
                return Optional.empty();
            }
        }

        @Nonnull
        @Override
        public Stream<RegistryEntry<?>> ownedRegistries() {
            Set<ResourceKey<? extends Registry<?>>> keys = new HashSet<>(WorldGenRegistries.this.frozenRegistries);
            if (WorldGenRegistries.this.frozen) {
                for (RegistryData<?> reg : RegistryAccess.knownRegistries()) {
                    keys.add(reg.key());
                }
            }
            //noinspection unchecked
            return keys.stream().map(key -> this.entry((ResourceKey<? extends Registry<Object>>) key));
        }
        
        private <T> RegistryAccess.RegistryEntry<T> entry(ResourceKey<? extends Registry<T>> key) {
            return new RegistryEntry<>(key, WorldGenRegistries.this.registry(key));
        }
    }
}
