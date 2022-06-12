package net.blueberrymc.impl.world.level.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.blueberrymc.impl.world.level.block.BlueberryBlockData;
import net.blueberrymc.util.Reflected;
import net.blueberrymc.world.level.block.BlockData;
import net.blueberrymc.world.level.block.BlockFace;
import net.blueberrymc.world.level.block.state.BlockState;
import net.blueberrymc.world.level.block.state.properties.BooleanProperty;
import net.blueberrymc.world.level.block.state.properties.DirectionProperty;
import net.blueberrymc.world.level.block.state.properties.EnumProperty;
import net.blueberrymc.world.level.block.state.properties.IntegerProperty;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public record BlueberryBlockState(@NotNull net.minecraft.world.level.block.state.BlockState handle) implements BlockState {
    public BlueberryBlockState {
        Objects.requireNonNull(handle, "handle");
    }

    @Override
    public boolean isAir() {
        return handle.isAir();
    }

    @Override
    public @NotNull BlockData getBlockData() {
        return new BlueberryBlockData(handle.getBlock());
    }

    @Contract("_, _ -> new")
    @Override
    public <V extends Comparable<V>> @NotNull BlockState setValue(@NotNull BlockState.Property<V> property, @NotNull V value) {
        var result = Property.toMinecraftWithMapper(property);
        return new BlueberryBlockState(handle.setValue(result.property, result.mapper.apply(value)));
    }

    @Override
    public <V extends Comparable<V>> @NotNull V getValue(@NotNull BlockState.Property<V> property) {
        var result = Property.toMinecraftWithMapper(property);
        return result.reverseMapper.apply(handle.getValue(result.property));
    }

    @Override
    public @NotNull BlockData getOwner() {
        return getBlockData();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Contract(pure = true)
    @NotNull
    @Reflected
    public static BlockState create(@NotNull BlockData blockData, @NotNull Map<BlockState.Property<?>, Comparable<?>> values, @NotNull Object mapCodec) {
        // TODO: This code does not convert the handmade BlockData to the Minecraft Block.
        var block = ((BlueberryBlockData) blockData).getHandle();
        ImmutableMap mcValues = ImmutableMap.ofEntries(values.entrySet()
                .stream()
                .map(entry -> {
                    var key = entry.getKey();
                    var value = entry.getValue();
                    var result = Property.toMinecraftProperty(key);
                    return new AbstractMap.SimpleImmutableEntry<net.minecraft.world.level.block.state.properties.Property<?>, Comparable<?>>(result, value);
                }).toArray(Map.Entry[]::new));
        var mcMapCodec = (MapCodec<net.minecraft.world.level.block.state.BlockState>) mapCodec;
        return new BlueberryBlockState(new net.minecraft.world.level.block.state.BlockState(block, mcValues, mcMapCodec));
    }

    public static class Property {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public static <T extends Comparable<T>> net.minecraft.world.level.block.state.properties.@NotNull Property<?> toMinecraftProperty(@NotNull BlockState.Property<T> property) {
            if (property instanceof IntegerProperty prop) {
                return net.minecraft.world.level.block.state.properties.IntegerProperty.create(prop.name(), prop.min(), prop.max());
            } else if (property instanceof BooleanProperty prop) {
                return net.minecraft.world.level.block.state.properties.BooleanProperty.create(prop.name());
            } else if (property instanceof DirectionProperty prop) {
                var values = prop.getPossibleValues().stream().map(BlockFace::name).map(Direction::valueOf).toList();
                return net.minecraft.world.level.block.state.properties.DirectionProperty.create(prop.name(), values);
            } else if (property instanceof EnumProperty<?> prop) {
                return net.minecraft.world.level.block.state.properties.EnumProperty.create(prop.name(), (Class) prop.type(), (Collection) prop.getPossibleValues());
            } else {
                throw new IllegalArgumentException("Unsupported property type: " + property.getClass().getName());
            }
        }

        @SuppressWarnings("unchecked")
        @Contract("_ -> new")
        @NotNull
        public static <T extends Comparable<T>, R extends Comparable<R>> PropertyConvertResult<T, R> toMinecraftWithMapper(@NotNull BlockState.Property<T> property) {
            var prop = toMinecraftProperty(property);
            if (prop instanceof net.minecraft.world.level.block.state.properties.DirectionProperty) {
                return (PropertyConvertResult<T, R>) new PropertyConvertResult<BlockFace, Direction>(
                        (net.minecraft.world.level.block.state.properties.DirectionProperty) prop,
                        face -> Direction.valueOf(face.name()),
                        direction -> BlockFace.valueOf(direction.name()));
            } else {
                return PropertyConvertResult.createEmpty(prop);
            }
        }
    }

    public record PropertyConvertResult<T extends Comparable<T>, R extends Comparable<R>>(
            @NotNull net.minecraft.world.level.block.state.properties.Property<R> property,
            @NotNull Function<T, R> mapper,
            @NotNull Function<R, T> reverseMapper) {
        @SuppressWarnings("unchecked")
        @Contract("_ -> new")
        public static <T extends Comparable<T>, R extends Comparable<R>> @NotNull PropertyConvertResult<T, R> createEmpty(
                @NotNull net.minecraft.world.level.block.state.properties.Property<?> property) {
            return new PropertyConvertResult<>((net.minecraft.world.level.block.state.properties.Property<R>) property, t -> (R) t, r -> (T) r);
        }
    }
}
