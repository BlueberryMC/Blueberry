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

@ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
@DeprecatedReason("All methods are deprecated; see method descriptions for details")
@Deprecated(forRemoval = true)
public class ModManager {
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @DeprecatedReason("Use Blueberry#getEventManager() instead")
    @Deprecated(forRemoval = true)
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

    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @DeprecatedReason("Use EventManager#registerEvents(BlueberryMod, Object) instead")
    @Deprecated(forRemoval = true)
    public void registerEvents(@NotNull BlueberryMod mod, @NotNull Object listener) {
        getEventManager().registerEvents(mod, listener);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @DeprecatedReason("Use EventManager#callEvent(Event) instead")
    @Deprecated(forRemoval = true)
    public void callEvent(@NotNull Event event) {
        getEventManager().callEvent(event);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @DeprecatedReason("Use ModLoader#loadMods instead")
    @Deprecated(forRemoval = true)
    public void loadMods() {
        Blueberry.getModLoader().loadMods();
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @DeprecatedReason("Use ModLoader#loadMod(File) instead")
    @Deprecated(forRemoval = true)
    @NotNull
    public BlueberryMod loadMod(@NotNull File file) {
        return Blueberry.getModLoader().loadMod(file);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @DeprecatedReason("Use ModLoader#disableMod(BlueberryMod) instead")
    @Deprecated(forRemoval = true)
    public void unloadMod(@NotNull BlueberryMod mod) {
        Blueberry.getModLoader().disableMod(mod);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @DeprecatedReason("Use ModLoader#getModsDir() instead")
    @Deprecated(forRemoval = true)
    @NotNull
    public File getModsDir() {
        return Blueberry.getModLoader().getModsDir();
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @DeprecatedReason("Use ModLoader#getConfigDir() instead")
    @Deprecated(forRemoval = true)
    @NotNull
    public File getConfigDir() {
        return Blueberry.getModLoader().getConfigDir();
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @DeprecatedReason("Use ModLoader#loadPacks(Consumer) instead")
    @Deprecated(forRemoval = true)
    public void loadPacks(@NotNull Consumer<Pack> consumer) {
        Blueberry.getModLoader().loadPacks(consumer);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @DeprecatedReason("Use ModLoader#getModInfos() instead")
    @Deprecated(forRemoval = true)
    @NotNull
    public List<ModInfo> getModInfos() {
        List<ModInfo> modInfos = new ArrayList<>();
        Blueberry.getModLoader().getLoadedMods().forEach(mod ->
                modInfos.add(new ModInfo(mod.getDescription().getModId(), mod.getDescription().getVersion())));
        return ImmutableList.copyOf(modInfos);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @DeprecatedReason("Use ModLoader#getModsById(String) instead")
    @Deprecated(forRemoval = true)
    @Nullable
    public BlueberryMod getModById(@NotNull String modId) {
        return Blueberry.getModLoader().getModById(modId);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @DeprecatedReason("Use ModLoader#getModsByName(String) instead")
    @Deprecated(forRemoval = true)
    @Nullable
    public BlueberryMod getModByName(@NotNull String modName) {
        for (BlueberryMod mod : Blueberry.getModLoader().getLoadedMods()) {
            if (mod.getName().equals(modName)) return mod;
        }
        return null;
    }
}
