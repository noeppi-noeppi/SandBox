package io.github.noeppi_noeppi.mods.sandbox.datagen.ext;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import io.github.noeppi_noeppi.mods.sandbox.datagen.ext.base.SingleWorldGenData;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.pools.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import org.moddingx.libx.util.Misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TemplateData extends SingleWorldGenData<StructureTemplatePool> {
    
    public TemplateData(Properties properties) {
        super(properties, Registry.TEMPLATE_POOL_REGISTRY, StructureTemplatePool.DIRECT_CODEC, TemplateData::modifyPool);
    }
    
    public TemplateBuilder template() {
        return this.template(Pools.EMPTY.location());
    }
    
    public TemplateBuilder template(ResourceLocation fallback) {
        return new TemplateBuilder(fallback);
    }
    
    public class TemplateBuilder {
        
        private final ResourceLocation fallback;
        private final List<PoolEntry> elements;
        private StructureTemplatePool.Projection currentProjection;

        private TemplateBuilder(ResourceLocation fallback) {
            this.fallback = fallback;
            this.elements = new ArrayList<>();
            this.currentProjection = StructureTemplatePool.Projection.RIGID;
        }
        
        public TemplateBuilder projection(StructureTemplatePool.Projection projection) {
            this.currentProjection = projection;
            return this;
        }
        
        public TemplateBuilder empty() {
            return this.empty(1);
        }
        
        public TemplateBuilder empty(int weight) {
            return this.element(weight, EmptyPoolElement.INSTANCE);
        }
        
        public TemplateBuilder feature(Holder<PlacedFeature> feature) {
            return this.feature(1, feature);
        }
        
        public TemplateBuilder feature(int weight, Holder<PlacedFeature> feature) {
            return this.element(weight, new FeaturePoolElement(TemplateData.this.registries.holder(Registry.PLACED_FEATURE_REGISTRY, feature), this.currentProjection));
        }

        public TemplateBuilder single(String templatePath) {
            return this.single(1, templatePath);
        }

        public TemplateBuilder single(int weight, String templatePath) {
            return this.single(weight, templatePath, ProcessorLists.EMPTY);
        }

        public TemplateBuilder single(String namespace, String path) {
            return this.single(1, namespace, path);
        }

        public TemplateBuilder single(int weight, String namespace, String path) {
            return this.single(weight, namespace, path, ProcessorLists.EMPTY);
        }

        public TemplateBuilder single(ResourceLocation templateId) {
            return this.single(1, templateId);
        }

        public TemplateBuilder single(int weight, ResourceLocation templateId) {
            return this.single(weight, templateId, ProcessorLists.EMPTY);
        }
        
        public TemplateBuilder single(String templatePath, Holder<StructureProcessorList> processor) {
            return this.single(1, templatePath, processor);
        }
        
        public TemplateBuilder single(int weight, String templatePath, Holder<StructureProcessorList> processor) {
            return this.single(weight, TemplateData.this.mod.resource(templatePath), processor);
        }
        
        public TemplateBuilder single(String namespace, String path, Holder<StructureProcessorList> processor) {
            return this.single(1, namespace, path, processor);
        }
        
        public TemplateBuilder single(int weight, String namespace, String path, Holder<StructureProcessorList> processor) {
            return this.single(weight, new ResourceLocation(namespace, path), processor);
        }
        
        public TemplateBuilder single(ResourceLocation templateId, Holder<StructureProcessorList> processor) {
            return this.single(1, templateId, processor);
        }
        
        public TemplateBuilder single(int weight, ResourceLocation templateId, Holder<StructureProcessorList> processor) {
            return this.element(weight, new SinglePoolElement(Either.left(templateId), TemplateData.this.registries.holder(Registry.PROCESSOR_LIST_REGISTRY, processor), this.currentProjection));
        }

        public TemplateBuilder legacy(String templatePath) {
            return this.legacy(1, templatePath);
        }

        public TemplateBuilder legacy(int weight, String templatePath) {
            return this.legacy(weight, templatePath, ProcessorLists.EMPTY);
        }

        public TemplateBuilder legacy(String namespace, String path) {
            return this.legacy(1, namespace, path);
        }

        public TemplateBuilder legacy(int weight, String namespace, String path) {
            return this.legacy(weight, namespace, path, ProcessorLists.EMPTY);
        }

        public TemplateBuilder legacy(ResourceLocation templateId) {
            return this.legacy(1, templateId);
        }

        public TemplateBuilder legacy(int weight, ResourceLocation templateId) {
            return this.legacy(weight, templateId, ProcessorLists.EMPTY);
        }

        public TemplateBuilder legacy(String templatePath, Holder<StructureProcessorList> processor) {
            return this.legacy(1, templatePath, processor);
        }

        public TemplateBuilder legacy(int weight, String templatePath, Holder<StructureProcessorList> processor) {
            return this.legacy(weight, TemplateData.this.mod.resource(templatePath), processor);
        }

        public TemplateBuilder legacy(String namespace, String path, Holder<StructureProcessorList> processor) {
            return this.legacy(1, namespace, path, processor);
        }

        public TemplateBuilder legacy(int weight, String namespace, String path, Holder<StructureProcessorList> processor) {
            return this.legacy(weight, new ResourceLocation(namespace, path), processor);
        }

        public TemplateBuilder legacy(ResourceLocation templateId, Holder<StructureProcessorList> processor) {
            return this.legacy(1, templateId, processor);
        }

        public TemplateBuilder legacy(int weight, ResourceLocation templateId, Holder<StructureProcessorList> processor) {
            return this.element(weight, new LegacySinglePoolElement(Either.left(templateId), TemplateData.this.registries.holder(Registry.PROCESSOR_LIST_REGISTRY, processor), this.currentProjection));
        }
        
        public TemplateBuilder list(StructurePoolElement... elements) {
            return this.list(1, elements);
        }
        
        public TemplateBuilder list(int weight, StructurePoolElement... elements) {
            return this.list(weight, Arrays.asList(elements));
        }
        
        public TemplateBuilder list(List<StructurePoolElement> elements) {
            return this.list(1, elements);
        }
        
        public TemplateBuilder list(int weight, List<StructurePoolElement> elements) {
            return this.element(weight, new ListPoolElement(List.copyOf(elements), this.currentProjection));
        }
        
        public TemplateBuilder element(StructurePoolElement element) {
            return this.element(1, element);
        }
        
        public TemplateBuilder element(int weight, StructurePoolElement element) {
            this.elements.add(new PoolEntry(element, weight));
            return this;
        }
        
        public Holder<StructureTemplatePool> build() {
            StructureTemplatePool pool = new StructureTemplatePool(Misc.MISSIGNO, this.fallback, this.elements.stream().map(PoolEntry::build).toList());
            return TemplateData.this.addToList(TemplateData.this.registries.holder(Registry.TEMPLATE_POOL_REGISTRY, pool));
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
