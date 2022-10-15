package io.github.noeppi_noeppi.mods.sandbox.template;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.noeppi_noeppi.mods.sandbox.SandBox;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class PoolExtension {
    
    public static final Codec<PoolExtension> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("pool").forGetter(p -> p.poolId),
            Codec.BOOL.fieldOf("required").orElse(false).forGetter(p -> p.required),
            Codec.mapPair(
                    StructurePoolElement.CODEC.fieldOf("element"),
                    Codec.intRange(1, 150).fieldOf("weight")
            ).codec().listOf().fieldOf("elements").forGetter(p -> p.elements)
    ).apply(instance, PoolExtension::new));

    public static final Codec<Holder<PoolExtension>> CODEC = RegistryFileCodec.create(SandBox.TEMPLATE_POOL_EXTENSION_REGISTRY, DIRECT_CODEC);
    
    private final ResourceLocation poolId;
    private final boolean required;
    private final List<Pair<StructurePoolElement, Integer>> elements;

    public PoolExtension(ResourceLocation poolId, boolean required, List<Pair<StructurePoolElement, Integer>> elements) {
        this.poolId = poolId;
        this.required = required;
        this.elements = List.copyOf(elements);
    }
    
    public ResourceLocation poolId() {
        return this.poolId;
    }

    public boolean required() {
        return this.required;
    }

    public List<Pair<StructurePoolElement, Integer>> elements() {
        return this.elements;
    }
}
