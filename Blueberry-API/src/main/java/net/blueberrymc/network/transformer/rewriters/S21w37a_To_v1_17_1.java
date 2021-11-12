package net.blueberrymc.network.transformer.rewriters;

import io.netty.buffer.Unpooled;
import net.blueberrymc.common.util.reflect.Ref;
import net.blueberrymc.native_util.NativeUtil;
import net.blueberrymc.network.transformer.PacketWrapper;
import net.blueberrymc.network.transformer.TransformableProtocolVersions;
import net.blueberrymc.util.IntPair;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.BitStorage;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.BitSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class S21w37a_To_v1_17_1 extends S21w40a_To_S21w39a {
    private static final int WIDTH_BITS = 2;
    private static final int HORIZONTAL_MASK = 3;
    private static final int BIOMES_PER_CHUNK = 4 * 4 * 4;

    public S21w37a_To_v1_17_1() {
        this(TransformableProtocolVersions.SNAPSHOT_21W37A, TransformableProtocolVersions.v1_17_1);
    }

    protected S21w37a_To_v1_17_1(@NotNull TransformableProtocolVersions sourcePV, @NotNull TransformableProtocolVersions targetPV) {
        super(sourcePV, targetPV);
    }

    @Override
    protected void preRegister() {
        registerSoundRewriter();
    }

    @Override
    public void registerOutbound() {
    }

    @Override
    public void registerInbound() {
        // ClientboundBlockEntityDataPacket
        rewriteInbound(ConnectionProtocol.PLAY, 0x0A, wrapper -> {
            wrapper.passthrough(PacketWrapper.Type.BLOCK_POS);
            int type = wrapper.readUnsignedByte(); // Action
            type = switch (type) {
                case 1 -> Registry.BLOCK_ENTITY_TYPE.getId(BlockEntityType.MOB_SPAWNER);
                case 2 -> Registry.BLOCK_ENTITY_TYPE.getId(BlockEntityType.COMMAND_BLOCK);
                case 3 -> Registry.BLOCK_ENTITY_TYPE.getId(BlockEntityType.BEACON);
                case 4 -> Registry.BLOCK_ENTITY_TYPE.getId(BlockEntityType.SKULL);
                case 5 -> Registry.BLOCK_ENTITY_TYPE.getId(BlockEntityType.CONDUIT);
                case 6 -> Registry.BLOCK_ENTITY_TYPE.getId(BlockEntityType.BANNER);
                case 7 -> Registry.BLOCK_ENTITY_TYPE.getId(BlockEntityType.STRUCTURE_BLOCK);
                case 8 -> Registry.BLOCK_ENTITY_TYPE.getId(BlockEntityType.END_GATEWAY);
                case 9 -> Registry.BLOCK_ENTITY_TYPE.getId(BlockEntityType.SIGN);
                case 11 -> Registry.BLOCK_ENTITY_TYPE.getId(BlockEntityType.BED);
                case 12 -> Registry.BLOCK_ENTITY_TYPE.getId(BlockEntityType.JIGSAW);
                case 13 -> Registry.BLOCK_ENTITY_TYPE.getId(BlockEntityType.CAMPFIRE);
                case 14 -> Registry.BLOCK_ENTITY_TYPE.getId(BlockEntityType.BEEHIVE);
                default -> type;
            };
            wrapper.writeVarInt(type); // BlockEntity ID
            wrapper.passthrough(PacketWrapper.Type.NBT); // Tag
        });
        // ClientboundLevelChunkPacket -> ClientLevelChunkWithLightPacket
        rewriteInbound(ConnectionProtocol.PLAY, 0x22, wrapper -> {
            int x = wrapper.passthroughInt(); // Chunk X
            int z = wrapper.passthroughInt(); // Chunk Z
            var bitSet = BitSet.valueOf(wrapper.readLongArray());
            int dataLength = 0;
            for (int i = 0; i < bitSet.size(); i++) {
                if (bitSet.get(i)) dataLength++;
            }
            wrapper.passthrough(PacketWrapper.Type.NBT); // Heightmaps
            int[] biomes = wrapper.readVarIntArray(); // Biomes length / Biomes
            byte[] data = wrapper.readByteArray();
            var dataWrapper = new PacketWrapper(Unpooled.wrappedBuffer(data), Unpooled.buffer());
            ChunkSection[] sections = new ChunkSection[dataLength];
            for (int i = 0; i < dataLength; i++) {
                var section = sections[i] = ChunkSection.readWithoutBiomes(dataWrapper);
                //dataWrapper.writeByte(2); // Bits Per Block (sizeBits)
                int bpb = 2;
                Object theData = ChunkSection.createOrReuseData(section.biomes, bpb);
                var storage = ChunkSection.getStorageFor(theData); // unused
                var palette = ChunkSection.getPaletteFor(theData);
                for (int biomeIndex = i * BIOMES_PER_CHUNK; biomeIndex < (i * BIOMES_PER_CHUNK) + BIOMES_PER_CHUNK; biomeIndex++) {
                    int biome = biomes[biomeIndex];
                    int minX = (biomeIndex & HORIZONTAL_MASK) << 2;
                    int minY = ((biomeIndex >> WIDTH_BITS + WIDTH_BITS) << 2) & 15;
                    int minZ = (biomeIndex >> WIDTH_BITS & HORIZONTAL_MASK) << 2;
                    for (int bX = minX; bX < minX + 4; bX++) {
                        for (int bY = minY; bY < minY + 4; bY++) {
                            for (int bZ = minZ; bZ < minZ + 4; bZ++) {
                                int index = PalettedContainer.Strategy.SECTION_BIOMES.getIndex(bX, bY, bZ);
                                // TODO: IllegalArgumentException: The value <index> is not in the specified inclusive range of 0 to <bits>
                                section.biomes.set(bX, bY, bZ, (Biome) palette.valueFor(biome));
                                //storage.set(index, biome);
                            }
                        }
                    }
                }
                ChunkSection.setDataFor(section.biomes, theData);
                section.biomes.write(dataWrapper);
            }
            dataWrapper.getWrite().readerIndex(0);
            byte[] newData = new byte[dataWrapper.getWrite().readableBytes()];
            dataWrapper.getWrite().readBytes(newData);
            wrapper.writeByteArray(newData);
            wrapper.passthroughCollection(() -> {
                // Block entities (old)
                var tag = Objects.requireNonNull(wrapper.readNbt(), "tag cannot be null");
                int relX = tag.getInt("x") & 15;
                int relZ = tag.getInt("z") & 15;
                int packedXZ = (relX << 4) | relZ;
                int y = tag.getInt("y") & 15;
                var id = new ResourceLocation(tag.getString("id"));
                var blockEntityType = Registry.BLOCK_ENTITY_TYPE.get(id);
                if (blockEntityType == null) throw new RuntimeException(id + " is missing a mapping");
                tag.remove("x");
                tag.remove("y");
                tag.remove("z");
                wrapper.writeByte(packedXZ); // Packed XZ
                wrapper.writeShort(y); // The height relative to the world
                wrapper.writeVarInt(Registry.BLOCK_ENTITY_TYPE.getId(blockEntityType)); // The type of block entity
                wrapper.writeNbt(tag); // The block entity's data, without the X, Y, and Z values
            });
            writeLightData(x, z, wrapper);
        });
        // ClientboundLightUpdatePacket
        rewriteInbound(ConnectionProtocol.PLAY, 0x25, wrapper -> {
            int x = wrapper.passthroughVarInt();
            int z = wrapper.passthroughVarInt();
            var data = ChunkLightData.passthrough(wrapper);
            lightDataMap.put(IntPair.of(x, z), data);
            LOGGER.debug("Added ChunkLightData for {}, {}", x, z);
        });
        // ClientboundUpdateTagsPacket
        // TODO: it should be 0x66
        rewriteInbound(ConnectionProtocol.PLAY, 0x67, wrapper -> {
            var tags = wrapper.readMap((buf) -> ResourceKey.createRegistryKey(buf.readResourceLocation()), TagCollection.NetworkPayload::read);
            wrapper.writeVarInt(tags.size());
            tags.forEach((key, value) -> {
                wrapper.writeResourceLocation(key.location());
                if (key.location().getNamespace().equals("minecraft") && key.location().getPath().equals("block")) {
                    addEmptyTags(value, "lava_pool_stone_cannot_replace");
                }
                value.write(wrapper.getWrite());
            });
        });
    }

    @Override
    protected int remapSoundId(int soundId) {
        if (soundId >= 159) return soundId + 3;
        return soundId;
    }

    private void writeLightData(int x, int z, PacketWrapper wrapper) {
        var data = lightDataMap.get(IntPair.of(x, z));
        if (data == null) {
            LOGGER.warn("ChunkLightData doesn't exist for {}, {}", x, z);
            wrapper.writeBoolean(false); // Trust edges
            wrapper.writeVarInt(0);
            wrapper.writeVarInt(0);
            wrapper.writeVarInt(0);
            wrapper.writeVarInt(0);
            wrapper.writeVarInt(1);
            wrapper.writeVarInt(2048);
            wrapper.writeBytes(new byte[2048]);
            wrapper.writeVarInt(1);
            wrapper.writeVarInt(2048);
            wrapper.writeBytes(new byte[2048]);
        } else {
            data.write(wrapper);
        }
    }

    private final Map<IntPair, ChunkLightData> lightDataMap = new ConcurrentHashMap<>();

    private static record ChunkLightData(boolean trustEdges, @NotNull BitSet slm, @NotNull BitSet blm, @NotNull BitSet eslm, @NotNull BitSet eblm, byte[][] sl, byte[][] bl) {
        @NotNull
        public static ChunkLightData passthrough(@NotNull PacketWrapper wrapper) {
            boolean trustEdges = wrapper.passthroughBoolean();
            BitSet slm = wrapper.passthroughBitSet();
            BitSet blm = wrapper.passthroughBitSet();
            BitSet eslm = wrapper.passthroughBitSet();
            BitSet eblm = wrapper.passthroughBitSet();
            int slCount = wrapper.passthroughVarInt();
            byte[][] sl = new byte[slCount][];
            for (int i = 0; i < slCount; i++) {
                sl[i] = wrapper.passthroughByteArray(2048);
            }
            int blCount = wrapper.passthroughVarInt();
            byte[][] bl = new byte[blCount][];
            for (int i = 0; i < blCount; i++) {
                bl[i] = wrapper.passthroughByteArray(2048);
            }
            return new ChunkLightData(trustEdges, slm, blm, eslm, eblm, sl, bl);
        }

        public void write(@NotNull PacketWrapper wrapper) {
            wrapper.writeBoolean(trustEdges);
            wrapper.writeBitSet(slm);
            wrapper.writeBitSet(blm);
            wrapper.writeBitSet(eslm);
            wrapper.writeBitSet(eblm);
            wrapper.writeVarInt(sl.length);
            for (byte[] bytes : sl) wrapper.writeByteArray(bytes);
            wrapper.writeVarInt(bl.length);
            for (byte[] bytes : bl) wrapper.writeByteArray(bytes);
        }
    }

    private static class ChunkSection {
        public final short nonEmptyBlockCount;
        public final PalettedContainer<BlockState> states;
        public final PalettedContainer<Biome> biomes;

        private ChunkSection(short nonEmptyBlockCount) {
            this.nonEmptyBlockCount = nonEmptyBlockCount;
            Registry<Biome> registry = RegistryAccess.builtin().registryOrThrow(Registry.BIOME_REGISTRY);
            this.states = new net.minecraft.world.level.chunk.PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
            this.biomes = new net.minecraft.world.level.chunk.PalettedContainer<>(registry, registry.getOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
        }

        @NotNull
        public static ChunkSection read(@NotNull PacketWrapper wrapper) {
            var section = readWithoutBiomes(wrapper);
            section.biomes.read(wrapper);
            return section;
        }

        @NotNull
        public static ChunkSection readWithoutBiomes(@NotNull PacketWrapper wrapper) {
            short nonEmptyBlockCount = wrapper.readShort();
            var section = new ChunkSection(nonEmptyBlockCount);
            section.states.read(wrapper);
            return section;
        }

        private static final Class<?> PalettedContainer_Data = Ref.forName("net.minecraft.world.level.chunk.PalettedContainer$Data").getClazz();
        private static final Field dataField = Ref.getClass(PalettedContainer.class).getDeclaredField("data").getField();
        private static final Field storageField = Ref.getClass(PalettedContainer_Data).getDeclaredField("storage").getField();
        private static final Field paletteField = Ref.getClass(PalettedContainer_Data).getDeclaredField("palette").getField();
        private static final Method createOrReuseDataMethod = Ref.getClass(PalettedContainer.class).getDeclaredMethod("createOrReuseData", PalettedContainer_Data, int.class).getMethod();

        @Nullable
        public static Object getDataFor(@NotNull PalettedContainer<?> palettedContainer) {
            return NativeUtil.get(dataField, palettedContainer);
        }

        public static void setDataFor(@NotNull PalettedContainer<?> palettedContainer, @NotNull Object data) {
            NativeUtil.set(dataField, palettedContainer, data);
        }

        @NotNull
        public static Object createOrReuseData(@NotNull PalettedContainer<?> palettedContainer, int i) {
            return NativeUtil.invoke(createOrReuseDataMethod, palettedContainer, getDataFor(palettedContainer), i);
        }

        @NotNull
        public static Object createOrReuseData(@NotNull PalettedContainer<?> palettedContainer, @NotNull Object data, int i) {
            return NativeUtil.invoke(createOrReuseDataMethod, palettedContainer, data, i);
        }

        @NotNull
        public static Palette<?> getPaletteFor(@NotNull Object data) {
            return (Palette<?>) NativeUtil.get(paletteField, data);
        }

        @NotNull
        public static BitStorage getStorageFor(@NotNull Object data) {
            return (BitStorage) NativeUtil.get(storageField, data);
        }
    }
}
