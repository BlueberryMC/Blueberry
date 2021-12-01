package net.blueberrymc.network;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ByteProcessor;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
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

public class FriendlyByteBuf extends net.minecraft.network.FriendlyByteBuf {
    public FriendlyByteBuf(@NotNull ByteBuf byteBuf) {
        super(byteBuf);
    }

    @Override
    public <T> @NotNull T readWithCodec(@NotNull Codec<T> codec) {
        return super.readWithCodec(codec);
    }

    @Override
    public <T> void writeWithCodec(@NotNull Codec<T> codec, @NotNull T object) {
        super.writeWithCodec(codec, object);
    }

    @Override
    public <T, C extends Collection<T>> @NotNull C readCollection(@NotNull IntFunction<C> intFunction, @NotNull Function<net.minecraft.network.FriendlyByteBuf, T> function) {
        return super.readCollection(intFunction, function);
    }

    @Override
    public <T> void writeCollection(@NotNull Collection<T> collection, @NotNull BiConsumer<net.minecraft.network.FriendlyByteBuf, T> biConsumer) {
        super.writeCollection(collection, biConsumer);
    }

    @Override
    public <T> @NotNull List<T> readList(@NotNull Function<net.minecraft.network.FriendlyByteBuf, T> function) {
        return super.readList(function);
    }

    @Override
    public @NotNull IntList readIntIdList() {
        return super.readIntIdList();
    }

    @Override
    public void writeIntIdList(@NotNull IntList intList) {
        super.writeIntIdList(intList);
    }

    @Override
    public <K, V, M extends Map<K, V>> @NotNull M readMap(@NotNull IntFunction<M> intFunction, @NotNull Function<net.minecraft.network.FriendlyByteBuf, K> function, @NotNull Function<net.minecraft.network.FriendlyByteBuf, V> function2) {
        return super.readMap(intFunction, function, function2);
    }

    @Override
    public <K, V> @NotNull Map<K, V> readMap(@NotNull Function<net.minecraft.network.FriendlyByteBuf, K> function, @NotNull Function<net.minecraft.network.FriendlyByteBuf, V> function2) {
        return super.readMap(function, function2);
    }

    @Override
    public <K, V> void writeMap(@NotNull Map<K, V> map, @NotNull BiConsumer<net.minecraft.network.FriendlyByteBuf, K> biConsumer, @NotNull BiConsumer<net.minecraft.network.FriendlyByteBuf, V> biConsumer2) {
        super.writeMap(map, biConsumer, biConsumer2);
    }

    @Override
    public void readWithCount(@NotNull Consumer<net.minecraft.network.FriendlyByteBuf> consumer) {
        super.readWithCount(consumer);
    }

    @Override
    public <T> void writeOptional(@NotNull Optional<T> optional, @NotNull BiConsumer<net.minecraft.network.FriendlyByteBuf, T> biConsumer) {
        super.writeOptional(optional, biConsumer);
    }

    @Override
    public <T> @NotNull Optional<T> readOptional(@NotNull Function<net.minecraft.network.FriendlyByteBuf, T> function) {
        return super.readOptional(function);
    }

    @Override
    public byte @NotNull [] readByteArray() {
        return super.readByteArray();
    }

    @NotNull
    @Override
    public net.minecraft.network.FriendlyByteBuf writeByteArray(byte @NotNull [] bytes) {
        return super.writeByteArray(bytes);
    }

    @Override
    public byte @NotNull [] readByteArray(int i) {
        return super.readByteArray(i);
    }

    @Override
    public @NotNull FriendlyByteBuf writeVarIntArray(int @NotNull [] ints) {
        super.writeVarIntArray(ints);
        return this;
    }

    @Override
    public int @NotNull [] readVarIntArray() {
        return super.readVarIntArray();
    }

    @Override
    public int @NotNull [] readVarIntArray(int i) {
        return super.readVarIntArray(i);
    }

    @Override
    public @NotNull FriendlyByteBuf writeLongArray(long @NotNull [] longs) {
        super.writeLongArray(longs);
        return this;
    }

    @Override
    public long @NotNull [] readLongArray() {
        return super.readLongArray();
    }

    @Override
    public long @NotNull [] readLongArray(long@Nullable[] longs) {
        return super.readLongArray(longs);
    }

    @Override
    public long @NotNull [] readLongArray(long@Nullable[] longs, int i) {
        return super.readLongArray(longs, i);
    }

    @Override
    public byte @NotNull [] accessByteBufWithCorrectSize() {
        return super.accessByteBufWithCorrectSize();
    }

    @Override
    public @NotNull BlockPos readBlockPos() {
        return super.readBlockPos();
    }

    @NotNull
    @Override
    public net.minecraft.network.FriendlyByteBuf writeBlockPos(@NotNull BlockPos blockPos) {
        return super.writeBlockPos(blockPos);
    }

    @Override
    public @NotNull ChunkPos readChunkPos() {
        return super.readChunkPos();
    }

    @NotNull
    @Override
    public net.minecraft.network.FriendlyByteBuf writeChunkPos(@NotNull ChunkPos chunkPos) {
        return super.writeChunkPos(chunkPos);
    }

    @Override
    public @NotNull SectionPos readSectionPos() {
        return super.readSectionPos();
    }

    @NotNull
    @Override
    public net.minecraft.network.FriendlyByteBuf writeSectionPos(@NotNull SectionPos sectionPos) {
        return super.writeSectionPos(sectionPos);
    }

    @Override
    public @NotNull Component readComponent() {
        return super.readComponent();
    }

    @NotNull
    @Override
    public net.minecraft.network.FriendlyByteBuf writeComponent(@NotNull Component component) {
        return super.writeComponent(component);
    }

    @Override
    public <T extends Enum<T>> @NotNull T readEnum(@NotNull Class<T> clazz) {
        return super.readEnum(clazz);
    }

    @NotNull
    @Override
    public net.minecraft.network.FriendlyByteBuf writeEnum(@NotNull Enum<?> enum_) {
        return super.writeEnum(enum_);
    }

    @Override
    public int readVarInt() {
        return super.readVarInt();
    }

    @Override
    public long readVarLong() {
        return super.readVarLong();
    }

    @Override
    public @NotNull FriendlyByteBuf writeUUID(@NotNull UUID uuid) {
        super.writeUUID(uuid);
        return this;
    }

    @Override
    public @NotNull UUID readUUID() {
        return super.readUUID();
    }

    @Override
    public @NotNull FriendlyByteBuf writeVarInt(int i) {
        super.writeVarInt(i);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf writeVarLong(long l) {
        super.writeVarLong(l);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf writeNbt(@Nullable CompoundTag compoundTag) {
        super.writeNbt(compoundTag);
        return this;
    }

    @Nullable
    @Override
    public CompoundTag readNbt() {
        return super.readNbt();
    }

    @Nullable
    @Override
    public CompoundTag readAnySizeNbt() {
        return super.readAnySizeNbt();
    }

    @Nullable
    @Override
    public CompoundTag readNbt(@NotNull NbtAccounter nbtAccounter) {
        return super.readNbt(nbtAccounter);
    }

    @Override
    public @NotNull FriendlyByteBuf writeItem(@NotNull ItemStack itemStack) {
        super.writeItem(itemStack);
        return this;
    }

    @Override
    public @NotNull ItemStack readItem() {
        return super.readItem();
    }

    @Override
    public @NotNull String readUtf() {
        return super.readUtf();
    }

    @Override
    public @NotNull String readUtf(int i) {
        return super.readUtf(i);
    }

    @Override
    public @NotNull FriendlyByteBuf writeUtf(@NotNull String s) {
        super.writeUtf(s);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf writeUtf(@NotNull String s, int i) {
        super.writeUtf(s, i);
        return this;
    }

    @Override
    public @NotNull ResourceLocation readResourceLocation() {
        return super.readResourceLocation();
    }

    @Override
    public @NotNull FriendlyByteBuf writeResourceLocation(@NotNull ResourceLocation resourceLocation) {
        super.writeResourceLocation(resourceLocation);
        return this;
    }

    @Override
    public @NotNull Date readDate() {
        return super.readDate();
    }

    @Override
    public @NotNull FriendlyByteBuf writeDate(@NotNull Date date) {
        super.writeDate(date);
        return this;
    }

    @Override
    public @NotNull BlockHitResult readBlockHitResult() {
        return super.readBlockHitResult();
    }

    @Override
    public void writeBlockHitResult(@NotNull BlockHitResult blockHitResult) {
        super.writeBlockHitResult(blockHitResult);
    }

    @Override
    public @NotNull BitSet readBitSet() {
        return super.readBitSet();
    }

    @Override
    public void writeBitSet(@NotNull BitSet bitSet) {
        super.writeBitSet(bitSet);
    }

    @Override
    public int capacity() {
        return super.capacity();
    }

    @Override
    public @NotNull FriendlyByteBuf capacity(int i) {
        super.capacity(i);
        return this;
    }

    @Override
    public int maxCapacity() {
        return super.maxCapacity();
    }

    @Override
    public @NotNull ByteBufAllocator alloc() {
        return super.alloc();
    }

    @Override
    public @NotNull ByteOrder order() {
        return super.order();
    }

    @Override
    public @NotNull ByteBuf order(@NotNull ByteOrder byteOrder) {
        return super.order(byteOrder);
    }

    @Override
    public @NotNull ByteBuf unwrap() {
        return super.unwrap();
    }

    @Override
    public boolean isDirect() {
        return super.isDirect();
    }

    @Override
    public boolean isReadOnly() {
        return super.isReadOnly();
    }

    @Override
    public @NotNull ByteBuf asReadOnly() {
        return super.asReadOnly();
    }

    @Override
    public int readerIndex() {
        return super.readerIndex();
    }

    @Override
    public @NotNull FriendlyByteBuf readerIndex(int i) {
        super.readerIndex(i);
        return this;
    }

    @Override
    public int writerIndex() {
        return super.writerIndex();
    }

    @Override
    public @NotNull FriendlyByteBuf writerIndex(int i) {
        super.writerIndex(i);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf setIndex(int i, int i2) {
        super.setIndex(i, i2);
        return this;
    }

    @Override
    public int readableBytes() {
        return super.readableBytes();
    }

    @Override
    public int writableBytes() {
        return super.writableBytes();
    }

    @Override
    public int maxWritableBytes() {
        return super.maxWritableBytes();
    }

    @Override
    public boolean isReadable() {
        return super.isReadable();
    }

    @Override
    public boolean isReadable(int i) {
        return super.isReadable(i);
    }

    @Override
    public boolean isWritable() {
        return super.isWritable();
    }

    @Override
    public boolean isWritable(int i) {
        return super.isWritable(i);
    }

    @Override
    public @NotNull FriendlyByteBuf clear() {
        super.clear();
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf markReaderIndex() {
        super.markReaderIndex();
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf resetReaderIndex() {
        super.resetReaderIndex();
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf markWriterIndex() {
        super.markWriterIndex();
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf resetWriterIndex() {
        super.resetWriterIndex();
        return this;
    }

    @Override
    public @NotNull ByteBuf discardReadBytes() {
        return super.discardReadBytes();
    }

    @Override
    public @NotNull ByteBuf discardSomeReadBytes() {
        return super.discardSomeReadBytes();
    }

    @Override
    public @NotNull ByteBuf ensureWritable(int i) {
        return super.ensureWritable(i);
    }

    @Override
    public int ensureWritable(int i, boolean flag) {
        return super.ensureWritable(i, flag);
    }

    @Override
    public boolean getBoolean(int i) {
        return super.getBoolean(i);
    }

    @Override
    public byte getByte(int i) {
        return super.getByte(i);
    }

    @Override
    public short getUnsignedByte(int i) {
        return super.getUnsignedByte(i);
    }

    @Override
    public short getShort(int i) {
        return super.getShort(i);
    }

    @Override
    public short getShortLE(int i) {
        return super.getShortLE(i);
    }

    @Override
    public int getUnsignedShort(int i) {
        return super.getUnsignedShort(i);
    }

    @Override
    public int getUnsignedShortLE(int i) {
        return super.getUnsignedShortLE(i);
    }

    @Override
    public int getMedium(int i) {
        return super.getMedium(i);
    }

    @Override
    public int getMediumLE(int i) {
        return super.getMediumLE(i);
    }

    @Override
    public int getUnsignedMedium(int i) {
        return super.getUnsignedMedium(i);
    }

    @Override
    public int getUnsignedMediumLE(int i) {
        return super.getUnsignedMediumLE(i);
    }

    @Override
    public int getInt(int i) {
        return super.getInt(i);
    }

    @Override
    public int getIntLE(int i) {
        return super.getIntLE(i);
    }

    @Override
    public long getUnsignedInt(int i) {
        return super.getUnsignedInt(i);
    }

    @Override
    public long getUnsignedIntLE(int i) {
        return super.getUnsignedIntLE(i);
    }

    @Override
    public long getLong(int i) {
        return super.getLong(i);
    }

    @Override
    public long getLongLE(int i) {
        return super.getLongLE(i);
    }

    @Override
    public char getChar(int i) {
        return super.getChar(i);
    }

    @Override
    public float getFloat(int i) {
        return super.getFloat(i);
    }

    @Override
    public double getDouble(int i) {
        return super.getDouble(i);
    }

    @Override
    public @NotNull FriendlyByteBuf getBytes(int i, @NotNull ByteBuf byteBuf) {
        super.getBytes(i, byteBuf);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf getBytes(int i, @NotNull ByteBuf byteBuf, int i2) {
        super.getBytes(i, byteBuf, i2);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf getBytes(int i, @NotNull ByteBuf byteBuf, int i2, int i3) {
        super.getBytes(i, byteBuf, i2, i3);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf getBytes(int i, byte @NotNull [] bytes) {
        super.getBytes(i, bytes);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf getBytes(int i, byte @NotNull [] bytes, int i2, int i3) {
        super.getBytes(i, bytes, i2, i3);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf getBytes(int i, @NotNull ByteBuffer byteBuffer) {
        super.getBytes(i, byteBuffer);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf getBytes(int i, @NotNull OutputStream outputStream, int i2) throws IOException {
        super.getBytes(i, outputStream, i2);
        return this;
    }

    @Override
    public int getBytes(int i, @NotNull GatheringByteChannel gatheringByteChannel, int i2) throws IOException {
        return super.getBytes(i, gatheringByteChannel, i2);
    }

    @Override
    public int getBytes(int i, @NotNull FileChannel fileChannel, long l, int i2) throws IOException {
        return super.getBytes(i, fileChannel, l, i2);
    }

    @Override
    public @NotNull CharSequence getCharSequence(int i, int i2, @NotNull Charset charset) {
        return super.getCharSequence(i, i2, charset);
    }

    @Override
    public @NotNull FriendlyByteBuf setBoolean(int i, boolean flag) {
        super.setBoolean(i, flag);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf setByte(int i, int i2) {
        super.setByte(i, i2);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf setShort(int i, int i2) {
        super.setShort(i, i2);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf setShortLE(int i, int i2) {
        super.setShortLE(i, i2);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf setMedium(int i, int i2) {
        super.setMedium(i, i2);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf setMediumLE(int i, int i2) {
        super.setMediumLE(i, i2);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf setInt(int i, int i2) {
        super.setInt(i, i2);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf setIntLE(int i, int i2) {
        super.setIntLE(i, i2);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf setLong(int i, long l) {
        super.setLong(i, l);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf setLongLE(int i, long l) {
        super.setLongLE(i, l);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf setChar(int i, int i2) {
        super.setChar(i, i2);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf setFloat(int i, float f) {
        super.setFloat(i, f);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf setDouble(int i, double d) {
        super.setDouble(i, d);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf setBytes(int i, @NotNull ByteBuf byteBuf) {
        super.setBytes(i, byteBuf);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf setBytes(int i, @NotNull ByteBuf byteBuf, int i2) {
        super.setBytes(i, byteBuf, i2);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf setBytes(int i, @NotNull ByteBuf byteBuf, int i2, int i3) {
        super.setBytes(i, byteBuf, i2, i3);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf setBytes(int i, byte @NotNull [] bytes) {
        super.setBytes(i, bytes);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf setBytes(int i, byte @NotNull [] bytes, int i2, int i3) {
        super.setBytes(i, bytes, i2, i3);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf setBytes(int i, @NotNull ByteBuffer byteBuffer) {
        super.setBytes(i, byteBuffer);
        return this;
    }

    @Override
    public int setBytes(int i, @NotNull InputStream inputStream, int i2) throws IOException {
        return super.setBytes(i, inputStream, i2);
    }

    @Override
    public int setBytes(int i, @NotNull ScatteringByteChannel scatteringByteChannel, int i2) throws IOException {
        return super.setBytes(i, scatteringByteChannel, i2);
    }

    @Override
    public int setBytes(int i, @NotNull FileChannel fileChannel, long l, int i2) throws IOException {
        return super.setBytes(i, fileChannel, l, i2);
    }

    @Override
    public @NotNull FriendlyByteBuf setZero(int i, int i2) {
        super.setZero(i, i2);
        return this;
    }

    @Override
    public int setCharSequence(int i, @NotNull CharSequence charSequence, @NotNull Charset charset) {
        return super.setCharSequence(i, charSequence, charset);
    }

    @Override
    public boolean readBoolean() {
        return super.readBoolean();
    }

    @Override
    public byte readByte() {
        return super.readByte();
    }

    @Override
    public short readUnsignedByte() {
        return super.readUnsignedByte();
    }

    @Override
    public short readShort() {
        return super.readShort();
    }

    @Override
    public short readShortLE() {
        return super.readShortLE();
    }

    @Override
    public int readUnsignedShort() {
        return super.readUnsignedShort();
    }

    @Override
    public int readUnsignedShortLE() {
        return super.readUnsignedShortLE();
    }

    @Override
    public int readMedium() {
        return super.readMedium();
    }

    @Override
    public int readMediumLE() {
        return super.readMediumLE();
    }

    @Override
    public int readUnsignedMedium() {
        return super.readUnsignedMedium();
    }

    @Override
    public int readUnsignedMediumLE() {
        return super.readUnsignedMediumLE();
    }

    @Override
    public int readInt() {
        return super.readInt();
    }

    @Override
    public int readIntLE() {
        return super.readIntLE();
    }

    @Override
    public long readUnsignedInt() {
        return super.readUnsignedInt();
    }

    @Override
    public long readUnsignedIntLE() {
        return super.readUnsignedIntLE();
    }

    @Override
    public long readLong() {
        return super.readLong();
    }

    @Override
    public long readLongLE() {
        return super.readLongLE();
    }

    @Override
    public char readChar() {
        return super.readChar();
    }

    @Override
    public float readFloat() {
        return super.readFloat();
    }

    @Override
    public double readDouble() {
        return super.readDouble();
    }

    @Override
    public @NotNull ByteBuf readBytes(int i) {
        return super.readBytes(i);
    }

    @Override
    public @NotNull ByteBuf readSlice(int i) {
        return super.readSlice(i);
    }

    @Override
    public @NotNull ByteBuf readRetainedSlice(int i) {
        return super.readRetainedSlice(i);
    }

    @Override
    public @NotNull FriendlyByteBuf readBytes(@NotNull ByteBuf byteBuf) {
        super.readBytes(byteBuf);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf readBytes(@NotNull ByteBuf byteBuf, int i) {
        super.readBytes(byteBuf, i);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf readBytes(@NotNull ByteBuf byteBuf, int i, int i2) {
        super.readBytes(byteBuf, i, i2);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf readBytes(byte @NotNull [] bytes) {
        super.readBytes(bytes);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf readBytes(byte @NotNull [] bytes, int i, int i2) {
        super.readBytes(bytes, i, i2);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf readBytes(@NotNull ByteBuffer byteBuffer) {
        super.readBytes(byteBuffer);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf readBytes(@NotNull OutputStream outputStream, int i) throws IOException {
        super.readBytes(outputStream, i);
        return this;
    }

    @Override
    public int readBytes(@NotNull GatheringByteChannel gatheringByteChannel, int i) throws IOException {
        return super.readBytes(gatheringByteChannel, i);
    }

    @Override
    public @NotNull CharSequence readCharSequence(int i, @NotNull Charset charset) {
        return super.readCharSequence(i, charset);
    }

    @Override
    public int readBytes(@NotNull FileChannel fileChannel, long l, int i) throws IOException {
        return super.readBytes(fileChannel, l, i);
    }

    @Override
    public @NotNull FriendlyByteBuf skipBytes(int i) {
        super.skipBytes(i);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf writeBoolean(boolean flag) {
        super.writeBoolean(flag);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf writeByte(int i) {
        super.writeByte(i);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf writeShort(int i) {
        super.writeShort(i);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf writeShortLE(int i) {
        super.writeShortLE(i);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf writeMedium(int i) {
        super.writeMedium(i);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf writeMediumLE(int i) {
        super.writeMediumLE(i);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf writeInt(int i) {
        super.writeInt(i);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf writeIntLE(int i) {
        super.writeIntLE(i);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf writeLong(long l) {
        super.writeLong(l);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf writeLongLE(long l) {
        super.writeLongLE(l);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf writeChar(int i) {
        super.writeChar(i);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf writeFloat(float f) {
        super.writeFloat(f);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf writeDouble(double d) {
        super.writeDouble(d);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf writeBytes(@NotNull ByteBuf byteBuf) {
        super.writeBytes(byteBuf);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf writeBytes(@NotNull ByteBuf byteBuf, int i) {
        super.writeBytes(byteBuf, i);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf writeBytes(@NotNull ByteBuf byteBuf, int i, int i2) {
        super.writeBytes(byteBuf, i, i2);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf writeBytes(byte @NotNull [] bytes) {
        super.writeBytes(bytes);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf writeBytes(byte @NotNull [] bytes, int i, int i2) {
        super.writeBytes(bytes, i, i2);
        return this;
    }

    @Override
    public @NotNull FriendlyByteBuf writeBytes(@NotNull ByteBuffer byteBuffer) {
        super.writeBytes(byteBuffer);
        return this;
    }

    @Override
    public int writeBytes(@NotNull InputStream inputStream, int i) throws IOException {
        return super.writeBytes(inputStream, i);
    }

    @Override
    public int writeBytes(@NotNull ScatteringByteChannel scatteringByteChannel, int i) throws IOException {
        return super.writeBytes(scatteringByteChannel, i);
    }

    @Override
    public int writeBytes(@NotNull FileChannel fileChannel, long l, int i) throws IOException {
        return super.writeBytes(fileChannel, l, i);
    }

    @Override
    public @NotNull FriendlyByteBuf writeZero(int i) {
        super.writeZero(i);
        return this;
    }

    @Override
    public int writeCharSequence(@NotNull CharSequence charSequence, @NotNull Charset charset) {
        return super.writeCharSequence(charSequence, charset);
    }

    @Override
    public int indexOf(int i, int i2, byte b) {
        return super.indexOf(i, i2, b);
    }

    @Override
    public int bytesBefore(byte b) {
        return super.bytesBefore(b);
    }

    @Override
    public int bytesBefore(int i, byte b) {
        return super.bytesBefore(i, b);
    }

    @Override
    public int bytesBefore(int i, int i2, byte b) {
        return super.bytesBefore(i, i2, b);
    }

    @Override
    public int forEachByte(@NotNull ByteProcessor byteProcessor) {
        return super.forEachByte(byteProcessor);
    }

    @Override
    public int forEachByte(int i, int i2, @NotNull ByteProcessor byteProcessor) {
        return super.forEachByte(i, i2, byteProcessor);
    }

    @Override
    public int forEachByteDesc(@NotNull ByteProcessor byteProcessor) {
        return super.forEachByteDesc(byteProcessor);
    }

    @Override
    public int forEachByteDesc(int i, int i2, @NotNull ByteProcessor byteProcessor) {
        return super.forEachByteDesc(i, i2, byteProcessor);
    }

    @Override
    public @NotNull ByteBuf copy() {
        return super.copy();
    }

    @Override
    public @NotNull ByteBuf copy(int i, int i2) {
        return super.copy(i, i2);
    }

    @Override
    public @NotNull ByteBuf slice() {
        return super.slice();
    }

    @Override
    public @NotNull ByteBuf retainedSlice() {
        return super.retainedSlice();
    }

    @Override
    public @NotNull ByteBuf slice(int i, int i2) {
        return super.slice(i, i2);
    }

    @Override
    public @NotNull ByteBuf retainedSlice(int i, int i2) {
        return super.retainedSlice(i, i2);
    }

    @Override
    public @NotNull ByteBuf duplicate() {
        return super.duplicate();
    }

    @Override
    public @NotNull ByteBuf retainedDuplicate() {
        return super.retainedDuplicate();
    }

    @Override
    public int nioBufferCount() {
        return super.nioBufferCount();
    }

    @Override
    public @NotNull ByteBuffer nioBuffer() {
        return super.nioBuffer();
    }

    @Override
    public @NotNull ByteBuffer nioBuffer(int i, int i2) {
        return super.nioBuffer(i, i2);
    }

    @Override
    public @NotNull ByteBuffer internalNioBuffer(int i, int i2) {
        return super.internalNioBuffer(i, i2);
    }

    @Override
    public ByteBuffer @NotNull [] nioBuffers() {
        return super.nioBuffers();
    }

    @Override
    public ByteBuffer @NotNull [] nioBuffers(int i, int i2) {
        return super.nioBuffers(i, i2);
    }

    @Override
    public boolean hasArray() {
        return super.hasArray();
    }

    @Override
    public byte @NotNull [] array() {
        return super.array();
    }

    @Override
    public int arrayOffset() {
        return super.arrayOffset();
    }

    @Override
    public boolean hasMemoryAddress() {
        return super.hasMemoryAddress();
    }

    @Override
    public long memoryAddress() {
        return super.memoryAddress();
    }

    @Override
    public @NotNull String toString(@NotNull Charset charset) {
        return super.toString(charset);
    }

    @Override
    public @NotNull String toString(int i, int i2, @NotNull Charset charset) {
        return super.toString(i, i2, charset);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    @Override
    public int compareTo(@NotNull ByteBuf byteBuf) {
        return super.compareTo(byteBuf);
    }

    @Override
    public @NotNull String toString() {
        return super.toString();
    }

    @Override
    public @NotNull ByteBuf retain(int i) {
        return super.retain(i);
    }

    @Override
    public @NotNull ByteBuf retain() {
        return super.retain();
    }

    @Override
    public @NotNull ByteBuf touch() {
        return super.touch();
    }

    @Override
    public @NotNull ByteBuf touch(@NotNull Object object) {
        return super.touch(object);
    }

    @Override
    public int refCnt() {
        return super.refCnt();
    }

    @Override
    public boolean release() {
        return super.release();
    }

    @Override
    public boolean release(int i) {
        return super.release(i);
    }

    @Override
    public int maxFastWritableBytes() {
        return super.maxFastWritableBytes();
    }

    @Override
    public float getFloatLE(int index) {
        return super.getFloatLE(index);
    }

    @Override
    public double getDoubleLE(int index) {
        return super.getDoubleLE(index);
    }

    @Override
    @NotNull
    public FriendlyByteBuf setFloatLE(int index, float value) {
        super.setFloatLE(index, value);
        return this;
    }

    @Override
    @NotNull
    public FriendlyByteBuf setDoubleLE(int index, double value) {
        super.setDoubleLE(index, value);
        return this;
    }

    @Override
    public float readFloatLE() {
        return super.readFloatLE();
    }

    @Override
    public double readDoubleLE() {
        return super.readDoubleLE();
    }

    @NotNull
    @Override
    public FriendlyByteBuf writeFloatLE(float value) {
        super.writeFloatLE(value);
        return this;
    }

    @NotNull
    @Override
    public FriendlyByteBuf writeDoubleLE(double value) {
        super.writeDoubleLE(value);
        return this;
    }

    @Override
    public boolean isContiguous() {
        return super.isContiguous();
    }
}
