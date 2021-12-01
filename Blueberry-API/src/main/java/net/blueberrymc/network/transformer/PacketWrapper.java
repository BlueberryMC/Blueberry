package net.blueberrymc.network.transformer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;
import it.unimi.dsi.fastutil.ints.IntList;
import net.blueberrymc.util.IntPair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
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
public class PacketWrapper extends FriendlyByteBuf {
    private final FriendlyByteBuf read;
    private final FriendlyByteBuf write;
    private boolean readIsPassthrough = false;
    private boolean cancelled = false;

    public PacketWrapper(@NotNull ByteBuf read) {
        this(read, Unpooled.buffer());
    }

    public PacketWrapper(@NotNull ByteBuf read, @NotNull ByteBuf write) {
        super(read);
        this.read = new FriendlyByteBuf(read);
        this.write = new FriendlyByteBuf(write);
    }

    public void cancel() {
        cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isReadIsPassthrough() {
        return readIsPassthrough;
    }

    @Override
    public int indexOf(int i, int i2, byte b) {
        return read.indexOf(i, i2, b);
    }

    @Override
    public int bytesBefore(byte b) {
        return read.bytesBefore(b);
    }

    @Override
    public int bytesBefore(int i, byte b) {
        return read.bytesBefore(i, b);
    }

    @Override
    public int bytesBefore(int i, int i2, byte b) {
        return read.bytesBefore(i, i2, b);
    }

    @Override
    public int forEachByte(@NotNull ByteProcessor byteProcessor) {
        return read.forEachByte(byteProcessor);
    }

    @Override
    public int forEachByte(int i, int i2, @NotNull ByteProcessor byteProcessor) {
        return read.forEachByte(i, i2, byteProcessor);
    }

    @Override
    public int forEachByteDesc(@NotNull ByteProcessor byteProcessor) {
        return read.forEachByteDesc(byteProcessor);
    }

    @Override
    public int forEachByteDesc(int i, int i2, @NotNull ByteProcessor byteProcessor) {
        return read.forEachByteDesc(i, i2, byteProcessor);
    }

    @Override
    public @NotNull ByteBuf copy() {
        return read.copy();
    }

    @Override
    public @NotNull ByteBuf copy(int i, int i2) {
        return read.copy(i, i2);
    }

    @Override
    public @NotNull ByteBuf slice() {
        return read.slice();
    }

    @Override
    public @NotNull ByteBuf retainedSlice() {
        return read.retainedSlice();
    }

    @Override
    public @NotNull ByteBuf slice(int i, int i2) {
        return read.slice(i, i2);
    }

    @Override
    public @NotNull ByteBuf retainedSlice(int i, int i2) {
        return read.retainedSlice(i, i2);
    }

    @Override
    public @NotNull ByteBuf duplicate() {
        return read.duplicate();
    }

    @Override
    public @NotNull ByteBuf retainedDuplicate() {
        return read.retainedDuplicate();
    }

    @Override
    public int nioBufferCount() {
        return read.nioBufferCount();
    }

    @Override
    public @NotNull ByteBuffer nioBuffer() {
        return read.nioBuffer();
    }

    @Override
    public @NotNull ByteBuffer nioBuffer(int i, int i2) {
        return read.nioBuffer(i, i2);
    }

    @Override
    public @NotNull ByteBuffer internalNioBuffer(int i, int i2) {
        return read.internalNioBuffer(i, i2);
    }

    @Override
    public ByteBuffer @NotNull [] nioBuffers() {
        return read.nioBuffers();
    }

    @Override
    public ByteBuffer @NotNull [] nioBuffers(int i, int i2) {
        return read.nioBuffers(i, i2);
    }

    @Override
    public boolean hasArray() {
        return read.hasArray();
    }

    @Override
    public byte @NotNull [] array() {
        return read.array();
    }

    @Override
    public int arrayOffset() {
        return read.arrayOffset();
    }

    @Override
    public boolean hasMemoryAddress() {
        return read.hasMemoryAddress();
    }

    @Override
    public long memoryAddress() {
        return read.memoryAddress();
    }

    @Override
    public @NotNull String toString(@NotNull Charset charset) {
        return read.toString(charset);
    }

    @Override
    public @NotNull String toString(int i, int i2, @NotNull Charset charset) {
        return read.toString(i, i2, charset);
    }

    @Override
    public @NotNull ByteBuf retain(int i) {
        return read.retain(i);
    }

    @Override
    public @NotNull ByteBuf retain() {
        return read.retain();
    }

    @Override
    public @NotNull ByteBuf touch() {
        return read.touch();
    }

    @Override
    public @NotNull ByteBuf touch(@NotNull Object object) {
        return read.touch(object);
    }

    @Override
    public boolean release() {
        return read.release() && write.release();
    }

    @Override
    public boolean release(int i) {
        return read.release(i) && write.release(i);
    }

    @Override
    public @NotNull ByteBuf setBoolean(int i, boolean flag) {
        return write.setBoolean(i, flag);
    }

    @Override
    public @NotNull ByteBuf setByte(int i, int i2) {
        return write.setByte(i, i2);
    }

    @Override
    public @NotNull ByteBuf setShort(int i, int i2) {
        return write.setShort(i, i2);
    }

    @Override
    public @NotNull ByteBuf setShortLE(int i, int i2) {
        return write.setShortLE(i, i2);
    }

    @Override
    public @NotNull ByteBuf setMedium(int i, int i2) {
        return write.setMedium(i, i2);
    }

    @Override
    public @NotNull ByteBuf setMediumLE(int i, int i2) {
        return write.setMediumLE(i, i2);
    }

    @Override
    public @NotNull ByteBuf setInt(int i, int i2) {
        return write.setInt(i, i2);
    }

    @Override
    public @NotNull ByteBuf setIntLE(int i, int i2) {
        return write.setIntLE(i, i2);
    }

    @Override
    public @NotNull ByteBuf setLong(int i, long l) {
        return write.setLong(i, l);
    }

    @Override
    public @NotNull ByteBuf setLongLE(int i, long l) {
        return write.setLongLE(i, l);
    }

    @Override
    public @NotNull ByteBuf setChar(int i, int i2) {
        return write.setChar(i, i2);
    }

    @Override
    public @NotNull ByteBuf setFloat(int i, float f) {
        return write.setFloat(i, f);
    }

    @Override
    public @NotNull ByteBuf setDouble(int i, double d) {
        return write.setDouble(i, d);
    }

    @Override
    public @NotNull ByteBuf setBytes(int i, @NotNull ByteBuf byteBuf) {
        return write.setBytes(i, byteBuf);
    }

    @Override
    public @NotNull ByteBuf setBytes(int i, @NotNull ByteBuf byteBuf, int i2) {
        return write.setBytes(i, byteBuf, i2);
    }

    @Override
    public @NotNull ByteBuf setBytes(int i, @NotNull ByteBuf byteBuf, int i2, int i3) {
        return write.setBytes(i, byteBuf, i2, i3);
    }

    @Override
    public @NotNull ByteBuf setBytes(int i, byte @NotNull [] bytes) {
        return write.setBytes(i, bytes);
    }

    @Override
    public @NotNull ByteBuf setBytes(int i, byte @NotNull [] bytes, int i2, int i3) {
        return write.setBytes(i, bytes, i2, i3);
    }

    @Override
    public @NotNull ByteBuf setBytes(int i, @NotNull ByteBuffer byteBuffer) {
        return write.setBytes(i, byteBuffer);
    }

    @Override
    public int setBytes(int i, @NotNull InputStream inputStream, int i2) throws IOException {
        return write.setBytes(i, inputStream, i2);
    }

    @Override
    public int setBytes(int i, @NotNull ScatteringByteChannel scatteringByteChannel, int i2) throws IOException {
        return write.setBytes(i, scatteringByteChannel, i2);
    }

    @Override
    public int setBytes(int i, @NotNull FileChannel fileChannel, long l, int i2) throws IOException {
        return write.setBytes(i, fileChannel, l, i2);
    }

    @Override
    public @NotNull ByteBuf setZero(int i, int i2) {
        return write.setZero(i, i2);
    }

    @Override
    public int setCharSequence(int i, @NotNull CharSequence charSequence, @NotNull Charset charset) {
        return write.setCharSequence(i, charSequence, charset);
    }

    @Override
    public int capacity() {
        return read.capacity();
    }

    @NotNull
    @Override
    public ByteBuf capacity(int i) {
        return read.capacity(i);
    }

    @Override
    public int maxCapacity() {
        return read.maxCapacity();
    }

    @NotNull
    @Override
    public ByteBufAllocator alloc() {
        return read.alloc();
    }

    @NotNull
    @Override
    public ByteOrder order() {
        return read.order();
    }

    @NotNull
    @Override
    public ByteBuf order(@NotNull ByteOrder byteOrder) {
        return read.order(byteOrder);
    }

    @NotNull
    @Override
    public ByteBuf unwrap() {
        return read.unwrap();
    }

    @Override
    public boolean isDirect() {
        return read.isDirect();
    }

    @Override
    public boolean isReadOnly() {
        return read.isReadOnly();
    }

    @NotNull
    @Override
    public ByteBuf asReadOnly() {
        return read.asReadOnly();
    }

    @Override
    public boolean getBoolean(int i) {
        return read.getBoolean(i);
    }

    @Override
    public byte getByte(int i) {
        return read.getByte(i);
    }

    @Override
    public short getUnsignedByte(int i) {
        return read.getUnsignedByte(i);
    }

    @Override
    public short getShort(int i) {
        return read.getShort(i);
    }

    @Override
    public short getShortLE(int i) {
        return read.getShortLE(i);
    }

    @Override
    public int getUnsignedShort(int i) {
        return read.getUnsignedShort(i);
    }

    @Override
    public int getUnsignedShortLE(int i) {
        return read.getUnsignedShortLE(i);
    }

    @Override
    public int getMedium(int i) {
        return read.getMedium(i);
    }

    @Override
    public int getMediumLE(int i) {
        return read.getMediumLE(i);
    }

    @Override
    public int getUnsignedMedium(int i) {
        return read.getUnsignedMedium(i);
    }

    @Override
    public int getUnsignedMediumLE(int i) {
        return read.getUnsignedMediumLE(i);
    }

    @Override
    public int getInt(int i) {
        return read.getInt(i);
    }

    @Override
    public int getIntLE(int i) {
        return read.getIntLE(i);
    }

    @Override
    public long getUnsignedInt(int i) {
        return read.getUnsignedInt(i);
    }

    @Override
    public long getUnsignedIntLE(int i) {
        return read.getUnsignedIntLE(i);
    }

    @Override
    public long getLong(int i) {
        return read.getLong(i);
    }

    @Override
    public long getLongLE(int i) {
        return read.getLongLE(i);
    }

    @Override
    public char getChar(int i) {
        return read.getChar(i);
    }

    @Override
    public float getFloat(int i) {
        return read.getFloat(i);
    }

    @Override
    public double getDouble(int i) {
        return read.getDouble(i);
    }

    @NotNull
    @Override
    public ByteBuf getBytes(int i, @NotNull ByteBuf byteBuf) {
        return read.getBytes(i, byteBuf);
    }

    @NotNull
    @Override
    public ByteBuf getBytes(int i, @NotNull ByteBuf byteBuf, int i2) {
        return read.getBytes(i, byteBuf, i2);
    }

    @NotNull
    @Override
    public ByteBuf getBytes(int i, @NotNull ByteBuf byteBuf, int i2, int i3) {
        return read.getBytes(i, byteBuf, i2, i3);
    }

    @NotNull
    @Override
    public ByteBuf getBytes(int i, byte@NotNull[] bytes) {
        return read.getBytes(i, bytes);
    }

    @NotNull
    @Override
    public ByteBuf getBytes(int i, byte@NotNull[] bytes, int i2, int i3) {
        return read.getBytes(i, bytes, i2, i3);
    }

    @NotNull
    @Override
    public ByteBuf getBytes(int i, @NotNull ByteBuffer byteBuffer) {
        return read.getBytes(i, byteBuffer);
    }

    @NotNull
    @Override
    public ByteBuf getBytes(int i, @NotNull OutputStream outputStream, int i2) throws IOException {
        return read.getBytes(i, outputStream, i2);
    }

    @Override
    public int getBytes(int i, @NotNull GatheringByteChannel gatheringByteChannel, int i2) throws IOException {
        return read.getBytes(i, gatheringByteChannel, i2);
    }

    @Override
    public int getBytes(int i, @NotNull FileChannel fileChannel, long l, int i2) throws IOException {
        return read.getBytes(i, fileChannel, l, i2);
    }

    @NotNull
    @Override
    public CharSequence getCharSequence(int i, int i2, @NotNull Charset charset) {
        return read.getCharSequence(i, i2, charset);
    }

    @NotNull
    @Override
    public ByteBuf setIndex(int i, int i2) {
        return read.setIndex(i, i2);
    }

    @NotNull
    @Override
    public ByteBuf clear() {
        return read.clear();
    }

    @NotNull
    @Override
    public ByteBuf discardReadBytes() {
        return read.discardReadBytes();
    }

    @NotNull
    @Override
    public ByteBuf discardSomeReadBytes() {
        return read.discardSomeReadBytes();
    }

    @NotNull
    @Override
    public ByteBuf ensureWritable(int i) {
        return write.ensureWritable(i);
    }

    @Override
    public int ensureWritable(int i, boolean flag) {
        return write.ensureWritable(i, flag);
    }

    @Override
    public int refCnt() {
        return read.refCnt();
    }

    public void setReadIsPassthrough(boolean readIsPassthrough) {
        this.readIsPassthrough = readIsPassthrough;
    }

    public void readIsPassthrough(@NotNull Runnable runnable) {
        setReadIsPassthrough(true);
        try {
            runnable.run();
        } finally {
            setReadIsPassthrough(false);
        }
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
    public PacketWrapper writeBytes(byte@NotNull[] bytes) {
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
    public PacketWrapper writeBytes(byte@NotNull[] bytes, int i, int i2) {
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
    public PacketWrapper writeByteArray(byte@NotNull[] bytes) {
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
    public PacketWrapper writeLongArray(long@NotNull[] longs) {
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
    public PacketWrapper writeVarIntArray(int@NotNull[] intArray) {
        write.writeVarIntArray(intArray);
        return this;
    }

    @NotNull
    public PacketWrapper writeVarLong(long l) {
        write.writeVarLong(l);
        return this;
    }

    public boolean readBoolean() {
        if (readIsPassthrough) return passthroughBoolean();
        return read.readBoolean();
    }

    public byte readByte() {
        if (readIsPassthrough) return passthroughByte();
        return read.readByte();
    }

    public short readUnsignedByte() {
        if (readIsPassthrough) return passthroughUnsignedByte();
        return read.readUnsignedByte();
    }

    public short readShort() {
        if (readIsPassthrough) return passthroughShort();
        return read.readShort();
    }

    public short readShortLE() {
        if (readIsPassthrough) return passthroughShortLE();
        return read.readShortLE();
    }

    public int readUnsignedShort() {
        if (readIsPassthrough) return passthroughUnsignedShort();
        return read.readUnsignedShort();
    }

    public int readUnsignedShortLE() {
        if (readIsPassthrough) return passthroughUnsignedShortLE();
        return read.readUnsignedShortLE();
    }

    public int readMedium() {
        if (readIsPassthrough) return passthroughMedium();
        return read.readMedium();
    }

    public int readMediumLE() {
        if (readIsPassthrough) return passthroughMediumLE();
        return read.readMediumLE();
    }

    public int readUnsignedMedium() {
        if (readIsPassthrough) return passthroughUnsignedMedium();
        return read.readUnsignedMedium();
    }

    public int readUnsignedMediumLE() {
        if (readIsPassthrough) return passthroughUnsignedMediumLE();
        return read.readUnsignedMediumLE();
    }

    public int readInt() {
        if (readIsPassthrough) return passthroughInt();
        return read.readInt();
    }

    public int readIntLE() {
        if (readIsPassthrough) return passthroughIntLE();
        return read.readIntLE();
    }

    public long readUnsignedInt() {
        if (readIsPassthrough) return passthroughUnsignedInt();
        return read.readUnsignedInt();
    }

    public long readUnsignedIntLE() {
        if (readIsPassthrough) return passthroughUnsignedIntLE();
        return read.readUnsignedIntLE();
    }

    public long readLong() {
        if (readIsPassthrough) return passthroughLong();
        return read.readLong();
    }

    public long readLongLE() {
        if (readIsPassthrough) return passthroughLongLE();
        return read.readLongLE();
    }

    public char readChar() {
        if (readIsPassthrough) return passthroughChar();
        return read.readChar();
    }

    public float readFloat() {
        if (readIsPassthrough) return passthroughFloat();
        return read.readFloat();
    }

    public double readDouble() {
        if (readIsPassthrough) return passthroughDouble();
        return read.readDouble();
    }

    @NotNull
    public ByteBuf readBytes(int i) {
        if (readIsPassthrough) return passthroughBytes(i);
        return read.readBytes(i);
    }

    @NotNull
    public ByteBuf readSlice(int i) {
        if (readIsPassthrough) return passthroughSlice(i);
        return read.readSlice(i);
    }

    @NotNull
    public ByteBuf readRetainedSlice(int i) {
        if (readIsPassthrough) return passthroughRetainedSlice(i);
        return read.readRetainedSlice(i);
    }

    @NotNull
    public PacketWrapper readBytes(@NotNull ByteBuf byteBuf) {
        if (readIsPassthrough) {
            passthroughBytes(byteBuf);
            return this;
        }
        read.readBytes(byteBuf);
        return this;
    }

    @NotNull
    public PacketWrapper readBytes(@NotNull ByteBuf byteBuf, int length) {
        if (readIsPassthrough) {
            passthroughBytes(byteBuf, length);
            return this;
        }
        read.readBytes(byteBuf, length);
        return this;
    }

    @NotNull
    public PacketWrapper readBytes(@NotNull ByteBuf byteBuf, int dstIndex, int length) {
        if (readIsPassthrough) {
            passthroughBytes(byteBuf, dstIndex, length);
            return this;
        }
        read.readBytes(byteBuf, dstIndex, length);
        return this;
    }

    @NotNull
    public PacketWrapper readBytes(byte@NotNull[] bytes) {
        if (readIsPassthrough) {
            passthroughBytes(bytes);
            return this;
        }
        read.readBytes(bytes);
        return this;
    }

    @NotNull
    public PacketWrapper readBytes(byte@NotNull[] bytes, int dstIndex, int length) {
        if (readIsPassthrough) {
            passthroughBytes(bytes, dstIndex, length);
            return this;
        }
        read.readBytes(bytes, dstIndex, length);
        return this;
    }

    @NotNull
    public PacketWrapper readBytes(@NotNull ByteBuffer dst) {
        if (readIsPassthrough) {
            int pos = dst.position();
            read.readBytes(dst);
            int pos2 = dst.position();
            dst.position(pos);
            write.writeBytes(dst);
            dst.position(pos2);
            return this;
        }
        read.readBytes(dst);
        return this;
    }

    // no readIsPassthrough
    @NotNull
    public PacketWrapper readBytes(@NotNull OutputStream outputStream, int i) throws IOException {
        read.readBytes(outputStream, i);
        return this;
    }

    // no readIsPassthrough
    public int readBytes(@NotNull GatheringByteChannel gatheringByteChannel, int i) throws IOException {
        return read.readBytes(gatheringByteChannel, i);
    }

    @NotNull
    public CharSequence readCharSequence(int i, @NotNull Charset charset) {
        if (readIsPassthrough) return passthroughCharSequence(i, charset);
        return read.readCharSequence(i, charset);
    }

    // no readIsPassthrough
    public int readBytes(@NotNull FileChannel fileChannel, long l, int i) throws IOException {
        return read.readBytes(fileChannel, l, i);
    }

    @NotNull
    public PacketWrapper skipBytes(int i) {
        read.skipBytes(i);
        write.skipBytes(i);
        return this;
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
        if (readIsPassthrough) return passthroughWithCodec(codec);
        return read.readWithCodec(codec);
    }

    public <T> void writeWithCodec(@NotNull Codec<T> codec, @NotNull T object) {
        write.writeWithCodec(codec, object);
    }

    @NotNull
    public <T, C extends Collection<T>> C readCollection(@NotNull IntFunction<C> toCollectionFunction, @NotNull Function<FriendlyByteBuf, T> valueFunction) {
        if (readIsPassthrough) return passthroughCollection(toCollectionFunction, valueFunction);
        return read.readCollection(toCollectionFunction, valueFunction);
    }

    public <T> void writeCollection(@NotNull Collection<T> collection, @NotNull BiConsumer<FriendlyByteBuf, T> biConsumer) {
        write.writeCollection(collection, biConsumer);
    }

    @NotNull
    public <T> List<T> readList(@NotNull Function<FriendlyByteBuf, T> valueFunction) {
        return this.readCollection(Lists::newArrayListWithCapacity, valueFunction);
    }

    @NotNull
    public IntList readIntIdList() {
        if (readIsPassthrough) return passthroughIntIdList();
        return read.readIntIdList();
    }

    public void writeIntIdList(@NotNull IntList intList) {
        write.writeIntIdList(intList);
    }

    @NotNull
    public <K, V, M extends Map<K, V>> M readMap(@NotNull IntFunction<M> toMapFunction, @NotNull Function<FriendlyByteBuf, K> keyFunction, @NotNull Function<FriendlyByteBuf, V> valueFunction) {
        if (readIsPassthrough) return passthroughMap(toMapFunction, keyFunction, valueFunction);
        return read.readMap(toMapFunction, keyFunction, valueFunction);
    }

    @NotNull
    public <K, V> Map<K, V> readMap(@NotNull Function<FriendlyByteBuf, K> keyFunction, @NotNull Function<FriendlyByteBuf, V> valueFunction) {
        return this.readMap(Maps::newHashMapWithExpectedSize, keyFunction, valueFunction);
    }

    public <K, V> void writeMap(@NotNull Map<K, V> map, @NotNull BiConsumer<FriendlyByteBuf, K> biConsumer, @NotNull BiConsumer<FriendlyByteBuf, V> biConsumer2) {
        write.writeMap(map, biConsumer, biConsumer2);
    }

    public void readWithCount(@NotNull Consumer<FriendlyByteBuf> consumer) {
        if (readIsPassthrough) {
            passthroughWithCount(consumer);
            return;
        }
        read.readWithCount(consumer);
    }

    public <T> void writeOptional(@NotNull Optional<T> optional, @NotNull BiConsumer<FriendlyByteBuf, T> biConsumer) {
        write.writeOptional(optional, biConsumer);
    }

    @NotNull
    public <T> Optional<T> readOptional(@NotNull Function<FriendlyByteBuf, T> function) {
        return this.readBoolean() ? Optional.of(function.apply(this)) : Optional.empty();
    }

    public byte@NotNull[] readByteArray() {
        if (readIsPassthrough) return passthroughByteArray();
        return read.readByteArray();
    }

    public byte@NotNull[] readByteArray(int i) {
        if (readIsPassthrough) return passthroughByteArray(i);
        return read.readByteArray(i);
    }

    public int@NotNull[] readVarIntArray() {
        if (readIsPassthrough) return passthroughVarIntArray();
        return read.readVarIntArray();
    }

    public int@NotNull[] readVarIntArray(int i) {
        if (readIsPassthrough) return passthroughVarIntArray(i);
        return read.readVarIntArray(i);
    }

    public long@NotNull[] readLongArray() {
        if (readIsPassthrough) return passthroughLongArray();
        return read.readLongArray();
    }

    public long@NotNull[] readLongArray(long[] longs) {
        if (readIsPassthrough) return passthroughLongArray(longs);
        return read.readLongArray(longs);
    }

    public long@NotNull[] readLongArray(long[] longs, int i) {
        if (readIsPassthrough) return passthroughLongArray(longs, i);
        return read.readLongArray(longs, i);
    }

    public byte@NotNull[] accessByteBufWithCorrectSize() {
        return read.accessByteBufWithCorrectSize();
    }

    @NotNull
    public BlockPos readBlockPos() {
        if (readIsPassthrough) return passthroughBlockPos();
        return read.readBlockPos();
    }

    @NotNull
    public ChunkPos readChunkPos() {
        if (readIsPassthrough) return passthroughChunkPos();
        return read.readChunkPos();
    }

    @NotNull
    public SectionPos readSectionPos() {
        if (readIsPassthrough) return passthroughSectionPos();
        return read.readSectionPos();
    }

    @NotNull
    public Component readComponent() {
        if (readIsPassthrough) return passthroughComponent();
        return read.readComponent();
    }

    @NotNull
    public <T extends Enum<T>> T readEnum(@NotNull Class<T> clazz) {
        return clazz.getEnumConstants()[this.readVarInt()];
    }

    public int readVarInt() {
        if (readIsPassthrough) return passthroughVarInt();
        return read.readVarInt();
    }

    public long readVarLong() {
        if (readIsPassthrough) return passthroughVarLong();
        return read.readVarLong();
    }

    @NotNull
    public UUID readUUID() {
        if (readIsPassthrough) return passthroughUUID();
        return read.readUUID();
    }

    @Nullable
    public CompoundTag readNbt() {
        if (readIsPassthrough) return passthroughNbt();
        return read.readNbt();
    }

    @Nullable
    public CompoundTag readAnySizeNbt() {
        if (readIsPassthrough) return passthroughAnySizeNbt();
        return read.readAnySizeNbt();
    }

    @Nullable
    public CompoundTag readNbt(@NotNull NbtAccounter nbtAccounter) {
        if (readIsPassthrough) return passthroughNbt(nbtAccounter);
        return read.readNbt(nbtAccounter);
    }

    @NotNull
    public ItemStack readItem() {
        if (readIsPassthrough) return passthroughItem();
        return read.readItem();
    }

    @NotNull
    public String readUtf() {
        if (readIsPassthrough) return passthroughUtf();
        return read.readUtf();
    }

    @NotNull
    public String readUtf(int i) {
        if (readIsPassthrough) return passthroughUtf(i);
        return read.readUtf(i);
    }

    @NotNull
    public ResourceLocation readResourceLocation() {
        if (readIsPassthrough) return passthroughResourceLocation();
        return read.readResourceLocation();
    }

    @NotNull
    public Date readDate() {
        if (readIsPassthrough) return passthroughDate();
        return read.readDate();
    }

    @NotNull
    public BlockHitResult readBlockHitResult() {
        if (readIsPassthrough) return passthroughBlockHitResult();
        return read.readBlockHitResult();
    }

    public void writeBlockHitResult(@NotNull BlockHitResult blockHitResult) {
        write.writeBlockHitResult(blockHitResult);
    }

    @NotNull
    public PacketWrapper writeBlockHitResultAndReturnSelf(@NotNull BlockHitResult blockHitResult) {
        write.writeBlockHitResult(blockHitResult);
        return this;
    }

    @NotNull
    public BitSet readBitSet() {
        if (readIsPassthrough) return passthroughBitSet();
        return write.readBitSet();
    }

    public void writeBitSet(@NotNull BitSet bitSet) {
        write.writeBitSet(bitSet);
    }

    @NotNull
    public PacketWrapper writeBitSetAndReturnSelf(@NotNull BitSet bitSet) {
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
    public String passthroughUtf() {
        String s = read.readUtf();
        write.writeUtf(s);
        return s;
    }

    @NotNull
    public String passthroughUtf(int maxLength) {
        String s = read.readUtf(maxLength);
        write.writeUtf(s);
        return s;
    }

    public byte passthroughByte() {
        byte b = read.readByte();
        write.writeByte(b);
        return b;
    }

    public short passthroughShort() {
        short s = read.readShort();
        write.writeShort(s);
        return s;
    }

    public short passthroughShortLE() {
        short s = read.readShortLE();
        write.writeShortLE(s);
        return s;
    }

    public int passthroughUnsignedShort() {
        int i = read.readUnsignedShort();
        write.writeShort(i);
        return i;
    }

    public int passthroughUnsignedShortLE() {
        int i = read.readUnsignedShortLE();
        write.writeShortLE(i);
        return i;
    }

    public int passthroughMedium() {
        int i = read.readMedium();
        write.writeMedium(i);
        return i;
    }

    public int passthroughMediumLE() {
        int i = read.readMediumLE();
        write.writeMediumLE(i);
        return i;
    }

    public int passthroughUnsignedMedium() {
        int i = read.readUnsignedMedium();
        write.writeMedium(i);
        return i;
    }

    public int passthroughUnsignedMediumLE() {
        int i = read.readUnsignedMediumLE();
        write.writeMediumLE(i);
        return i;
    }

    public short passthroughUnsignedByte() {
        short s = read.readUnsignedByte();
        write.writeByte(s);
        return s;
    }

    public int passthroughVarInt() {
        int i = read.readVarInt();
        write.writeVarInt(i);
        return i;
    }

    public long passthroughVarLong() {
        long l = read.readVarLong();
        write.writeVarLong(l);
        return l;
    }

    public int passthroughInt() {
        int i = read.readInt();
        write.writeInt(i);
        return i;
    }

    public int passthroughIntLE() {
        int i = read.readIntLE();
        write.writeIntLE(i);
        return i;
    }

    public long passthroughUnsignedInt() {
        long l = read.readUnsignedInt();
        write.writeInt((int) l);
        return l;
    }

    public long passthroughUnsignedIntLE() {
        long l = read.readUnsignedIntLE();
        write.writeIntLE((int) l);
        return l;
    }

    public boolean passthroughBoolean() {
        boolean b = read.readBoolean();
        write.writeBoolean(b);
        return b;
    }

    public long passthroughLong() {
        long l = read.readLong();
        write.writeLong(l);
        return l;
    }

    public long passthroughLongLE() {
        long l = read.readLongLE();
        write.writeLongLE(l);
        return l;
    }

    public char passthroughChar() {
        char c = read.readChar();
        write.writeChar(c);
        return c;
    }

    public float passthroughFloat() {
        float f = read.readFloat();
        write.writeFloat(f);
        return f;
    }

    public float passthroughFloatLE() {
        float f = read.readFloatLE();
        write.writeFloatLE(f);
        return f;
    }

    public double passthroughDouble() {
        double d = read.readDouble();
        write.writeDoubleLE(d);
        return d;
    }

    public double passthroughDoubleLE() {
        double d = read.readDoubleLE();
        write.writeDoubleLE(d);
        return d;
    }

    @NotNull
    public ByteBuf passthroughBytes(int i) {
        ByteBuf buf = read.readBytes(i);
        write.writeBytes(buf);
        return buf;
    }

    @NotNull
    public ByteBuf passthroughSlice(int i) {
        ByteBuf buf = read.readSlice(i);
        write.writeBytes(buf);
        return buf;
    }

    @NotNull
    public ByteBuf passthroughRetainedSlice(int i) {
        ByteBuf buf = read.readRetainedSlice(i);
        write.writeBytes(buf);
        return buf;
    }

    @NotNull
    public ByteBuf passthroughBytes(@NotNull ByteBuf buf) {
        read.readBytes(buf);
        write.writeBytes(buf);
        return buf;
    }

    @NotNull
    public ByteBuf passthroughBytes(@NotNull ByteBuf buf, int length) {
        read.readBytes(buf, length);
        write.writeBytes(buf, length);
        return buf;
    }

    @NotNull
    public ByteBuf passthroughBytes(@NotNull ByteBuf buf, int dstIndex, int length) {
        read.readBytes(buf, dstIndex, length);
        write.writeBytes(buf, dstIndex, length);
        return buf;
    }

    public byte@NotNull[] passthroughBytes(byte@NotNull[] bytes) {
        read.readBytes(bytes);
        write.writeBytes(bytes);
        return bytes;
    }

    public byte@NotNull[] passthroughBytes(byte@NotNull[] bytes, int dstIndex, int length) {
        if (bytes.length == length) {
            return passthroughBytes(bytes);
        }
        read.readBytes(bytes, dstIndex, length);
        write.writeBytes(bytes, dstIndex, length);
        return bytes;
    }

    @NotNull
    public CharSequence passthroughCharSequence(int i, @NotNull Charset charset) {
        CharSequence cs = read.readCharSequence(i, charset);
        write.writeCharSequence(cs, charset);
        return cs;
    }

    @NotNull
    public <T> T passthroughWithCodec(@NotNull Codec<T> codec) {
        T object = read.readWithCodec(codec);
        write.writeWithCodec(codec, object);
        return object;
    }

    @NotNull
    public IntList passthroughIntIdList() {
        IntList intList = read.readIntIdList();
        write.writeIntIdList(intList);
        return intList;
    }

    @NotNull
    public <T, C extends Collection<T>> C passthroughCollection(@NotNull IntFunction<C> toCollectionFunction, @NotNull Function<FriendlyByteBuf, T> valueFunction) {
        int length = this.passthroughVarInt();
        C collection = toCollectionFunction.apply(length);
        for (int i = 0; i < length; ++i) {
            collection.add(valueFunction.apply(this));
        }
        return collection;
    }

    @NotNull
    public <K, V, M extends Map<K, V>> M passthroughMap(@NotNull IntFunction<M> toMapFunction, @NotNull Function<FriendlyByteBuf, K> keyFunction, @NotNull Function<FriendlyByteBuf, V> valueFunction) {
        int length = passthroughVarInt();
        M map = toMapFunction.apply(length);
        for (int i = 0; i < length; ++i) {
            K key = keyFunction.apply(this);
            V value = valueFunction.apply(this);
            map.put(key, value);
        }
        return map;
    }

    public void passthroughWithCount(@NotNull Consumer<FriendlyByteBuf> consumer) {
        int count = passthroughVarInt();
        for (int i = 0; i < count; i++) {
            consumer.accept(this);
        }
    }

    public byte@NotNull[] passthroughByteArray() {
        byte[] bytes = read.readByteArray();
        write.writeByteArray(bytes);
        return bytes;
    }

    public byte@NotNull[] passthroughByteArray(int i) {
        byte[] bytes = read.readByteArray();
        write.writeByteArray(bytes);
        return bytes;
    }

    public int@NotNull[] passthroughVarIntArray() {
        int[] ints = read.readVarIntArray();
        write.writeVarIntArray(ints);
        return ints;
    }

    public int@NotNull[] passthroughVarIntArray(int i) {
        int[] ints = read.readVarIntArray(i);
        write.writeVarIntArray(ints);
        return ints;
    }

    public long@NotNull[] passthroughLongArray() {
        long[] longs = read.readLongArray();
        write.writeLongArray(longs);
        return longs;
    }

    public long@NotNull[] passthroughLongArray(long[] arr) {
        long[] longs = read.readLongArray(arr);
        write.writeLongArray(longs);
        return longs;
    }

    public long@NotNull[] passthroughLongArray(long[] arr, int i) {
        long[] longs = read.readLongArray(arr, i);
        write.writeLongArray(longs);
        return longs;
    }

    @NotNull
    public BlockPos passthroughBlockPos() {
        BlockPos blockPos = read.readBlockPos();
        write.writeBlockPos(blockPos);
        return blockPos;
    }

    @NotNull
    public ChunkPos passthroughChunkPos() {
        ChunkPos chunkPos = read.readChunkPos();
        write.writeChunkPos(chunkPos);
        return chunkPos;
    }

    @NotNull
    public SectionPos passthroughSectionPos() {
        SectionPos sectionPos = read.readSectionPos();
        write.writeSectionPos(sectionPos);
        return sectionPos;
    }

    @NotNull
    public Component passthroughComponent() {
        Component component = read.readComponent();
        write.writeComponent(component);
        return component;
    }

    @NotNull
    public UUID passthroughUUID() {
        UUID uuid = read.readUUID();
        write.writeUUID(uuid);
        return uuid;
    }

    @Nullable
    public CompoundTag passthroughNbt() {
        return passthroughNbt(new NbtAccounter(2097152L));
    }

    @Nullable
    public CompoundTag passthroughAnySizeNbt() {
        CompoundTag tag = read.readAnySizeNbt();
        write.writeNbt(tag);
        return tag;
    }

    @Nullable
    public CompoundTag passthroughNbt(@NotNull NbtAccounter nbtAccounter) {
        int readerIndex = this.readerIndex();
        int writerIndex = this.writerIndex();
        byte type = passthroughByte();
        if (type == 0) {
            return null;
        } else {
            this.readerIndex(readerIndex);
            this.writerIndex(writerIndex);
            try {
                CompoundTag tag = NbtIo.read(new ByteBufInputStream(this), nbtAccounter);
                if (!readIsPassthrough) write.writeNbt(tag);
                return tag;
            } catch (IOException var5) {
                throw new EncoderException(var5);
            }
        }
    }

    @NotNull
    public ItemStack passthroughItem() {
        if (passthroughBoolean()) {
            int id = passthroughVarInt();
            int count = passthroughByte();
            ItemStack itemStack = new ItemStack(Item.byId(id), count);
            itemStack.setTag(passthroughNbt());
            return itemStack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @NotNull
    public PacketWrapper passthroughEntityMetadataValue(int type) {
        if (type == 0) passthrough(Type.BYTE); // Byte
        if (type == 1) passthrough(Type.VAR_INT); // VarInt
        if (type == 2) passthrough(Type.FLOAT); // Float
        if (type == 3) passthrough(Type.STRING); // String
        if (type == 4) passthrough(Type.COMPONENT); // Chat
        if (type == 5 && passthroughBoolean()) passthrough(Type.COMPONENT); // Optional Chat
        if (type == 6) passthrough(Type.ITEM); // Slot (Item)
        if (type == 7) passthrough(Type.BOOLEAN); // Boolean
        if (type == 8) passthrough(Type.FLOAT).passthrough(Type.FLOAT).passthrough(Type.FLOAT); // Rotation (3 floats)
        if (type == 9) passthrough(Type.BLOCK_POS); // Position
        if (type == 10 && passthroughBoolean()) passthrough(Type.BLOCK_POS); // Position
        if (type == 11) passthrough(Type.VAR_INT); // Direction (VarInt)
        if (type == 12 && passthroughBoolean()) passthrough(Type.UUID); // Optional UUID
        if (type == 13) passthrough(Type.VAR_INT); // Optional Block ID (VarInt) (0 for absent)
        if (type == 14) passthrough(Type.NBT); // NBT
        if (type == 15) passthroughParticle(readVarInt()); // Particle
        if (type == 16) passthrough(Type.VAR_INT).passthrough(Type.VAR_INT).passthrough(Type.VAR_INT); // Villager Data (type, profession, level)
        if (type == 17) passthrough(Type.VAR_INT); // Optional VarInt (0 for absent); used for entity IDs
        if (type == 18) passthrough(Type.VAR_INT); // Pose (VarInt)
        return this;
    }

    @NotNull
    public PacketWrapper passthroughParticle(int id) {
        if (id == 4) { // minecraft:block
            passthrough(Type.VAR_INT); // BlockState
        }
        if (id == 15) { // minecraft:dust
            passthrough(Type.FLOAT); // Red, 0-1
            passthrough(Type.FLOAT); // Green, 0-1
            passthrough(Type.FLOAT); // Blue, 0-1
            passthrough(Type.FLOAT); // Scale, will be clamped between 0.01 and 4
        }
        if (id == 16) {
            passthrough(Type.FLOAT); // FromRed, 0-1
            passthrough(Type.FLOAT); // FromGreen, 0-1
            passthrough(Type.FLOAT); // FromBlue, 0-1
            passthrough(Type.FLOAT); // Scale, will be clamped between 0.01 and 4
            passthrough(Type.FLOAT); // ToRed, 0-1
            passthrough(Type.FLOAT); // ToGreen, 0-1
            passthrough(Type.FLOAT); // ToBlue, 0-1
        }
        if (id == 25) { // minecraft:falling_dust
            passthrough(Type.VAR_INT); // BlockState
        }
        if (id == 36) { // minecraft:item
            passthrough(Type.ITEM); // Item
        }
        if (id == 37) { // minecraft:vibration
            passthrough(Type.DOUBLE); // Origin X
            passthrough(Type.DOUBLE); // Origin Y
            passthrough(Type.DOUBLE); // Origin Z
            passthrough(Type.DOUBLE); // Dest X
            passthrough(Type.DOUBLE); // Dest Y
            passthrough(Type.DOUBLE); // Dest Z
            passthrough(Type.INT); // Ticks
        }
        return this;
    }

    @NotNull
    public PacketWrapper passthroughCollection(@NotNull Type type) {
        int length = passthroughVarInt();
        for (int i = 0; i < length; i++) {
            passthrough(type);
        }
        return this;
    }

    @NotNull
    public PacketWrapper passthroughCollection(@NotNull Runnable runnable) {
        int length = passthroughVarInt();
        for (int i = 0; i < length; i++) {
           runnable.run();
        }
        return this;
    }

    @NotNull
    public PacketWrapper passthroughMap(@NotNull Runnable key, @NotNull Runnable value) {
        int size = passthroughVarInt();
        for (int i = 0; i < size; i++) {
            key.run();
            value.run();
        }
        return this;
    }

    @NotNull
    public PacketWrapper passthroughMap(@NotNull Type key, @NotNull Type value) {
        int size = passthroughVarInt();
        for (int i = 0; i < size; i++) {
            passthrough(key);
            passthrough(value);
        }
        return this;
    }

    @NotNull
    public PacketWrapper passthroughAdvancement() {
        if (passthroughBoolean()) { // Has parent
            passthrough(Type.RESOURCE_LOCATION); // Parent id
        }
        if (passthroughBoolean()) { // Has display
            passthroughAdvancementDisplay(); // Display data
        }
        passthroughMap(Type.STRING, Type.VOID); // Criteria
        int length = passthroughVarInt(); // Array length
        for (int i = 0; i < length; i++) {
            int length2 = passthroughVarInt(); // Array length 2
            for (int j = 0; j < length2; j++) {
                passthrough(Type.STRING); // Requirement
            }
        }
        return this;
    }

    @NotNull
    public PacketWrapper passthroughAdvancementDisplay() {
        passthrough(Type.COMPONENT); // Title
        passthrough(Type.COMPONENT); // Description
        passthrough(Type.ITEM); // Icon
        passthrough(Type.VAR_INT); // Frame Type (0 = task, 1 = challenge, 2 = goal)
        int flags = passthroughInt(); // Flags (0x01 = has background texture, 0x02 = show_toast, 0x04 = hidden)
        if ((flags & 1) != 0) passthrough(Type.RESOURCE_LOCATION); // Background texture
        passthrough(Type.FLOAT); // X coord
        passthrough(Type.FLOAT); // Y coord
        return this;
    }

    @NotNull
    public PacketWrapper passthroughAdvancementProgress() {
        passthroughMap(() -> passthrough(Type.STRING), this::passthroughCriterionProgress); // criteria progress
        return this;
    }

    @NotNull
    public PacketWrapper passthroughCriterionProgress() {
        if (passthroughBoolean()) { // achieved
            passthrough(Type.LONG); // date of achieving
        }
        return this;
    }

    @NotNull
    public Date passthroughDate() {
        Date date = read.readDate();
        write.writeDate(date);
        return date;
    }

    @NotNull
    public ResourceLocation passthroughResourceLocation() {
        ResourceLocation location = read.readResourceLocation();
        write.writeResourceLocation(location);
        return location;
    }

    @NotNull
    public BlockHitResult passthroughBlockHitResult() {
        BlockHitResult blockHitResult = read.readBlockHitResult();
        write.writeBlockHitResult(blockHitResult);
        return blockHitResult;
    }

    @NotNull
    public BitSet passthroughBitSet() {
        BitSet bitSet = read.readBitSet();
        write.writeBitSet(bitSet);
        return bitSet;
    }

    @NotNull
    public PacketWrapper passthroughAll() {
        write.writeBytes(read, read.readerIndex(), read.readableBytes());
        return this;
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
            case BIT_SET -> writeBitSetAndReturnSelf(readBitSet());
            case BLOCK_POS -> writeBlockPos(readBlockPos());
            case BLOCK_HIT_RESULT -> writeBlockHitResultAndReturnSelf(readBlockHitResult());
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
            case VOID -> this; // do nothing
        };
    }

    public int peekVarInt() {
        int readerIndex = read.readerIndex();
        int i = read.readVarInt();
        read.readerIndex(readerIndex);
        return i;
    }

    @NotNull
    public IntPair index() {
        return IntPair.of(readerIndex(), writerIndex());
    }

    public void index(@NotNull IntPair pair) {
        readerIndex(pair.first());
        writerIndex(pair.second());
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
        VOID, // void
    }
}
