package io.github.noeppi_noeppi.mods.sandbox.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.github.noeppi_noeppi.mods.sandbox.datagen.ext.base.WorldGenData;
import io.github.noeppi_noeppi.mods.sandbox.datagen.registry.WorldGenRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

;

/**
 * Data provider for world gen data.
 * <p>
 * Different parts of world gen should be organised in different instances of {@link WorldGenData}. These can expose
 * their results in fields to be used by other {@link WorldGenData} instances later on. To add a {@link WorldGenData}
 * instance to this provider, use {@link #addData(Function)}. This should be done in {@link #setup()}. Other
 * {@link WorldGenData} instances can then query already added data with {@link #getData(Class)}.
 */
public abstract class WorldGenProviderBase implements DataProvider {

    private final ModX mod;
    private final DataGenerator generator;
    private final WorldGenRegistries registries;
    private final ExistingFileHelper fileHelper;
    
    private final Map<Class<? extends WorldGenData>, WorldGenData> data;

    public WorldGenProviderBase(ModX mod, DataGenerator generator, ExistingFileHelper fileHelper) {
        this.mod = mod;
        this.generator = generator;
        this.registries = new WorldGenRegistries();
        this.fileHelper = fileHelper;
        this.data = new HashMap<>();
        
        // This provider must run before any additional providers. However it is probably not added to the generator yet
        // during the constructor. So add another provider to really do the work.
        generator.addProvider(true, new DataProvider() {
            
            @Nonnull
            @Override
            public String getName() {
                return WorldGenProviderBase.this.getName() + " Implementation";
            }

            @Override
            public void run(@Nonnull CachedOutput cache) throws IOException {
                WorldGenProviderBase.this.doRun(cache);
            }
        });
        
        this.addAdditionalProviders(mod, generator, fileHelper, this.registries);
    }

    /**
     * Can for example be used to add additional data providers that need to reference the worldgen registry
     * set. (For example tag providers.
     */
    protected void addAdditionalProviders(ModX mod, DataGenerator generator, ExistingFileHelper fileHelper, WorldGenRegistries registries) {
        //
    }
    
    protected abstract void setup();
    
    public <T extends WorldGenData> void addData(Function<WorldGenData.Properties, T> factory) {
        T data = factory.apply(new WorldGenData.Properties(this.mod, this, this.registries, this.fileHelper));
        Class<? extends WorldGenData> cls = data.getClass();
        if (this.data.containsKey(cls)) {
            throw new IllegalArgumentException("Duplicate world gen data: " + cls);
        } else {
            this.data.put(cls, data);
        }
    }
    
    public <T extends WorldGenData> T getData(Class<T> cls) {
        if (this.data.containsKey(cls)) {
            //noinspection unchecked
            return (T) this.data.get(cls);
        } else {
            throw new NoSuchElementException("World gen data not present: " + cls);
        }
    }
    
    @Nonnull
    @Override
    public String getName() {
        return this.mod + " world gen";
    }

    @Override
    public void run(@Nonnull CachedOutput cache) throws IOException {
        //
    }

    private void doRun(@Nonnull CachedOutput cache) throws IOException {
        this.setup();
        for (WorldGenData entry : this.data.values()) {
            this.setupAutoIds(entry);
        }
        this.registries.freezeAll();
        List<WorldGenData.Result<?>> results = this.data.values().stream().map(WorldGenData::results).flatMap(List::stream).toList();
        for (WorldGenData.Result<?> result : results) {
            this.writeResult(cache, result);
        }
    }
    
    private void setupAutoIds(WorldGenData data) throws IOException {
        try {
            for (Field field : data.getClass().getDeclaredFields()) {
                if (Modifier.isPublic(field.getModifiers()) && !Modifier.isStatic(field.getModifiers()) && Holder.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    Holder<?> holder = (Holder<?>) field.get(data);
                    if (holder.kind() == Holder.Kind.REFERENCE && holder instanceof Holder.Reference<?> ref && ref.getType() == Holder.Reference.Type.INTRUSIVE) {
                        StringBuilder id = new StringBuilder();
                        for (char chr : field.getName().toCharArray()) {
                            if (Character.isUpperCase(chr)) id.append("_");
                            id.append(Character.toString(chr).toLowerCase(Locale.ROOT));
                        }
                        //noinspection unchecked
                        this.registries.assignId((ResourceKey<? extends Registry<Object>>) ref.registry.key(), this.mod.resource(id.toString()), holder.value());
                    }
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new IOException(e);
        }
    }
    
    private <T> void writeResult(CachedOutput cache, WorldGenData.Result<T> result) throws IOException {
        DynamicOps<JsonElement> ops = this.registries.dynamicOps(JsonOps.INSTANCE);
        Codec<T> codec = result.codec();
        for (Holder<T> value : result.elements()) {
            if (value.kind() == Holder.Kind.DIRECT || !(value instanceof Holder.Reference<T> ref)) {
                throw new IllegalStateException("Direct holder in world gen provider: " + value);
            } else if (!ref.isBound()) {
                throw new IllegalStateException("Unbound holder in world gen provider: " + value);
            } else {
                ResourceLocation id = ref.key().location();
                Path dest = result.getPath(this.generator.getOutputFolder().toAbsolutePath().normalize(), id);
                DataResult<JsonElement> data = codec.encodeStart(ops, result.modify(id, ref.value()));
                if (data.result().isPresent()) {
                    DataProvider.saveStable(cache, data.result().get(), dest);
                } else {
                    throw new IllegalStateException("Failed to encode element " + ref.key() + ": " + data.error().map(DataResult.PartialResult::message).orElse("unknown"));
                }
            }
        }
    }
}
