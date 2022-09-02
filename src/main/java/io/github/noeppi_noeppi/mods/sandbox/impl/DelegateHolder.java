package io.github.noeppi_noeppi.mods.sandbox.impl;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

// Only for use, where the usage scope of the holder is known
public class DelegateHolder<T> implements Holder<T> {

    private Holder<T> holder;
    private final boolean forceDirect;
    
    public DelegateHolder(Holder<T> initial, boolean forceDirect) {
        this.holder = initial;
        this.forceDirect = forceDirect;
    }
    
    public void set(Holder<T> value) {
        this.holder = value;
    }
    
    @Nonnull
    @Override
    public T value() {
        return this.holder.value();
    }

    @Override
    public boolean isBound() {
        return this.forceDirect || this.holder.isBound();
    }

    @Override
    public boolean is(@Nonnull ResourceLocation id) {
        return !this.forceDirect && this.holder.is(id);
    }

    @Override
    public boolean is(@Nonnull ResourceKey<T> key) {
        return !this.forceDirect && this.holder.is(key);
    }

    @Override
    public boolean is(@Nonnull Predicate<ResourceKey<T>> predicate) {
        return !this.forceDirect && this.holder.is(predicate);
    }

    @Override
    public boolean is(@Nonnull TagKey<T> key) {
        return !this.forceDirect && this.holder.is(key);
    }

    @Nonnull
    @Override
    public Stream<TagKey<T>> tags() {
        return this.forceDirect ? Stream.empty() : this.holder.tags();
    }

    @Nonnull
    @Override
    public Either<ResourceKey<T>, T> unwrap() {
        return this.forceDirect ? Either.right(this.value()) : this.holder.unwrap();
    }

    @Nonnull
    @Override
    public Optional<ResourceKey<T>> unwrapKey() {
        return this.forceDirect ? Optional.empty() : this.holder.unwrapKey();
    }

    @Nonnull
    @Override
    public Kind kind() {
        return this.forceDirect ? Kind.DIRECT : this.holder.kind();
    }

    @Override
    public boolean isValidInRegistry(@Nonnull Registry<T> registry) {
        return this.forceDirect || this.holder.isValidInRegistry(registry);
    }
}
