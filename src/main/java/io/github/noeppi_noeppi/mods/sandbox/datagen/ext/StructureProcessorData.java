package io.github.noeppi_noeppi.mods.sandbox.datagen.ext;

import io.github.noeppi_noeppi.mods.sandbox.datagen.ext.base.SingleWorldGenData;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;

import javax.annotation.Nullable;
import java.util.*;

public class StructureProcessorData extends SingleWorldGenData<StructureProcessorList> {
    
    public StructureProcessorData(Properties properties) {
        super(properties, Registry.PROCESSOR_LIST_REGISTRY, StructureProcessorType.DIRECT_CODEC);
    }
    
    public ProcessorListBuilder processor() {
        return new ProcessorListBuilder();
    }

    public static ProcessorRuleBuilder rule(Block block) {
        return rule(block.defaultBlockState());
    }

    public static ProcessorRuleBuilder rule(Block block, CompoundTag nbt) {
        return rule(block.defaultBlockState(), nbt);
    }

    public static ProcessorRuleBuilder rule(BlockState state) {
        return new ProcessorRuleBuilder(state, null);
    }

    public static ProcessorRuleBuilder rule(BlockState state, CompoundTag nbt) {
        return new ProcessorRuleBuilder(state, nbt);
    }
    
    public class ProcessorListBuilder {
        
        private final List<StructureProcessor> processors;
        private final List<ProcessorRule> rules;

        private ProcessorListBuilder() {
            this.processors = new ArrayList<>();
            this.rules = new ArrayList<>();
        }
        
        public ProcessorListBuilder jigsaw() {
            return this.add(JigsawReplacementProcessor.INSTANCE);
        }
        
        public ProcessorListBuilder keep(TagKey<Block> protectedBlocks) {
            return this.add(new ProtectedBlockProcessor(protectedBlocks));
        }
        
        public ProcessorListBuilder ignore(List<Block> ignoredBlocks) {
            return this.add(new BlockIgnoreProcessor(List.copyOf(ignoredBlocks)));
        }
        
        public ProcessorListBuilder age(float mossProbability) {
            return this.add(new BlockAgeProcessor(mossProbability));
        }
        
        public ProcessorListBuilder addRule(ProcessorRule... rules) {
            this.rules.addAll(Arrays.asList(rules));
            return this;
        }
        
        public ProcessorListBuilder addRules(Collection<ProcessorRule> rules) {
            this.rules.addAll(rules);
            return this;
        }
        
        public ProcessorListBuilder add(StructureProcessor... processors) {
            this.rulesToProcessor();
            this.processors.addAll(Arrays.asList(processors));
            return this;
        }
        
        public ProcessorListBuilder addAll(Collection<StructureProcessor> processors) {
            this.rulesToProcessor();
            this.processors.addAll(processors);
            return this;
        }
        
        private void rulesToProcessor() {
            if (!this.rules.isEmpty()) {
                this.processors.add(new RuleProcessor(List.copyOf(this.rules)));
                this.rules.clear();
            }
        }
        
        public Holder<StructureProcessorList> build() {
            this.rulesToProcessor();
            if (this.processors.isEmpty()) this.add(NopProcessor.INSTANCE);
            return StructureProcessorData.this.addToList(StructureProcessorData.this.registries.holder(Registry.PROCESSOR_LIST_REGISTRY, new StructureProcessorList(List.copyOf(this.processors))));
        }
    }

    public static class ProcessorRuleBuilder {

        private final BlockState output;
        private final CompoundTag outputNbt;
        private RuleTest templateState;
        private RuleTest worldState;
        private PosRuleTest location;

        private ProcessorRuleBuilder(BlockState output, @Nullable CompoundTag outputNbt) {
            this.output = output;
            this.outputNbt = outputNbt == null ? null : outputNbt.copy();
            this.templateState = AlwaysTrueTest.INSTANCE;
            this.worldState = AlwaysTrueTest.INSTANCE;
            this.location = PosAlwaysTrueTest.INSTANCE;
        }

        public ProcessorRuleBuilder templateStateTest(RuleTest templateState) {
            this.templateState = templateState;
            return this;
        }

        public ProcessorRuleBuilder worldStateTest(RuleTest worldState) {
            this.worldState = worldState;
            return this;
        }

        public ProcessorRuleBuilder locationTest(PosRuleTest location) {
            this.location = location;
            return this;
        }

        public ProcessorRule build() {
            return new ProcessorRule(this.templateState, this.worldState, this.location, this.output, Optional.ofNullable(this.outputNbt));
        }
    }
}
