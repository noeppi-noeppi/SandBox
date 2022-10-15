package io.github.noeppi_noeppi.mods.sandbox.dev;

import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class SandBoxDev {
    
    public static void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SandBoxDev::gatherData);
    }
    
    private static void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(true, new RegistryKeyProvider<>(StructureTemplatePool.class, Registry.TEMPLATE_POOL_REGISTRY, "TemplatePools", event.getGenerator()));
    }

    public static String quote(String str) {
        StringBuilder sb = new StringBuilder("\"");
        for (char chr : str.toCharArray()) {
            if (chr == '\\') {
                sb.append("\\\\");
            } else if (chr == '\"') {
                sb.append("\\\"");
            } else if (chr == '\'') {
                sb.append("\\'");
            } else if (chr == '\n') {
                sb.append("\\n");
            } else if (chr == '\r') {
                sb.append("\\r");
            } else if (chr == '\t') {
                sb.append("\\t");
            } else if (chr == '\b') {
                sb.append("\\b");
            } else if (chr <= 0x1F || chr > 0xFF) {
                sb.append(String.format("\\u%04d", (int) chr));
            } else {
                sb.append(chr);
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}
