package io.github.noeppi_noeppi.mods.sandbox.dev;

import com.google.common.hash.HashCode;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Locale;

public class RegistryKeyProvider<T> implements DataProvider {

    public static final String PACKAGE = "io.github.noeppi_noeppi.mods.sandbox.keys";
    
    private final Class<T> registryClass;
    private final ResourceKey<? extends Registry<T>> registry;
    private final String className;
    
    private final DataGenerator gen;
    
    public RegistryKeyProvider(Class<T> registryClass, ResourceKey<? extends Registry<T>> registry, String className, DataGenerator gen) {
        this.registryClass = registryClass;
        this.registry = registry;
        this.className = className;
        this.gen = gen;
    }

    @Nonnull
    @Override
    public String getName() {
        return "Registry keys: " + this.registry;
    }

    @Override
    public void run(@Nonnull CachedOutput output) throws IOException {
        Path target = this.gen.getOutputFolder().toAbsolutePath().getParent().resolve("java")
                .resolve(PACKAGE.replace(".", File.separator))
                .resolve(this.className + ".java")
                .toAbsolutePath().normalize();
        
        String type = this.registryClass.getSimpleName();
        Registry<T> reg = RegistryAccess.BUILTIN.get().registryOrThrow(this.registry);
        
        StringBuilder sourceFile = new StringBuilder();
        sourceFile.append("package ").append(PACKAGE).append(";\n\n");
        sourceFile.append("import net.minecraft.core.Registry;\n");
        sourceFile.append("import net.minecraft.resources.ResourceLocation;\n");
        sourceFile.append("import net.minecraft.resources.ResourceKey;\n");
        sourceFile.append("import ").append(this.registryClass.getName().replace('$', '.')).append(";\n\n");
        sourceFile.append("public class ").append(this.className).append(" {\n\n");
        sourceFile.append("    private static final ResourceKey<Registry<").append(type).append(">> REGISTRY = ResourceKey.createRegistryKey(new ResourceLocation(")
                .append(SandBoxDev.quote(this.registry.location().getNamespace())).append(",").append(SandBoxDev.quote(this.registry.location().getPath()))
                .append("));\n\n");
        for (ResourceLocation key : reg.keySet().stream().sorted(ResourceLocation::compareNamespaced).toList()) {
            StringBuilder fnb = new StringBuilder();
            if ("realms".equals(key.getNamespace())) {
                fnb.append("REALMS_");
            } else if (!"minecraft".equals(key.getNamespace())) {
                continue;
            }
            for (int chr : key.getPath().chars().toArray()) {
                if (!Character.isJavaIdentifierPart((char) chr)) {
                    fnb.append("_");
                } else {
                    fnb.append(Character.toString(chr).toUpperCase(Locale.ROOT));
                }
            }
            String fn = fnb.toString();
            sourceFile.append("    public static final ResourceKey<").append(type).append("> ").append(fn).append(" = ResourceKey.create(REGISTRY, new ResourceLocation(")
                    .append(SandBoxDev.quote(key.getNamespace())).append(",").append(SandBoxDev.quote(key.getPath()))
                    .append("));\n");
        }
        sourceFile.append("}\n");

        ByteBuffer enc = StandardCharsets.UTF_8.encode(sourceFile.toString());
        byte[] data = new byte[enc.remaining()];
        enc.get(data);
        output.writeIfNeeded(target, data, HashCode.fromBytes(data));
    }
}
