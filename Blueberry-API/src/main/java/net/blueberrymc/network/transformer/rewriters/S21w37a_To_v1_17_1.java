package net.blueberrymc.network.transformer.rewriters;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.blueberrymc.network.transformer.PacketWrapper;
import net.blueberrymc.network.transformer.TransformableProtocolVersions;
import net.blueberrymc.util.CompactArrayUtil;
import net.blueberrymc.util.IntPair;
import net.minecraft.core.Registry;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class S21w37a_To_v1_17_1 extends S21w40a_To_S21w39a {
    private static final int WIDTH_BITS = 2;
    private static final int HORIZONTAL_MASK = 3;
    private static final int BIOMES_PER_CHUNK = 4 * 4 * 4;
    private final Map<IntPair, ChunkLightData> lightDataMap = new ConcurrentHashMap<>();
    private int biomesCount;

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
        // ClientboundForgetLevelChunkPacket
        rewriteInbound(ConnectionProtocol.PLAY, 0x1D, wrapper -> {
            int x = wrapper.passthroughInt(); // Chunk X
            int z = wrapper.passthroughInt(); // Chunk Z
            lightDataMap.remove(IntPair.of(x, z));
            //LOGGER.debug("Removed ChunkLightData for {}, {}", x, z);
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
            int sectionSize = Math.max(16, dataLength);
            ChunkSection[] sections = new ChunkSection[sectionSize];
            // Fill with chunk sections
            for (int i = dataLength; i < sectionSize; i++) sections[i] = ChunkSection.emptySection;
            // Read from packet
            for (int i = 0; i < dataLength; i++) {
                // Read nonEmptyBlockCount
                ChunkSection section = sections[i] = ChunkSection.readChunkSection(dataWrapper, Mth.ceillog2(20341), Mth.ceillog2(biomesCount));
                // Read block states
                section.states = section.statesType.readPalette(dataWrapper);
                // Create biomes data palette
                DataPalette palette = section.biomes = new DataPalette();
                for (int biomeIndex = i * BIOMES_PER_CHUNK; biomeIndex < (i * BIOMES_PER_CHUNK) + BIOMES_PER_CHUNK; biomeIndex++) {
                    int biome = biomes[biomeIndex];
                    int minX = (biomeIndex & HORIZONTAL_MASK) << 2;
                    int minY = ((biomeIndex >> WIDTH_BITS + WIDTH_BITS) << 2) & 15;
                    int minZ = (biomeIndex >> WIDTH_BITS & HORIZONTAL_MASK) << 2;
                    for (int bX = minX; bX < minX + 4; bX++) {
                        for (int bY = minY; bY < minY + 4; bY++) {
                            for (int bZ = minZ; bZ < minZ + 4; bZ++) {
                                palette.setIdAt(bX, bY, bZ, biome);
                            }
                        }
                    }
                }
            }
            // Write the data
            for (ChunkSection section : sections) {
                dataWrapper.writeShort(section.nonEmptyBlockCount);
                section.statesType.writePalette(dataWrapper, section.states);
                section.biomesType.writePalette(dataWrapper, section.biomes);
            }
            dataWrapper.getWrite().readerIndex(0);
            byte[] newData = new byte[dataWrapper.getWrite().readableBytes()];
            dataWrapper.getWrite().readBytes(newData);
            wrapper.writeByteArray(newData);
            // Release the data
            dataWrapper.getRead().release();
            dataWrapper.getWrite().release();
            wrapper.passthroughCollection(() -> {
                // Block entities (old)
                var tag = Objects.requireNonNull(wrapper.readNbt(), "tag cannot be null");
                int relX = tag.getInt("x") & 15;
                int relZ = tag.getInt("z") & 15;
                int packedXZ = (relX << 4) | relZ;
                int y = tag.getInt("y") & 15;
                var id = new ResourceLocation(tag.getString("id"));
                var blockEntityType = Registry.BLOCK_ENTITY_TYPE.get(id);
                int typeId = -1;
                if (blockEntityType == null) {
                    LOGGER.warn("Unknown block entity: {}", id);
                } else {
                    typeId = Registry.BLOCK_ENTITY_TYPE.getId(blockEntityType);
                }
                tag.remove("x");
                tag.remove("y");
                tag.remove("z");
                wrapper.writeByte(packedXZ); // Packed XZ
                wrapper.writeShort(y); // The height relative to the world
                wrapper.writeVarInt(typeId); // The type of block entity
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
            //LOGGER.debug("Added ChunkLightData for {}, {}", x, z);
        });
        // ClientboundLoginPacket
        rewriteInbound(ConnectionProtocol.PLAY, 0x26, wrapper -> {
            wrapper.passthrough(PacketWrapper.Type.INT); // Entity ID
            wrapper.passthrough(PacketWrapper.Type.BOOLEAN); // Is hardcore
            wrapper.passthrough(PacketWrapper.Type.BYTE); // Game mode
            wrapper.passthrough(PacketWrapper.Type.BYTE); // Previous game mode
            wrapper.passthroughCollection(PacketWrapper.Type.RESOURCE_LOCATION); // World count / World names
            var registry = wrapper.passthroughNbt(); // Dimension Codec
            var biomeRegistry = Objects.requireNonNull(registry).getCompound("minecraft:worldgen/biome");
            biomesCount = ((ListTag) Objects.requireNonNull(biomeRegistry.get("value"))).size();
            ChunkSection.emptySection = new ChunkSection((short) 0, Mth.ceillog2(20341), Mth.ceillog2(biomesCount));
            ChunkSection.emptySection.states = new DataPalette();
            ChunkSection.emptySection.states.addId(0);
            ChunkSection.emptySection.biomes = new DataPalette();
            ChunkSection.emptySection.biomes.addId(0);
            wrapper.passthroughAll();
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
        public static ChunkSection emptySection;
        public static final int SIZE = 16 * 16 * 16;
        public final short nonEmptyBlockCount;
        public DataPalettes statesType;
        public DataPalette states = null;
        public DataPalettes biomesType;
        public DataPalette biomes = null;

        private ChunkSection(short nonEmptyBlockCount, int statesGlobalPaletteBits, int biomesGlobalPaletteBits) {
            this.nonEmptyBlockCount = nonEmptyBlockCount;
            this.statesType = new DataPalettes(DataPaletteType.STATES, statesGlobalPaletteBits);
            this.biomesType = new DataPalettes(DataPaletteType.BIOMES, biomesGlobalPaletteBits);
        }

        @NotNull
        public static ChunkSection readChunkSection(@NotNull PacketWrapper wrapper, int statesGlobalPaletteBits, int biomesGlobalPaletteBits) {
            short nonEmptyBlockCount = wrapper.readShort();
            return new ChunkSection(nonEmptyBlockCount, statesGlobalPaletteBits, biomesGlobalPaletteBits);
        }
    }

    public enum DataPaletteType {
        STATES(ChunkSection.SIZE, 8),
        BIOMES(4 * 4 * 4, 2),
        ;

        private final int maxSize;
        private final int highestBitsPerValue;

        DataPaletteType(int maxSize, int highestBitsPerValue) {
            this.maxSize = maxSize;
            this.highestBitsPerValue = highestBitsPerValue;
        }
    }

    public static record DataPalettes(@NotNull DataPaletteType type, int globalPaletteBits) {
        @NotNull
        public DataPalette readPalette(@NotNull PacketWrapper wrapper) {
            int bitsPerValue = wrapper.readByte();
            int originalBitsPerValue = bitsPerValue;
            if (bitsPerValue > type.highestBitsPerValue) {
                bitsPerValue = globalPaletteBits;
            }
            DataPalette palette;
            if (bitsPerValue == 0) {
                palette = new DataPalette(1);
                palette.addId(wrapper.readVarInt());
                wrapper.readVarInt();
                return palette;
            }
            if (bitsPerValue != globalPaletteBits) {
                int paletteLength = wrapper.readVarInt();
                palette = new DataPalette(paletteLength);
                for (int i = 0; i < paletteLength; i++) {
                    palette.addId(wrapper.readVarInt());
                }
            } else {
                palette = new DataPalette();
            }
            long[] values = new long[wrapper.readVarInt()];
            if (values.length > 0) {
                char valuesPerLong = (char) (64 / bitsPerValue);
                int expectedLength = (type.maxSize + valuesPerLong - 1) / valuesPerLong;
                if (values.length != expectedLength) {
                    throw new IllegalStateException("Palette data length (" + values.length + ") does not match expected length (" + expectedLength + ")! bitsPerValue=" + bitsPerValue + ", originalBitsPerValue=" + originalBitsPerValue);
                }
                for (int i = 0; i < values.length; i++) {
                    values[i] = wrapper.readLong();
                }
                CompactArrayUtil.iterateCompactArrayWithPadding(
                        bitsPerValue,
                        type.maxSize,
                        values,
                        bitsPerValue == globalPaletteBits ? palette::setIdAt : palette::setPaletteIndexAt
                );
            }
            return palette;
        }

        public void writePalette(@NotNull PacketWrapper wrapper, @NotNull DataPalette palette) {
            int bitsPerValue;
            if (palette.size() > 1) {
                bitsPerValue = type == DataPaletteType.STATES ? 4 : 1;
                while (palette.size() > 1 << bitsPerValue) {
                    bitsPerValue++;
                }
                if (bitsPerValue > type.highestBitsPerValue) {
                    bitsPerValue = globalPaletteBits;
                }
            } else {
                bitsPerValue = 0;
            }
            wrapper.writeByte(bitsPerValue);
            if (bitsPerValue == 0) {
                wrapper.writeVarInt(palette.idByIndex(0));
                wrapper.writeVarInt(0);
                return;
            }
            if (bitsPerValue != globalPaletteBits) {
                wrapper.writeVarInt(palette.size());
                for (int i = 0; i < palette.size(); i++) {
                    wrapper.writeVarInt(palette.idByIndex(i));
                }
            }
            long[] data = CompactArrayUtil.createCompactArrayWithPadding(
                    bitsPerValue,
                    type.maxSize,
                    bitsPerValue == globalPaletteBits ? palette::idAt : palette::paletteIndexAt
            );
            wrapper.writeVarInt(data.length);
            for (long l : data) {
                wrapper.writeLong(l);
            }
        }
    }

    public static class DataPalette {
        private final int[] values;
        private final IntList palette;
        private final Int2IntMap inversePalette;

        public DataPalette() {
            this.values = new int[ChunkSection.SIZE];
            palette = new IntArrayList();
            inversePalette = new Int2IntOpenHashMap();
            inversePalette.defaultReturnValue(-1);
        }

        public DataPalette(int expectedSize) {
            this.values = new int[ChunkSection.SIZE];
            palette = new IntArrayList(expectedSize);
            inversePalette = new Int2IntOpenHashMap(expectedSize);
            inversePalette.defaultReturnValue(-1);
        }

        public int idAt(int sectionCoordinate) {
            int index = values[sectionCoordinate];
            return palette.getInt(index);
        }

        public int idAt(int secX, int secY, int secZ) {
            return idAt(index(secX, secY, secZ));
        }

        public void setIdAt(int sectionCoordinate, int id) {
            int index = inversePalette.get(id);
            if (index == -1) {
                index = palette.size();
                palette.add(id);
                inversePalette.put(id, index);
            }
            values[sectionCoordinate] = index;
        }

        public void setIdAt(int secX, int secY, int secZ, int id) {
            setIdAt(index(secX, secY, secZ), id);
        }

        public int idByIndex(int index) {
            return palette.getInt(index);
        }

        /*
        public void setIdByIndex(int index, int id) {
            int oldId = palette.set(index, id);
            if (oldId == id) return;
            inversePalette.put(id, index);
            if (inversePalette.get(oldId) == index) {
                inversePalette.remove(oldId);
                for (int i = 0; i < palette.size(); i++) {
                    if (palette.getInt(i) == oldId) {
                        inversePalette.put(oldId, i);
                        break;
                    }
                }
            }
        }
        */

        public int paletteIndexAt(int packedCoordinate) {
            return values[packedCoordinate];
        }

        public void setPaletteIndexAt(int sectionCoordinate, int index) {
            values[sectionCoordinate] = index;
        }

        public void addId(int id) {
            inversePalette.put(id, palette.size());
            palette.add(id);
        }

        /*
        public void replaceId(int oldId, int newId) {
            int index = inversePalette.remove(oldId);
            if (index == -1) return;
            inversePalette.put(newId, index);
            for (int i = 0; i < palette.size(); i++) {
                if (palette.getInt(i) == oldId) {
                    palette.set(i, newId);
                }
            }
        }
        */

        public int size() {
            return palette.size();
        }

        public void setEntry(int index, int id) {
            int oldId = palette.set(index, id);
            if (oldId == id) return;
            inversePalette.put(id, index);
            if (inversePalette.get(oldId) == index) {
                inversePalette.remove(oldId);
                for (int i = 0; i < palette.size(); i++) {
                    if (palette.getInt(i) == oldId) {
                        inversePalette.put(oldId, i);
                        break;
                    }
                }
            }
        }

        /*
        public void replaceEntry(int oldId, int newId) {
            final int index = inversePalette.remove(oldId);
            if (index == -1) return;
            inversePalette.put(newId, index);
            for (int i = 0; i < palette.size(); i++) {
                if (palette.getInt(i) == oldId) {
                    palette.set(i, newId);
                }
            }
        }
        */

        public void addEntry(int id) {
            inversePalette.put(id, palette.size());
            palette.add(id);
        }

        public void clear() {
            palette.clear();
            inversePalette.clear();
        }

        public static int index(int x, int y, int z) {
            return y << 8 | z << 4 | x;
        }
    }
}
