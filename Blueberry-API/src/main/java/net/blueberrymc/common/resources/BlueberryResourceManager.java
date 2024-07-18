package net.blueberrymc.common.resources;

import net.blueberrymc.common.bml.BlueberryMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.Optional;

public class BlueberryResourceManager extends FallbackResourceManager {
    @NotNull private final BlueberryMod mod;
    @NotNull private final PackResources packResources;

    public BlueberryResourceManager(@NotNull BlueberryMod mod) {
        super(PackType.CLIENT_RESOURCES, mod.getDescription().modId());
        this.mod = mod;
        if (this.mod.getFile().isDirectory()) {
            PackLocationInfo locationInfo = new PackLocationInfo(
                    mod.modId(),
                    Component.literal("Mod Resources for " + mod.name() + " (Folder)"),
                    PackSource.FEATURE,
                    Optional.of(KnownPack.vanilla(mod.modId()))
            );
            packResources = new PathPackResources(locationInfo, this.mod.getFile().toPath()) {
                @Nullable
                @Override
                public IoSupplier<InputStream> getRootResource(String @NotNull ... paths) {
                    if (paths.length == 1 && paths[0].equals("pack.mcmeta")) {
                        return () -> ModPackResources.createMetadata(mod);
                    }
                    return super.getRootResource(paths);
                }
            };
        } else {
            packResources = new ModPackResources(mod);
        }
        this.push(packResources);
    }

    public @NotNull PackResources getPackResources() {
        return packResources;
    }

    @NotNull
    public BlueberryMod getMod() {
        return mod;
    }
}
