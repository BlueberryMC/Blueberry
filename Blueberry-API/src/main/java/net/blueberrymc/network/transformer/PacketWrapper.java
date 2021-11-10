package net.blueberrymc.network.transformer;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;

@SuppressWarnings({ "unused", "UnusedReturnValue" })
public class PacketWrapper {
    private final FriendlyByteBuf read;
    private final FriendlyByteBuf write;

    public PacketWrapper(@NotNull ByteBuf read) {
        this(read, Unpooled.buffer());
    }

    public PacketWrapper(@NotNull ByteBuf read, @NotNull ByteBuf write) {
        this.read = new FriendlyByteBuf(read);
        this.write = new FriendlyByteBuf(write);
    }

    @NotNull
    public FriendlyByteBuf getRead() {
        return read;
    }

    @NotNull
    public FriendlyByteBuf getWrite() {
        return write;
    }

    @NotNull
    public PacketWrapper writeBoolean(boolean flag) {
        write.writeBoolean(flag);
        return this;
    }

    @NotNull
    public PacketWrapper writeByte(int i) {
        write.writeByte(i);
        return this;
    }

    @NotNull
    public PacketWrapper writeBytes(byte[] bytes) {
        write.writeBytes(bytes);
        return this;
    }

    @NotNull
    public PacketWrapper writeBytes(@NotNull ByteBuf byteBuf) {
        write.writeBytes(byteBuf);
        return this;
    }

    @NotNull
    public PacketWrapper writeBytes(@NotNull ByteBuffer byteBuffer) {
        write.writeBytes(byteBuffer);
        return this;
    }

    @NotNull
    public PacketWrapper writeBytes(@NotNull ByteBuf byteBuf, int i) {
        write.writeBytes(byteBuf, i);
        return this;
    }

    @NotNull
    public PacketWrapper writeBytes(byte[] bytes, int i, int i2) {
        write.writeBytes(bytes, i, i2);
        return this;
    }

    @NotNull
    public PacketWrapper writeBytes(@NotNull ByteBuf byteBuf, int i, int i2) {
        write.writeBytes(byteBuf, i, i2);
        return this;
    }

    @NotNull
    public PacketWrapper writeChar(int i) {
        write.writeChar(i);
        return this;
    }

    @NotNull
    public PacketWrapper writeDouble(double d) {
        write.writeDouble(d);
        return this;
    }

    @NotNull
    public PacketWrapper writeDoubleLE(double value) {
        write.writeDoubleLE(value);
        return this;
    }

    @NotNull
    public PacketWrapper writeFloat(float f) {
        write.writeFloat(f);
        return this;
    }

    @NotNull
    public PacketWrapper writeFloatLE(float value) {
        write.writeFloatLE(value);
        return this;
    }

    @NotNull
    public PacketWrapper writeInt(int i) {
        write.writeInt(i);
        return this;
    }

    @NotNull
    public PacketWrapper writeIntLE(int i) {
        write.writeIntLE(i);
        return this;
    }

    @NotNull
    public PacketWrapper writeLong(long l) {
        write.writeLong(l);
        return this;
    }

    @NotNull
    public PacketWrapper writeLongLE(long l) {
        write.writeLongLE(l);
        return this;
    }

    @NotNull
    public PacketWrapper writeMedium(int i) {
        write.writeMedium(i);
        return this;
    }

    @NotNull
    public PacketWrapper writeMediumLE(int i) {
        write.writeMediumLE(i);
        return this;
    }

    @NotNull
    public PacketWrapper writerIndex(int i) {
        write.writerIndex(i);
        return this;
    }

    @NotNull
    public PacketWrapper writeShort(int i) {
        write.writeShort(i);
        return this;
    }

    @NotNull
    public PacketWrapper writeShortLE(int i) {
        write.writeShortLE(i);
        return this;
    }

    @NotNull
    public PacketWrapper writeZero(int i) {
        write.writeZero(i);
        return this;
    }

    @NotNull
    public PacketWrapper writeBlockPos(@NotNull BlockPos blockPos) {
        write.writeBlockPos(blockPos);
        return this;
    }

    @NotNull
    public PacketWrapper writeByteArray(byte[] bytes) {
        write.writeByteArray(bytes);
        return this;
    }

    @NotNull
    public PacketWrapper writeChunkPos(@NotNull ChunkPos chunkPos) {
        write.writeChunkPos(chunkPos);
        return this;
    }

    @NotNull
    public PacketWrapper writeComponent(@NotNull Component component) {
        write.writeComponent(component);
        return this;
    }

    @NotNull
    public PacketWrapper writeDate(@NotNull Date date) {
        write.writeDate(date);
        return this;
    }

    @NotNull
    public PacketWrapper writeEnum(@NotNull Enum<?> enum_) {
        write.writeEnum(enum_);
        return this;
    }

    @NotNull
    public PacketWrapper writeItem(@NotNull ItemStack itemStack) {
        write.writeItem(itemStack);
        return this;
    }

    @NotNull
    public PacketWrapper writeLongArray(long[] longs) {
        write.writeLongArray(longs);
        return this;
    }

    @NotNull
    public PacketWrapper writeNbt(@Nullable CompoundTag compoundTag) {
        write.writeNbt(compoundTag);
        return this;
    }

    @NotNull
    public PacketWrapper writeResourceLocation(@NotNull ResourceLocation resourceLocation) {
        write.writeResourceLocation(resourceLocation);
        return this;
    }

    @NotNull
    public PacketWrapper writeSectionPos(@NotNull SectionPos sectionPos) {
        write.writeSectionPos(sectionPos);
        return this;
    }

    @NotNull
    public PacketWrapper writeUtf(@NotNull String s) {
        write.writeUtf(s);
        return this;
    }

    @NotNull
    public PacketWrapper writeUtf(@NotNull String s, int i) {
        write.writeUtf(s, i);
        return this;
    }

    @NotNull
    public PacketWrapper writeUUID(@NotNull UUID uuid) {
        write.writeUUID(uuid);
        return this;
    }

    @NotNull
    public PacketWrapper writeVarInt(int i) {
        write.writeVarInt(i);
        return this;
    }

    @NotNull
    public PacketWrapper writeVarIntArray(int[] intArray) {
        write.writeVarIntArray(intArray);
        return this;
    }

    @NotNull
    public PacketWrapper writeVarLong(long l) {
        write.writeVarLong(l);
        return this;
    }

    public boolean readBoolean() {
        return read.readBoolean();
    }

    public byte readByte() {
        return read.readByte();
    }

    public short readUnsignedByte() {
        return read.readUnsignedByte();
    }

    public short readShort() {
        return read.readShort();
    }

    public short readShortLE() {
        return read.readShortLE();
    }

    public int readUnsignedShort() {
        return read.readUnsignedShort();
    }

    public int readUnsignedShortLE() {
        return read.readUnsignedShortLE();
    }

    public int readMedium() {
        return read.readMedium();
    }

    public int readMediumLE() {
        return read.readMediumLE();
    }

    public int readUnsignedMedium() {
        return read.readUnsignedMedium();
    }

    public int readUnsignedMediumLE() {
        return read.readUnsignedMediumLE();
    }

    public int readInt() {
        return read.readInt();
    }

    public int readIntLE() {
        return read.readIntLE();
    }

    public long readUnsignedInt() {
        return read.readUnsignedInt();
    }

    public long readUnsignedIntLE() {
        return read.readUnsignedIntLE();
    }

    public long readLong() {
        return read.readLong();
    }

    public long readLongLE() {
        return read.readLongLE();
    }

    public char readChar() {
        return read.readChar();
    }

    public float readFloat() {
        return read.readFloat();
    }

    public double readDouble() {
        return read.readDouble();
    }

    @NotNull
    public ByteBuf readBytes(int i) {
        return read.readBytes(i);
    }

    @NotNull
    public ByteBuf readSlice(int i) {
        return read.readSlice(i);
    }

    @NotNull
    public ByteBuf readRetainedSlice(int i) {
        return read.readRetainedSlice(i);
    }

    @NotNull
    public PacketWrapper readBytes(@NotNull ByteBuf byteBuf) {
        read.readBytes(byteBuf);
        return this;
    }

    @NotNull
    public PacketWrapper readBytes(@NotNull ByteBuf byteBuf, int i) {
        read.readBytes(byteBuf, i);
        return this;
    }

    @NotNull
    public PacketWrapper readBytes(@NotNull ByteBuf byteBuf, int i, int i2) {
        read.readBytes(byteBuf, i, i2);
        return this;
    }

    @NotNull
    public PacketWrapper readBytes(byte[] bytes) {
        read.readBytes(bytes);
        return this;
    }

    @NotNull
    public PacketWrapper readBytes(byte[] bytes, int i, int i2) {
        read.readBytes(bytes, i, i2);
        return this;
    }

    @NotNull
    public PacketWrapper readBytes(@NotNull ByteBuffer byteBuffer) {
        read.readBytes(byteBuffer);
        return this;
    }

    @NotNull
    public PacketWrapper readBytes(@NotNull OutputStream outputStream, int i) throws IOException {
        read.readBytes(outputStream, i);
        return this;
    }

    public int readBytes(@NotNull GatheringByteChannel gatheringByteChannel, int i) throws IOException {
        return read.readBytes(gatheringByteChannel, i);
    }

    @NotNull
    public CharSequence readCharSequence(int i, @NotNull Charset charset) {
        return read.readCharSequence(i, charset);
    }

    public int readBytes(@NotNull FileChannel fileChannel, long l, int i) throws IOException {
        return read.readBytes(fileChannel, l, i);
    }

    @NotNull
    public ByteBuf skipBytes(int i) {
        return read.skipBytes(i);
    }

    public int writeBytes(@NotNull InputStream inputStream, int i) throws IOException {
        return write.writeBytes(inputStream, i);
    }

    public int writeBytes(@NotNull ScatteringByteChannel scatteringByteChannel, int i) throws IOException {
        return write.writeBytes(scatteringByteChannel, i);
    }

    public int writeBytes(@NotNull FileChannel fileChannel, long l, int i) throws IOException {
        return write.writeBytes(fileChannel, l, i);
    }

    public int writeCharSequence(@NotNull CharSequence charSequence, @NotNull Charset charset) {
        return write.writeCharSequence(charSequence, charset);
    }

    @NotNull
    public <T> T readWithCodec(@NotNull Codec<T> codec) {
        return read.readWithCodec(codec);
    }

    public <T> void writeWithCodec(@NotNull Codec<T> codec, @NotNull T object) {
        write.writeWithCodec(codec, object);
    }

    @NotNull
    public <T, C extends Collection<T>> C readCollection(@NotNull IntFunction<C> intFunction, @NotNull Function<FriendlyByteBuf, T> function) {
        return read.readCollection(intFunction, function);
    }

    public <T> void writeCollection(@NotNull Collection<T> collection, @NotNull BiConsumer<FriendlyByteBuf, T> biConsumer) {
        write.writeCollection(collection, biConsumer);
    }

    @NotNull
    public <T> List<T> readList(@NotNull Function<FriendlyByteBuf, T> function) {
        return read.readList(function);
    }

    @NotNull
    public IntList readIntIdList() {
        return read.readIntIdList();
    }

    public void writeIntIdList(@NotNull IntList intList) {
        write.writeIntIdList(intList);
    }

    @NotNull
    public <K, V, M extends Map<K, V>> M readMap(@NotNull IntFunction<M> intFunction, @NotNull Function<FriendlyByteBuf, K> function, @NotNull Function<FriendlyByteBuf, V> function2) {
        return read.readMap(intFunction, function, function2);
    }

    @NotNull
    public <K, V> Map<K, V> readMap(@NotNull Function<FriendlyByteBuf, K> function, @NotNull Function<FriendlyByteBuf, V> function2) {
        return read.readMap(function, function2);
    }

    public <K, V> void writeMap(@NotNull Map<K, V> map, @NotNull BiConsumer<FriendlyByteBuf, K> biConsumer, @NotNull BiConsumer<FriendlyByteBuf, V> biConsumer2) {
        write.writeMap(map, biConsumer, biConsumer2);
    }

    public void readWithCount(@NotNull Consumer<FriendlyByteBuf> consumer) {
        read.readWithCount(consumer);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public <T> void writeOptional(@NotNull Optional<T> optional, @NotNull BiConsumer<FriendlyByteBuf, T> biConsumer) {
        write.writeOptional(optional, biConsumer);
    }

    @NotNull
    public <T> Optional<T> readOptional(@NotNull Function<FriendlyByteBuf, T> function) {
        return read.readOptional(function);
    }

    public byte[] readByteArray() {
        return read.readByteArray();
    }

    public byte[] readByteArray(int i) {
        return read.readByteArray(i);
    }

    public int[] readVarIntArray() {
        return read.readVarIntArray();
    }

    public int[] readVarIntArray(int i) {
        return read.readVarIntArray(i);
    }

    public long[] readLongArray() {
        return read.readLongArray();
    }

    public long[] readLongArray(long[] longs) {
        return read.readLongArray(longs);
    }

    public long[] readLongArray(long[] longs, int i) {
        return read.readLongArray(longs, i);
    }

    public byte[] accessByteBufWithCorrectSize() {
        return read.accessByteBufWithCorrectSize();
    }

    @NotNull
    public BlockPos readBlockPos() {
        return read.readBlockPos();
    }

    @NotNull
    public ChunkPos readChunkPos() {
        return read.readChunkPos();
    }

    @NotNull
    public SectionPos readSectionPos() {
        return read.readSectionPos();
    }

    @NotNull
    public Component readComponent() {
        return read.readComponent();
    }

    @NotNull
    public <T extends Enum<T>> T readEnum(@NotNull Class<T> clazz) {
        return read.readEnum(clazz);
    }

    public int readVarInt() {
        return read.readVarInt();
    }

    public long readVarLong() {
        return read.readVarLong();
    }

    @NotNull
    public UUID readUUID() {
        return read.readUUID();
    }

    @Nullable
    public CompoundTag readNbt() {
        return read.readNbt();
    }

    @Nullable
    public CompoundTag readAnySizeNbt() {
        return read.readAnySizeNbt();
    }

    @Nullable
    public CompoundTag readNbt(@NotNull NbtAccounter nbtAccounter) {
        return read.readNbt(nbtAccounter);
    }

    @NotNull
    public ItemStack readItem() {
        return read.readItem();
    }

    @NotNull
    public String readUtf() {
        return read.readUtf();
    }

    @NotNull
    public String readUtf(int i) {
        return read.readUtf(i);
    }

    @NotNull
    public ResourceLocation readResourceLocation() {
        return read.readResourceLocation();
    }

    @NotNull
    public Date readDate() {
        return read.readDate();
    }

    @NotNull
    public BlockHitResult readBlockHitResult() {
        return read.readBlockHitResult();
    }

    @NotNull
    public PacketWrapper writeBlockHitResult(@NotNull BlockHitResult blockHitResult) {
        write.writeBlockHitResult(blockHitResult);
        return this;
    }

    @NotNull
    public BitSet readBitSet() {
        return write.readBitSet();
    }

    @NotNull
    public PacketWrapper writeBitSet(@NotNull BitSet bitSet) {
        write.writeBitSet(bitSet);
        return this;
    }

    public int readerCapacity() {
        return read.capacity();
    }

    @NotNull
    public PacketWrapper readerCapacity(int i) {
        read.capacity(i);
        return this;
    }

    public int readerMaxCapacity() {
        return read.maxCapacity();
    }

    public int writerCapacity() {
        return write.capacity();
    }

    @NotNull
    public PacketWrapper writerCapacity(int i) {
        write.capacity(i);
        return this;
    }

    public int writerMaxCapacity() {
        return write.maxCapacity();
    }

    @NotNull
    public ByteOrder readerOrder() {
        return read.order();
    }

    @NotNull
    public ByteOrder writerOrder() {
        return write.order();
    }

    @NotNull
    public ByteBuf readerOrder(@NotNull ByteOrder byteOrder) {
        return read.order(byteOrder);
    }

    @NotNull
    public ByteBuf writerOrder(@NotNull ByteOrder byteOrder) {
        return write.order(byteOrder);
    }

    @Nullable
    public ByteBuf readerUnwrap() {
        return read.unwrap();
    }

    @Nullable
    public ByteBuf writerUnwrap() {
        return write.unwrap();
    }

    public boolean readerIsDirect() {
        return read.isDirect();
    }

    public boolean writerIsDirect() {
        return write.isDirect();
    }

    public boolean readerIsReadOnly() {
        return read.isReadOnly();
    }

    public boolean writerIsReadOnly() {
        return write.isReadOnly();
    }

    @NotNull
    public ByteBuf readerAsReadOnly() {
        return read.asReadOnly();
    }

    @NotNull
    public ByteBuf writerAsReadOnly() {
        return write.asReadOnly();
    }

    public int readerIndex() {
        return read.readerIndex();
    }

    @NotNull
    public PacketWrapper readerIndex(int i) {
        read.readerIndex(i);
        return this;
    }

    public int writerIndex() {
        return write.writerIndex();
    }

    @NotNull
    public PacketWrapper readerSetIndex(int i, int i2) {
        read.setIndex(i, i2);
        return this;
    }

    @NotNull
    public PacketWrapper writerSetIndex(int i, int i2) {
        write.setIndex(i, i2);
        return this;
    }

    public int readableBytes() {
        return read.readableBytes();
    }

    public int writableBytes() {
        return write.writableBytes();
    }

    public int maxWritableBytes() {
        return write.maxWritableBytes();
    }

    public boolean isReadable() {
        return read.isReadable();
    }

    public boolean isReadable(int i) {
        return read.isReadable(i);
    }

    public boolean isWritable() {
        return write.isWritable();
    }

    public boolean isWritable(int i) {
        return write.isWritable(i);
    }

    @NotNull
    public PacketWrapper readerClear() {
        read.clear();
        return this;
    }

    @NotNull
    public PacketWrapper writerClear() {
        write.clear();
        return this;
    }

    @NotNull
    public PacketWrapper markReaderIndex() {
        read.markReaderIndex();
        return this;
    }

    @NotNull
    public PacketWrapper resetReaderIndex() {
        read.resetReaderIndex();
        return this;
    }

    @NotNull
    public PacketWrapper markWriterIndex() {
        write.markWriterIndex();
        return this;
    }

    @NotNull
    public PacketWrapper resetWriterIndex() {
        write.resetWriterIndex();
        return this;
    }

    @NotNull
    public PacketWrapper passthroughAll() {
        write.writeBytes(read, read.readerIndex(), read.readableBytes());
        return this;
    }

    @NotNull
    public PacketWrapper passthroughUtf(int maxLength) {
        return writeUtf(readUtf(maxLength));
    }

    @NotNull
    public PacketWrapper passthrough(@NotNull Type type) {
        return switch (type) {
            case VAR_INT_LIST -> writeVarIntArray(readVarIntArray());
            case BYTE_ARRAY -> writeByteArray(readByteArray());
            case ENUM, VAR_INT -> writeVarInt(readVarInt());
            case BOOLEAN -> writeBoolean(readBoolean());
            case INT -> writeInt(readInt());
            case FLOAT -> writeFloat(readFloat());
            case LONG -> writeLong(readLong());
            case DOUBLE -> writeDouble(readDouble());
            case BIT_SET -> writeBitSet(readBitSet());
            case BLOCK_POS -> writeBlockPos(readBlockPos());
            case BLOCK_HIT_RESULT -> writeBlockHitResult(readBlockHitResult());
            case CHAR -> writeChar(readChar());
            case STRING -> writeUtf(readUtf());
            case DATE -> writeDate(readDate());
            case COMPONENT -> writeComponent(readComponent());
            case ITEM -> writeItem(readItem());
            case NBT -> writeNbt(readNbt());
            case NBT_UNLIMITED -> writeNbt(readAnySizeNbt());
            case UUID -> writeUUID(readUUID());
            case VAR_LONG -> writeVarLong(readVarLong());
            case SHORT -> writeShort(readShort());
            case BYTE -> writeByte(readByte());
            case UNSIGNED_BYTE -> writeByte(readUnsignedByte());
            case RESOURCE_LOCATION -> writeResourceLocation(readResourceLocation());
        };
    }

    public enum Type {
        VAR_INT_LIST, // IntList
        BYTE_ARRAY, // byte[]
        BYTE, // byte
        VAR_INT, // int, Enum
        ENUM, // same as VAR_INT
        BOOLEAN, // boolean
        INT, // int
        FLOAT, // float
        LONG, // long
        DOUBLE, // double
        BIT_SET, // BitSet
        BLOCK_POS, // BlockPos
        BLOCK_HIT_RESULT, // BlockHitResult
        CHAR, // char
        STRING, // String
        DATE, // Date
        COMPONENT, // Component
        ITEM, // ItemStack
        NBT, // CompoundTag
        NBT_UNLIMITED, // CompoundTag but unlimited size
        UUID, // UUID
        VAR_LONG, // long
        SHORT, // short
        UNSIGNED_BYTE, // short -> byte
        RESOURCE_LOCATION, // ResourceLocation
    }
}
