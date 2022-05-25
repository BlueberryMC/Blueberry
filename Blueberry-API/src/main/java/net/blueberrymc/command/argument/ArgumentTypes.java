package net.blueberrymc.command.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import net.blueberrymc.registry.BlueberryRegistries;
import net.blueberrymc.util.Util;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Map;

public class ArgumentTypes {
    private static final Field BY_CLASS_FIELD = Util.required(() -> {
        Field field = ArgumentTypeInfos.class.getDeclaredField("BY_CLASS");
        field.setAccessible(true);
        return field;
    });

    @SuppressWarnings("unchecked")
    private static void addByClassEntry(@NotNull Class<? extends ArgumentType<?>> type, @NotNull ArgumentTypeInfo<?, ?> typeInfo) {
        Util.required(() -> ((Map<Class<?>, ArgumentTypeInfo<?, ?>>) BY_CLASS_FIELD.get(null)).put(type, typeInfo));
    }

    public static void register(@NotNull String id, @NotNull Class<? extends ArgumentType<?>> type, @NotNull ArgumentTypeInfo<?, ?> typeInfo) {
        addByClassEntry(type, typeInfo);
        BlueberryRegistries.COMMAND_ARGUMENT_TYPE.register(id, typeInfo);
    }
}
