package net.blueberrymc.registry;

import com.google.common.base.Preconditions;
import net.blueberrymc.client.renderer.blockentity.MinecraftBlockEntityRenderDispatcher;
import net.blueberrymc.common.bml.ModClassLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

public final class BlueberryRegistries<T> {
    private static final Logger LOGGER = LogManager.getLogger();

    static { init(); }

    public static final BlueberryRegistries<ParticleType<?>> PARTICLE_TYPES = new BlueberryRegistries<>(Registry.PARTICLE_TYPE);
    public static final BlueberryRegistries<Block> BLOCK = new BlueberryRegistries<>(Registry.BLOCK);
    public static final BlueberryRegistries<Item> ITEM = new BlueberryRegistries<>(Registry.ITEM);
    public static final BlueberryRegistries<MenuType<?>> MENU = new BlueberryRegistries<>(Registry.MENU);
    public static final BlueberryRegistries<BlockEntityType<?>> BLOCK_ENTITY_TYPE = new BlueberryRegistries<>(Registry.BLOCK_ENTITY_TYPE);
    public static final BlueberryRegistries<Fluid> FLUID = new BlueberryRegistries<>(Registry.FLUID, fluid -> {
        for (FluidState fluidState : fluid.getStateDefinition().getPossibleStates()) {
            Fluid.FLUID_STATE_REGISTRY.add(fluidState);
        }
    });

    @NotNull private final Registry<T> registry;
    @Nullable private final Consumer<T> registerAction;

    private BlueberryRegistries(@NotNull Registry<T> registry) {
        this(registry, null);
    }

    private BlueberryRegistries(@NotNull Registry<T> registry, @Nullable Consumer<T> registerAction) {
        this.registry = registry;
        this.registerAction = registerAction;
    }

    @NotNull
    public Registry<T> getRegistry() { return registry; }

    public void register(@NotNull String id, @NotNull T object) {
        if (object.getClass().getClassLoader() instanceof ModClassLoader) {
            register(((ModClassLoader) object.getClass().getClassLoader()).getMod().getDescription().getModId().toLowerCase(Locale.ROOT), id, object);
        }
    }

    @NotNull
    public <R extends T> R register(@NotNull String namespace, @NotNull String id, @NotNull R object) {
        //if (has(namespace, id)) return; // TODO: figure out why it returns true even if it's not yet registered
        return register(new ResourceLocation(namespace, id), object);
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

    @NotNull
    public <R extends T> R register(@NotNull ResourceLocation location, @NotNull R object) {
        Preconditions.checkNotNull(location, "ResourceLocation cannot be null");
        Preconditions.checkNotNull(object, "value cannot be null");
        LOGGER.info("Registering " + object.getClass().getCanonicalName() + ": " + location);
        if (object instanceof BlockItem) {
            ((BlockItem) object).registerBlocks(Item.BY_BLOCK, (Item) object);
        }
        R result = Registry.register(registry, location, object);
        if (registerAction != null) registerAction.accept(object);
        return result;
    }

    public static synchronized <T extends BlockEntity> void bindTileEntityRenderer(
            @NotNull BlockEntityType<T> blockEntityType,
            @NotNull Function<? super BlockEntityRenderDispatcher, ? extends BlockEntityRenderer<? super T>> rendererFactory
    ) {
        ((MinecraftBlockEntityRenderDispatcher) Minecraft.getInstance().getBlockEntityRenderDispatcher()).registerSpecialRenderer(blockEntityType, rendererFactory.apply(Minecraft.getInstance().getBlockEntityRenderDispatcher()));
    }

    private static void init() {
        Registry.class.getClassLoader();
    }
}
