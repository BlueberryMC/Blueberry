package net.blueberrymc.common.resources;

import net.blueberrymc.common.bml.BlueberryMod;
import net.minecraft.DetectedVersion;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ModPackResources extends FilePackResources {
    private final BlueberryMod mod;

    public ModPackResources(BlueberryMod mod) {
        super("Mod Resources for " + mod.getName() + " (File)", mod.getFile(), true);
        this.mod = mod;
    }

    @SuppressWarnings("resource")
    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String @NotNull ... paths) {
        String path = String.join("/", paths);
        InputStream in = mod.getClass().getResourceAsStream("/" + path);
        if (in == null) in = mod.getClassLoader().getResourceAsStream("/" + path);
        if (in != null) {
            InputStream finalIn = in;
            return () -> finalIn;
        }

        // fallback for pack.mcmeta
        if (paths.length == 1 && paths[0].equals(PackResources.PACK_META)) { // PACK_META = pack.mcmeta
            return () -> createMetadata(mod);
        }

        return null;
    }

    @Override
    public @NotNull String packId() {
        return mod.getModId();
    }

    @Contract("_ -> new")
    @ApiStatus.Experimental
    @SuppressWarnings("StringBufferReplaceableByString")
    @NotNull
    public static InputStream createMetadata(@NotNull BlueberryMod mod) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"pack\": {\n");
        sb.append("    \"description\": \"Mod Resources for ").append(mod.getDescription().getModId()).append("\",");
        sb.append("    \"pack_format\": ").append(DetectedVersion.tryDetectVersion().getPackVersion(PackType.CLIENT_RESOURCES));
        sb.append("  }\n");
        sb.append("}\n");
        return new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
    }
}
