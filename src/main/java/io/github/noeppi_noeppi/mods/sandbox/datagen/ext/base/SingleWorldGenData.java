package io.github.noeppi_noeppi.mods.sandbox.datagen.ext.base;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * {@link WorldGenData} for entries that are loaded into a datapack registry at runtime.
 * Unlike {@link BaseWorldGenData}, this class only allows a single registry as target.
 */
public class SingleWorldGenData<T> extends BaseWorldGenData {
    
    private final ResourceKey<? extends Registry<T>> key;
    private final Codec<T> codec;
    private final BiFunction<ResourceLocation, T, T> modify;
    private final List<Holder<T>> elements;
    
    public SingleWorldGenData(Properties properties, ResourceKey<? extends Registry<T>> key, Codec<T> codec) {
        this(properties, key, codec, null);
    }
    
    public SingleWorldGenData(Properties properties, ResourceKey<? extends Registry<T>> key, Codec<T> codec, @Nullable BiFunction<ResourceLocation, T, T> modify) {
        super(properties);
        this.key = key;
        this.codec = codec;
        this.modify = modify;
        this.elements = new ArrayList<>();
    }
    
    protected final Holder<T> addToList(Holder<T> elem) {
        return this.addToList(this.elements, elem);
    }

    @Override
    public final List<Result<?>> results() {
        return List.of(this.createResult(this.key, this.codec, this.elements, this.modify));
    }
}
