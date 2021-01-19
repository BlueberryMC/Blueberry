package net.blueberrymc.registry;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.bml.ModClassLoader;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class BlueberryRegistries<T> {
    private static final Logger LOGGER = LogManager.getLogger();

    static { init(); }

    public static final BlueberryRegistries<Block> BLOCK = new BlueberryRegistries<>(Registry.BLOCK);
    public static final BlueberryRegistries<Item> ITEM = new BlueberryRegistries<>(Registry.ITEM);
    public static final BlueberryRegistries<Fluid> FLUID = new BlueberryRegistries<>(Registry.FLUID);

    @NotNull private final Registry<T> registry;

    private BlueberryRegistries(@NotNull Registry<T> registry) {
        this.registry = registry;
    }

    @NotNull
    public Registry<T> getRegistry() { return registry; }

    public void register(@NotNull String id, @NotNull T object) {
        if (object.getClass().getClassLoader() instanceof ModClassLoader) {
            register(((ModClassLoader) object.getClass().getClassLoader()).getMod().getDescription().getModId().toLowerCase(Locale.ROOT), id, object);
        }
    }

    public void register(@NotNull String namespace, @NotNull String id, @NotNull T object) {
        //if (has(namespace, id)) return; // TODO: figure out why it returns true even if it's not yet registered
        register(new ResourceLocation(namespace, id), object);
    }

    @Nullable
    public T get(@NotNull ResourceLocation location) {
        return registry.get(location);
    }

    @Nullable
    public T get(@NotNull String namespace, @NotNull String id) {
        return get(new ResourceLocation(namespace, id));
    }

    public int getId(@NotNull T t) {
        return registry.getId(t);
    }

    @Nullable
    public T byId(int id) {
        return registry.byId(id);
    }

    public boolean has(@NotNull ResourceLocation location) {
        return get(location) != null;
    }

    public boolean has(@NotNull String namespace, @NotNull String id) {
        return get(namespace, id) != null;
    }

    public void register(@NotNull ResourceLocation location, @NotNull T object) {
        Preconditions.checkNotNull(location, "ResourceLocation cannot be null");
        Preconditions.checkNotNull(object, "value cannot be null");
        LOGGER.info("Registering " + object.getClass().getSimpleName() + ": " + location);
        if (object instanceof BlockItem) {
            ((BlockItem) object).registerBlocks(Item.BY_BLOCK, (Item) object);
        }
        Registry.register(registry, location, object);
    }

    private static void init() {
        Registry.class.getClassLoader();
    }
}
