package io.github.noeppi_noeppi.mods.sandbox.datagen.ext;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import io.github.noeppi_noeppi.mods.sandbox.SandBox;
import io.github.noeppi_noeppi.mods.sandbox.datagen.ext.base.BaseWorldGenData;
import io.github.noeppi_noeppi.mods.sandbox.template.PoolExtension;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.pools.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import org.moddingx.libx.util.Misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TemplateData extends BaseWorldGenData {
    
    private final List<Holder<StructureTemplatePool>> pools;
    private final List<Holder<PoolExtension>> extensions;
    
    public TemplateData(Properties properties) {
        super(properties);
        this.pools = new ArrayList<>();
        this.extensions = new ArrayList<>();
    }
    
    public PoolBuilder template() {
        return this.template(Pools.EMPTY.location());
    }
    
    public PoolBuilder template(ResourceLocation fallback) {
        return new PoolBuilder(fallback);
    }
    
    public ExtensionBuilder extension(ResourceKey<StructureTemplatePool> key) {
        return new ExtensionBuilder(key.location());
    }

    @Override
    public List<Result<?>> results() {
        return List.of(
                this.createResult(Registry.TEMPLATE_POOL_REGISTRY, StructureTemplatePool.DIRECT_CODEC, this.pools, TemplateData::modifyPool),
                this.createResult(SandBox.TEMPLATE_POOL_EXTENSION_REGISTRY, PoolExtension.DIRECT_CODEC, this.extensions)
        );
    }

    public final class PoolBuilder extends TemplateBuilder<PoolBuilder> {

        private final ResourceLocation fallback;
        
        private PoolBuilder(ResourceLocation fallback) {
            this.fallback = fallback;
        }

        @Override
        protected PoolBuilder self() {
            return this;
        }
        
        public Holder<StructureTemplatePool> build() {
            StructureTemplatePool pool = new StructureTemplatePool(Misc.MISSIGNO, this.fallback, this.elements());
            return TemplateData.this.addToList(TemplateData.this.pools, TemplateData.this.registries.holder(Registry.TEMPLATE_POOL_REGISTRY, pool));
        }
    }
    
    public final class ExtensionBuilder extends TemplateBuilder<ExtensionBuilder> {

        private final ResourceLocation parent;
        private boolean required;
        
        private ExtensionBuilder(ResourceLocation parent) {
            this.parent = parent;
        }

        @Override
        protected ExtensionBuilder self() {
            return this;
        }
        
        public ExtensionBuilder required() {
            this.required = true;
            return this;
        }
        
        public Holder<PoolExtension> build() {
            PoolExtension ext = new PoolExtension(this.parent, this.required, this.elements());
            return TemplateData.this.addToList(TemplateData.this.extensions, TemplateData.this.registries.holder(SandBox.TEMPLATE_POOL_EXTENSION_REGISTRY, ext));
        }
    }
    
    public sealed abstract class TemplateBuilder<T extends TemplateBuilder<T>> permits PoolBuilder, ExtensionBuilder {
        
        private final List<PoolEntry> elements;
        private StructureTemplatePool.Projection currentProjection;

        private TemplateBuilder() {
            this.elements = new ArrayList<>();
            this.currentProjection = StructureTemplatePool.Projection.RIGID;
        }
        
        protected abstract T self();
        
        protected List<Pair<StructurePoolElement, Integer>> elements() {
            return this.elements.stream().map(PoolEntry::build).toList();
        }
        
        public T projection(StructureTemplatePool.Projection projection) {
            this.currentProjection = projection;
            return this.self();
        }
        
        public T empty() {
            return this.empty(1);
        }
        
        public T empty(int weight) {
            return this.element(weight, EmptyPoolElement.INSTANCE);
        }
        
        public T feature(Holder<PlacedFeature> feature) {
            return this.feature(1, feature);
        }
        
        public T feature(int weight, Holder<PlacedFeature> feature) {
            return this.element(weight, new FeaturePoolElement(TemplateData.this.registries.holder(Registry.PLACED_FEATURE_REGISTRY, feature), this.currentProjection));
        }

        public T single(String templatePath) {
            return this.single(1, templatePath);
        }

        public T single(int weight, String templatePath) {
            return this.single(weight, templatePath, ProcessorLists.EMPTY);
        }

        public T single(String namespace, String path) {
            return this.single(1, namespace, path);
        }

        public T single(int weight, String namespace, String path) {
            return this.single(weight, namespace, path, ProcessorLists.EMPTY);
        }

        public T single(ResourceLocation templateId) {
            return this.single(1, templateId);
        }

        public T single(int weight, ResourceLocation templateId) {
            return this.single(weight, templateId, ProcessorLists.EMPTY);
        }
        
        public T single(String templatePath, Holder<StructureProcessorList> processor) {
            return this.single(1, templatePath, processor);
        }
        
        public T single(int weight, String templatePath, Holder<StructureProcessorList> processor) {
            return this.single(weight, TemplateData.this.mod.resource(templatePath), processor);
        }
        
        public T single(String namespace, String path, Holder<StructureProcessorList> processor) {
            return this.single(1, namespace, path, processor);
        }
        
        public T single(int weight, String namespace, String path, Holder<StructureProcessorList> processor) {
            return this.single(weight, new ResourceLocation(namespace, path), processor);
        }
        
        public T single(ResourceLocation templateId, Holder<StructureProcessorList> processor) {
            return this.single(1, templateId, processor);
        }
        
        public T single(int weight, ResourceLocation templateId, Holder<StructureProcessorList> processor) {
            return this.element(weight, new SinglePoolElement(Either.left(templateId), TemplateData.this.registries.holder(Registry.PROCESSOR_LIST_REGISTRY, processor), this.currentProjection));
        }

        public T legacy(String templatePath) {
            return this.legacy(1, templatePath);
        }

        public T legacy(int weight, String templatePath) {
            return this.legacy(weight, templatePath, ProcessorLists.EMPTY);
        }

        public T legacy(String namespace, String path) {
            return this.legacy(1, namespace, path);
        }

        public T legacy(int weight, String namespace, String path) {
            return this.legacy(weight, namespace, path, ProcessorLists.EMPTY);
        }

        public T legacy(ResourceLocation templateId) {
            return this.legacy(1, templateId);
        }

        public T legacy(int weight, ResourceLocation templateId) {
            return this.legacy(weight, templateId, ProcessorLists.EMPTY);
        }

        public T legacy(String templatePath, Holder<StructureProcessorList> processor) {
            return this.legacy(1, templatePath, processor);
        }

        public T legacy(int weight, String templatePath, Holder<StructureProcessorList> processor) {
            return this.legacy(weight, TemplateData.this.mod.resource(templatePath), processor);
        }

        public T legacy(String namespace, String path, Holder<StructureProcessorList> processor) {
            return this.legacy(1, namespace, path, processor);
        }

        public T legacy(int weight, String namespace, String path, Holder<StructureProcessorList> processor) {
            return this.legacy(weight, new ResourceLocation(namespace, path), processor);
        }

        public T legacy(ResourceLocation templateId, Holder<StructureProcessorList> processor) {
            return this.legacy(1, templateId, processor);
        }

        public T legacy(int weight, ResourceLocation templateId, Holder<StructureProcessorList> processor) {
            return this.element(weight, new LegacySinglePoolElement(Either.left(templateId), TemplateData.this.registries.holder(Registry.PROCESSOR_LIST_REGISTRY, processor), this.currentProjection));
        }
        
        public T list(StructurePoolElement... elements) {
            return this.list(1, elements);
        }
        
        public T list(int weight, StructurePoolElement... elements) {
            return this.list(weight, Arrays.asList(elements));
        }
        
        public T list(List<StructurePoolElement> elements) {
            return this.list(1, elements);
        }
        
        public T list(int weight, List<StructurePoolElement> elements) {
            return this.element(weight, new ListPoolElement(List.copyOf(elements), this.currentProjection));
        }
        
        public T element(StructurePoolElement element) {
            return this.element(1, element);
        }
        
        public T element(int weight, StructurePoolElement element) {
            this.elements.add(new PoolEntry(element, weight));
            return this.self();
        }

        private record PoolEntry(StructurePoolElement element, int weight) {
            
            public Pair<StructurePoolElement, Integer> build() {
                return Pair.of(this.element(), this.weight());
            }
        }
    }
    
    private static StructureTemplatePool modifyPool(ResourceLocation id, StructureTemplatePool pool) {
        if (Misc.MISSIGNO.equals(pool.getName())) {
            return new StructureTemplatePool(id, pool.getFallback(), pool.rawTemplates);
        } else {
            return pool;
        }
    }
}
