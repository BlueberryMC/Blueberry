package net.blueberrymc.common.bml;

import com.google.common.collect.ImmutableList;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.DeprecatedReason;
import net.blueberrymc.common.bml.event.Event;
import net.blueberrymc.common.bml.event.EventManager;
import net.blueberrymc.common.bml.event.Listener;
import net.blueberrymc.network.mod.ModInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ModManager {
    @NotNull
    public EventManager getEventManager() {
        return Blueberry.getEventManager();
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @DeprecatedReason("Listener interface is deprecated")
    @Deprecated(forRemoval = true)
    public void registerEvents(@NotNull BlueberryMod mod, @NotNull Listener listener) {
        getEventManager().registerEvents(mod, (Object) listener);
    }

    public void registerEvents(@NotNull BlueberryMod mod, @NotNull Object listener) {
        getEventManager().registerEvents(mod, listener);
    }

    public void callEvent(@NotNull Event event) {
        getEventManager().callEvent(event);
    }

    public void loadMods() {
        Blueberry.getModLoader().loadMods();
    }

    @NotNull
    public BlueberryMod loadMod(@NotNull File file) {
        return Blueberry.getModLoader().loadMod(file);
    }

    public void unloadMod(@NotNull BlueberryMod mod) {
        Blueberry.getModLoader().disableMod(mod);
    }

    @NotNull
    public File getModsDir() {
        return Blueberry.getModLoader().getModsDir();
    }

    @NotNull
    public File getConfigDir() {
        return Blueberry.getModLoader().getConfigDir();
    }

    public void loadPacks(@NotNull Consumer<Pack> consumer) {
        for (BlueberryMod mod : Blueberry.getModLoader().getLoadedMods()) {
            try {
                PackResources packResources = mod.getResourceManager().getPackResources();
                Pack.Info info = Pack.readPackInfo(mod.getModId(), (s) -> packResources);
                if (info == null) {
                    throw new RuntimeException("Failed to load mod pack info for " + mod.getModId());
                }
                Pack pack = Pack.create(mod.getDescription().getModId(), Component.literal(mod.getName()), true, (s) -> packResources, info, PackType.CLIENT_RESOURCES, Pack.Position.BOTTOM, false, PackSource.BUILT_IN);
                consumer.accept(pack);
            } catch (IllegalArgumentException ex) {
                break; // resource manager has not been loaded yet
            }
        }
    }

    @NotNull
    public List<ModInfo> getModInfos() {
        List<ModInfo> modInfos = new ArrayList<>();
        Blueberry.getModLoader().getLoadedMods().forEach(mod ->
                modInfos.add(new ModInfo(mod.getDescription().getModId(), mod.getDescription().getVersion())));
        return ImmutableList.copyOf(modInfos);
    }

    @Nullable
    public BlueberryMod getModById(@NotNull String modId) {
        return Blueberry.getModLoader().getModById(modId);
    }

    @Nullable
    public BlueberryMod getModByName(@NotNull String modName) {
        for (BlueberryMod mod : Blueberry.getModLoader().getLoadedMods()) {
            if (mod.getName().equals(modName)) return mod;
        }
        return null;
    }
}
