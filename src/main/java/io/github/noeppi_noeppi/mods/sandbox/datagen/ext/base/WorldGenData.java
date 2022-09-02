package io.github.noeppi_noeppi.mods.sandbox.datagen.ext.base;

import com.mojang.serialization.Codec;
import io.github.noeppi_noeppi.mods.sandbox.datagen.WorldGenProviderBase;
import io.github.noeppi_noeppi.mods.sandbox.datagen.registry.WorldGenRegistries;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.moddingx.libx.mod.ModX;

import java.nio.file.Path;
import java.util.List;

/**
 * Interface for logic of generating a specific type of world gen data in a {@link WorldGenProviderBase}.
 * The class should do all the registering in its constructor or in initializers for instance fields.
 * After that, {@link #results()} should yield a result with the data that has been generated.
 */
public interface WorldGenData {
    
    /**
     * Gets the results from the {@link WorldGenData}.
     */
    List<Result<?>> results();
    
    /**
     * Some properties that are provided in the constructor.
     */
    record Properties(ModX mod, WorldGenProviderBase provider, WorldGenRegistries registries, ExistingFileHelper fileHelper) {}

    /**
     * A generation result from a {@link WorldGenData} instance.
     */
    interface Result<T> {

        /**
         * The {@link Codec} that should be used to encode the elements.
         */
        Codec<T> codec();

        /**
         * A list of holders to generate. These may not be direct holders.
         *
         * @see WorldGenRegistries#holder(ResourceKey, Object)
         * @see WorldGenRegistries#holder(ResourceKey, ResourceLocation)
         * @see WorldGenRegistries#holder(ResourceKey, Holder)
         */
        List<Holder<T>> elements();
        
        /**
         * Modifies the value. This is called, once the id for a value is known. If the value itself requires
         * its id, it can use a placeholder and then set the id in this method.
         */
        default T modify(ResourceLocation id, T value) {
            return value;
        }

        /**
         * Gets the destination path for a specific element.
         */
        Path getPath(Path base, ResourceLocation id);
    }
}
