package net.blueberrymc.common.resources;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import net.blueberrymc.common.bml.BlueberryMod;
import net.minecraft.DetectedVersion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ModPackResources extends AbstractPackResources {
    static final Logger LOGGER = LogUtils.getLogger();
    private final BlueberryMod mod;
    private final SharedZipFileAccess zipFileAccess;
    private final String prefix;

    public ModPackResources(BlueberryMod mod) {
        super("Mod Resources for " + mod.name() + " (File)", true);
        this.mod = mod;
        this.zipFileAccess = new SharedZipFileAccess(mod.getFile());
        this.prefix = "";
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

    private static String getPathFromLocation(PackType packType, ResourceLocation resourceLocation) {
        return String.format(Locale.ROOT, "%s/%s/%s", packType.getDirectory(), resourceLocation.getNamespace(), resourceLocation.getPath());
    }

    public IoSupplier<InputStream> getResource(@NotNull PackType packType, @NotNull ResourceLocation resourceLocation) {
        return this.getResource(getPathFromLocation(packType, resourceLocation));
    }

    private String addPrefix(String s) {
        return this.prefix.isEmpty() ? s : this.prefix + "/" + s;
    }

    private @Nullable IoSupplier<InputStream> getResource(String s) {
        ZipFile zipFile = this.zipFileAccess.getOrCreateZipFile();
        if (zipFile == null) {
            return null;
        } else {
            ZipEntry zipEntry = zipFile.getEntry(this.addPrefix(s));
            return zipEntry == null ? null : IoSupplier.create(zipFile, zipEntry);
        }
    }

    public void listResources(@NotNull PackType packType, @NotNull String s, @NotNull String s2, PackResources.@NotNull ResourceOutput resourceOutput) {
        ZipFile zipFile = this.zipFileAccess.getOrCreateZipFile();
        if (zipFile != null) {
            Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
            String s3 = this.addPrefix(packType.getDirectory() + "/" + s + "/");
            String s4 = s3 + s2 + "/";

            while(enumeration.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry)enumeration.nextElement();
                if (!zipEntry.isDirectory()) {
                    String s5 = zipEntry.getName();
                    if (s5.startsWith(s4)) {
                        String s6 = s5.substring(s3.length());
                        ResourceLocation resourceLocation = ResourceLocation.tryBuild(s, s6);
                        if (resourceLocation != null) {
                            resourceOutput.accept(resourceLocation, IoSupplier.create(zipFile, zipEntry));
                        } else {
                            LOGGER.warn("Invalid path in datapack: {}:{}, ignoring", s, s6);
                        }
                    }
                }
            }

        }
    }

    @Override
    public @NotNull Set<String> getNamespaces(@NotNull PackType packType) {
        ZipFile zipFile = this.zipFileAccess.getOrCreateZipFile();
        if (zipFile == null) {
            return Set.of();
        } else {
            Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
            Set<String> set = Sets.newHashSet();
            String s = this.addPrefix(packType.getDirectory() + "/");

            while(enumeration.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry)enumeration.nextElement();
                String s2 = zipEntry.getName();
                String s3 = extractNamespace(s, s2);
                if (!s3.isEmpty()) {
                    if (ResourceLocation.isValidNamespace(s3)) {
                        set.add(s3);
                    } else {
                        LOGGER.warn("Non [a-z0-9_.-] character in namespace {} in pack {}, ignoring", s3, this.zipFileAccess.file);
                    }
                }
            }

            return set;
        }
    }

    public static String extractNamespace(String s, String s2) {
        if (!s2.startsWith(s)) {
            return "";
        } else {
            int i = s.length();
            int i2 = s2.indexOf(47, i);
            return i2 == -1 ? s2.substring(i) : s2.substring(i, i2);
        }
    }

    @Override
    public @NotNull String packId() {
        return mod.modId();
    }

    @Override
    public void close() {
        zipFileAccess.close();
    }

    @Contract("_ -> new")
    @ApiStatus.Experimental
    @SuppressWarnings("StringBufferReplaceableByString")
    @NotNull
    public static InputStream createMetadata(@NotNull BlueberryMod mod) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"pack\": {\n");
        sb.append("    \"description\": \"Mod Resources for ").append(mod.getDescription().modId()).append("\",");
        sb.append("    \"pack_format\": ").append(DetectedVersion.tryDetectVersion().getPackVersion(PackType.CLIENT_RESOURCES));
        sb.append("  }\n");
        sb.append("}\n");
        return new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    static class SharedZipFileAccess implements AutoCloseable {
        final File file;
        @javax.annotation.Nullable
        private ZipFile zipFile;
        private boolean failedToLoad;

        SharedZipFileAccess(File file) {
            this.file = file;
        }

        @Nullable ZipFile getOrCreateZipFile() {
            if (this.failedToLoad) {
                return null;
            } else {
                if (this.zipFile == null) {
                    try {
                        this.zipFile = new ZipFile(this.file);
                    } catch (IOException var2) {
                        LOGGER.error("Failed to open pack {}", this.file, var2);
                        this.failedToLoad = true;
                        return null;
                    }
                }

                return this.zipFile;
            }
        }

        public void close() {
            if (this.zipFile != null) {
                IOUtils.closeQuietly(this.zipFile);
                this.zipFile = null;
            }

        }

        @Deprecated
        protected void finalize() throws Throwable {
            this.close();
            super.finalize();
        }
    }
}
