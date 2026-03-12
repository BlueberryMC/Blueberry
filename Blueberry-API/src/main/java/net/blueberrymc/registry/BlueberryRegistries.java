package net.blueberrymc.registry;

import com.google.common.base.Preconditions;
import com.mojang.serialization.MapCodec;
import net.blueberrymc.client.EarlyLoadingMessageManager;
import net.blueberrymc.client.renderer.blockentity.MinecraftBlockEntityRenderDispatcher;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.Side;
import net.blueberrymc.common.SideOnly;
import net.blueberrymc.common.bml.ModClassLoader;
import net.blueberrymc.common.util.VoidSafeExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public final class BlueberryRegistries<T> {
    private static final Logger LOGGER = LogManager.getLogger();

    static { init(); }

    public static final BlueberryRegistries<ParticleType<?>> PARTICLE_TYPES = new BlueberryRegistries<>(BuiltInRegistries.PARTICLE_TYPE);
    public static final BlueberryRegistries<Block> BLOCK = new BlueberryRegistries<>(BuiltInRegistries.BLOCK, block -> {
        for (BlockState blockState : block.getStateDefinition().getPossibleStates()) {
            Block.BLOCK_STATE_REGISTRY.add(blockState);
        }
        block.getLootTable(); // initialize loot table key
    });
    public static final BlueberryRegistries<Item> ITEM = new BlueberryRegistries<>(BuiltInRegistries.ITEM);
    public static final BlueberryRegistries<MenuType<?>> MENU = new BlueberryRegistries<>(BuiltInRegistries.MENU);
    public static final BlueberryRegistries<BlockEntityType<?>> BLOCK_ENTITY_TYPE = new BlueberryRegistries<>(BuiltInRegistries.BLOCK_ENTITY_TYPE);
    public static final BlueberryRegistries<Fluid> FLUID = new BlueberryRegistries<>(BuiltInRegistries.FLUID, fluid -> {
        for (FluidState fluidState : fluid.getStateDefinition().getPossibleStates()) {
            Fluid.FLUID_STATE_REGISTRY.add(fluidState);
        }
    });
    public static final BlueberryRegistries<SoundEvent> SOUND_EVENT = new BlueberryRegistries<>(BuiltInRegistries.SOUND_EVENT);
    public static final BlueberryRegistries<MobEffect> MOB_EFFECT = new BlueberryRegistries<>(BuiltInRegistries.MOB_EFFECT);
    public static final BlueberryRegistries<EntityType<?>> ENTITY_TYPE = new BlueberryRegistries<>(BuiltInRegistries.ENTITY_TYPE);
    public static final BlueberryRegistries<Potion> POTION = new BlueberryRegistries<>(BuiltInRegistries.POTION);
    public static final BlueberryRegistries<ParticleType<?>> PARTICLE_TYPE = new BlueberryRegistries<>(BuiltInRegistries.PARTICLE_TYPE);
    public static final BlueberryRegistries<Identifier> CUSTOM_STAT = new BlueberryRegistries<>(BuiltInRegistries.CUSTOM_STAT);
    public static final BlueberryRegistries<RuleTestType<?>> RULE_TEST = new BlueberryRegistries<>(BuiltInRegistries.RULE_TEST);
    public static final BlueberryRegistries<PosRuleTestType<?>> POS_RULE_TEST = new BlueberryRegistries<>(BuiltInRegistries.POS_RULE_TEST);
    public static final BlueberryRegistries<RecipeType<?>> RECIPE_TYPE = new BlueberryRegistries<>(BuiltInRegistries.RECIPE_TYPE);
    public static final BlueberryRegistries<RecipeSerializer<?>> RECIPE_SERIALIZER = new BlueberryRegistries<>(BuiltInRegistries.RECIPE_SERIALIZER);
    public static final BlueberryRegistries<Attribute> ATTRIBUTE = new BlueberryRegistries<>(BuiltInRegistries.ATTRIBUTE);
    public static final BlueberryRegistries<PositionSourceType<?>> POSITION_SOURCE_TYPE = new BlueberryRegistries<>(BuiltInRegistries.POSITION_SOURCE_TYPE);
    public static final BlueberryRegistries<StatType<?>> STAT_TYPE = new BlueberryRegistries<>(BuiltInRegistries.STAT_TYPE);
    public static final BlueberryRegistries<PoiType> POINT_OF_INTEREST_TYPE = new BlueberryRegistries<>(BuiltInRegistries.POINT_OF_INTEREST_TYPE);
    public static final BlueberryRegistries<MemoryModuleType<?>> MEMORY_MODULE_TYPE = new BlueberryRegistries<>(BuiltInRegistries.MEMORY_MODULE_TYPE);
    public static final BlueberryRegistries<SensorType<?>> SENSOR_TYPE = new BlueberryRegistries<>(BuiltInRegistries.SENSOR_TYPE);
    public static final BlueberryRegistries<Activity> ACTIVITY = new BlueberryRegistries<>(BuiltInRegistries.ACTIVITY);
    public static final BlueberryRegistries<MapCodec<? extends BiomeSource>> BIOME_SOURCE = new BlueberryRegistries<>(BuiltInRegistries.BIOME_SOURCE);
    public static final BlueberryRegistries<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATOR = new BlueberryRegistries<>(BuiltInRegistries.CHUNK_GENERATOR);
    public static final BlueberryRegistries<StructureProcessorType<?>> STRUCTURE_PROCESSOR = new BlueberryRegistries<>(BuiltInRegistries.STRUCTURE_PROCESSOR);
    public static final BlueberryRegistries<StructurePoolElementType<?>> STRUCTURE_POOL_ELEMENT = new BlueberryRegistries<>(BuiltInRegistries.STRUCTURE_POOL_ELEMENT);
    public static final BlueberryRegistries<WorldCarver<?>> CARVER = new BlueberryRegistries<>(BuiltInRegistries.CARVER);
    public static final BlueberryRegistries<Feature<?>> FEATURE = new BlueberryRegistries<>(BuiltInRegistries.FEATURE);
    public static final BlueberryRegistries<StructurePieceType> STRUCTURE_PIECE = new BlueberryRegistries<>(BuiltInRegistries.STRUCTURE_PIECE);
    public static final BlueberryRegistries<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPE = new BlueberryRegistries<>(BuiltInRegistries.COMMAND_ARGUMENT_TYPE);

    @NotNull private final Registry<T> registry;
    @Nullable private final Consumer<T> registerAction;

    @Contract(value = "_ -> new", pure = true)
    @NotNull
    public static <T> BlueberryRegistries<T> of(@NotNull Registry<T> registry) {
        return of(registry, null);
    }

    @Contract(value = "_, _ -> new", pure = true)
    @NotNull
    public static <T> BlueberryRegistries<T> of(@NotNull Registry<T> registry, @Nullable Consumer<T> registerAction) {
        return new BlueberryRegistries<>(registry, registerAction);
    }

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
            register(((ModClassLoader) object.getClass().getClassLoader()).getMod().getDescription().modId().toLowerCase(Locale.ROOT), id, object);
        }
    }

    @NotNull
    public <R extends T> R register(@NotNull String namespace, @NotNull String id, @NotNull R object) {
        //if (has(namespace, id)) return; // TODO: figure out why it returns true even if it's not yet registered
        return register(Identifier.fromNamespaceAndPath(namespace, id), object);
    }

    @Nullable
    public T get(@NotNull Identifier location) {
        return registry.get(location).map(Holder.Reference::value).orElse(null);
    }

    @Nullable
    public T get(@NotNull String namespace, @NotNull String id) {
        return get(Identifier.fromNamespaceAndPath(namespace, id));
    }

    public int getId(@NotNull T t) {
        return registry.getId(t);
    }

    @Nullable
    public T byId(int id) {
        return registry.byId(id);
    }

    public boolean has(@NotNull Identifier location) {
        return get(location) != null;
    }

    public boolean has(@NotNull String namespace, @NotNull String id) {
        return get(namespace, id) != null;
    }

    @NotNull
    public <R extends T> R register(@NotNull Identifier location, @NotNull R object) {
        Preconditions.checkNotNull(location, "Identifier cannot be null");
        Preconditions.checkNotNull(object, "value cannot be null");
        String message = "Registering " + object.getClass().getCanonicalName() + ": " + location;
        LOGGER.info(message);
        Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
            @Override
            public void execute() {
                EarlyLoadingMessageManager.logMinecraft(message);
            }
        });
        if (object instanceof BlockItem) {
            ((BlockItem) object).registerBlocks(Item.BY_BLOCK, (Item) object);
        }
        R result = Registry.register(registry, location, object);
        if (registerAction != null) registerAction.accept(object);
        return result;
    }

    @SideOnly(Side.CLIENT)
    public static synchronized <T extends BlockEntity, S extends BlockEntityRenderState> void bindTileEntityRenderer(
            @NotNull BlockEntityType<T> blockEntityType,
            @NotNull Function<? super BlockEntityRenderDispatcher, ? extends BlockEntityRenderer<? super T, ? super S>> rendererFactory
    ) {
        ((MinecraftBlockEntityRenderDispatcher) Minecraft.getInstance().getBlockEntityRenderDispatcher()).registerSpecialRenderer(blockEntityType, rendererFactory.apply(Minecraft.getInstance().getBlockEntityRenderDispatcher()));
    }

    private static void init() {
        BuiltInRegistries.BLOCK.byId(1);
    }
}
