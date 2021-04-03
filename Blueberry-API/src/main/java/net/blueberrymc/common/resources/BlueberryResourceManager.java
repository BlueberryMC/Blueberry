package net.blueberrymc.common.resources;

import net.blueberrymc.common.bml.BlueberryMod;
import net.minecraft.DetectedVersion;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class BlueberryResourceManager extends FallbackResourceManager {
    @NotNull private final BlueberryMod mod;
    @NotNull private final PackResources packResources;

    public BlueberryResourceManager(@NotNull BlueberryMod mod) {
        super(PackType.CLIENT_RESOURCES, mod.getDescription().getModId());
        this.mod = mod;
        if (this.mod.getFile().isDirectory()) {
            packResources = new FolderPackResources(this.mod.getFile()) {
                @NotNull
                @Override
                public String getName() {
                    return "Mod Resources for " + mod.getName() + " (Folder)";
                }

                @Nullable
                @Override
                protected InputStream getResource(@NotNull String s) throws IOException {
                    if (s.equals("pack.mcmeta")) {
                        return createMetadata(mod);
                    }
                    InputStream in = mod.getClass().getResourceAsStream("/" + s);
                    if (in == null) in = mod.getClassLoader().getResourceAsStream("/" + s);
                    if (in == null) {
                        try {
                            File file = new File(mod.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                            if (file.exists() && file.isDirectory()) {
                                File entry = new File(file, s);
                                if (entry.exists() && entry.isFile()) {
                                    in = new FileInputStream(entry);
                                }
                            }
                        } catch (URISyntaxException ignore) {}
                    }
                    if (in != null) return in;
                    return super.getResource(s);
                }

                @Override
                public boolean hasResource(@NotNull String s) {
                    try {
                        return getResource(s) != null;
                    } catch (IOException exception) {
                        return false;
                    }
                }
            };
        } else {
            packResources = new FilePackResources(this.mod.getFile()) {
                @NotNull
                @Override
                public String getName() {
                    return "Mod Resources for " + mod.getName() + " (File)";
                }

                @Nullable
                @Override
                protected InputStream getResource(@NotNull String s) throws IOException {
                    if (s.equals("pack.mcmeta")) {
                        return createMetadata(mod);
                    }
                    InputStream in = mod.getClass().getResourceAsStream("/" + s);
                    if (in == null) in = mod.getClassLoader().getResourceAsStream("/" + s);
                    if (in != null) return in;
                    return super.getResource(s);
                }

                @Override
                public boolean hasResource(@NotNull String s) {
                    try {
                        return getResource(s) != null;
                    } catch (IOException exception) {
                        return false;
                    }
                }
            };
        }
        this.add(packResources);
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    @NotNull
    private static InputStream createMetadata(@NotNull BlueberryMod mod) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"pack\": {\n");
        sb.append("    \"description\": \"Mod Resources for ").append(mod.getDescription().getModId()).append("\",");
        sb.append("    \"pack_format\": ").append(DetectedVersion.tryDetectVersion().getPackVersion(com.mojang.bridge.game.PackType.RESOURCE));
        sb.append("  }\n");
        sb.append("}\n");
        return new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    public @NotNull PackResources getPackResources() {
        return packResources;
    }

    @NotNull
    public BlueberryMod getMod() {
        return mod;
    }
}
