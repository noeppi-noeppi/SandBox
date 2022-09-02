package io.github.noeppi_noeppi.mods.sandbox.datagen.ext.base;

import com.mojang.serialization.Codec;
import io.github.noeppi_noeppi.mods.sandbox.datagen.WorldGenProviderBase;
import io.github.noeppi_noeppi.mods.sandbox.datagen.registry.WorldGenRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiFunction;

/**
 * {@link WorldGenData} for entries that are loaded into a datapack registry at runtime.
 */
public abstract class BaseWorldGenData implements WorldGenData {

    protected final ModX mod;
    private final WorldGenProviderBase provider;
    protected final WorldGenRegistries registries;
    protected final ExistingFileHelper fileHelper;

    public BaseWorldGenData(Properties properties) {
        this.mod = properties.mod();
        this.provider = properties.provider();
        this.registries = properties.registries();
        this.fileHelper = properties.fileHelper();
    }

    /**
     * Gets a reference holder to an element in the given registry with the given id under this mods namespace.
     */
    protected final <A> Holder<A> holder(ResourceKey<? extends Registry<A>> key, String path) {
        return this.holder(key, this.mod.resource(path));
    }

    /**
     * Gets a reference holder to an element in the given registry with the given id.
     */
    protected final <A> Holder<A> holder(ResourceKey<? extends Registry<A>> key, String namespace, String path) {
        return this.holder(key, new ResourceLocation(namespace, path));
    }

    /**
     * Gets a reference holder to an element in the given registry with the given id.
     */
    protected final <A> Holder<A> holder(ResourceKey<? extends Registry<A>> key, ResourceLocation id) {
        return this.registries.holder(key, id);
    }

    /**
     * Gets a named holder set in the world gen registries from a given tag key.
     */
    protected final <A> HolderSet<A> tag(TagKey<A> key) {
        return this.registries.tag(key);
    }

    /**
     * Resolves some previously added data from the provider.
     */
    protected final <A extends WorldGenData> A resolve(Class<A> cls) {
        return this.provider.getData(cls);
    }
    
    /**
     * Gets a reference holder to an element for the given {@link ResourceKey}.
     */
    protected final <A> Holder<A> holder(ResourceKey<A> key) {
        return this.registries.<A>holder(ResourceKey.createRegistryKey(key.registry()), key.location());
    }

    protected final <T> Holder<T> addToList(List<Holder<T>> list, Holder<T> elem) {
        if (elem.kind() == Holder.Kind.DIRECT) {
            throw new IllegalArgumentException("Can't add direct holder to world gen data");
        }
        list.add(elem);
        return elem;
    }

    protected final <T> Result<T> createResult(ResourceKey<? extends Registry<T>> key, Codec<T> codec, List<Holder<T>> elements) {
        return this.createResult(key, codec, elements, null);
    }
    
    protected final <T> Result<T> createResult(ResourceKey<? extends Registry<T>> key, Codec<T> codec, List<Holder<T>> elements, @Nullable BiFunction<ResourceLocation, T, T> modify) {
        String dataPackPath = defaultDataPackPath(key);
        List<Holder<T>> theElements = List.copyOf(elements);
        return new Result<T>() {

            @Override
            public Codec<T> codec() {
                return codec;
            }

            @Override
            public List<Holder<T>> elements() {
                return theElements;
            }

            @Override
            public T modify(ResourceLocation id, T value) {
                return modify == null ? value : modify.apply(id, value);
            }

            @Override
            public Path getPath(Path base, ResourceLocation id) {
                return base.resolve(PackType.SERVER_DATA.getDirectory()).resolve(id.getNamespace())
                        .resolve(dataPackPath).resolve(id.getPath() + ".json");
            }
        };
    }

    private static String defaultDataPackPath(ResourceKey<? extends Registry<?>> key) {
        if (key.location().getNamespace().equals("minecraft")) {
            return key.location().getPath();
        } else {
            return key.location().getNamespace() +  "/"  + key.location().getPath();
        }
    }
}
