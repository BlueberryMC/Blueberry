package net.blueberrymc.network;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public interface CustomComponentSerializer<T extends MutableComponent> {
    @NotNull
    Map<Class<?>, CustomComponentSerializer<?>> SERIALIZERS = new ConcurrentHashMap<>();
    Component.Serializer COMPONENT_SERIALIZER = new Component.Serializer();

    @NotNull
    @SuppressWarnings("unchecked")
    private JsonElement serializeUnchecked(@NotNull MutableComponent component, @NotNull JsonSerializationContext context) {
        try {
            return ((CustomComponentSerializer<MutableComponent>) this).serialize(component, context);
        } catch (ClassCastException e) {
            throw new AssertionError("Wrong type of serializer for " + component.getClass().getTypeName(), e);
        }
    }

    @NotNull
    JsonElement serialize(@NotNull T component, @NotNull JsonSerializationContext context);

    @NotNull
    T deserialize(@NotNull JsonElement element, @NotNull JsonDeserializationContext context);

    @NotNull
    default Object deserializeGlobal(@NotNull JsonElement element, @NotNull JsonDeserializationContext context) {
        return COMPONENT_SERIALIZER.deserialize(element, element.getClass(), context);
    }

    static <T extends MutableComponent> void registerSerializer(@NotNull Class<T> componentClass, @NotNull CustomComponentSerializer<T> componentSerializerClass) {
        SERIALIZERS.put(componentClass, componentSerializerClass);
    }

    @NotNull
    static JsonElement callSerialize(@NotNull Component component, @NotNull JsonSerializationContext context) {
        CustomComponentSerializer<?> serializer = SERIALIZERS.get(component.getClass());
        if (serializer == null) {
            throw new IllegalArgumentException("Don't know how to serialize " + component + " as a Component");
        }
        // this cast should be safe because the serializer requires a MutableComponent as type parameter
        return serializer.serializeUnchecked((MutableComponent) component, context);
    }

    @NotNull
    static MutableComponent callDeserialize(@NotNull String customComponentType, @NotNull JsonElement element, @NotNull JsonDeserializationContext context) {
        CustomComponentSerializer<?> serializer = SERIALIZERS.get(getCustomComponentClass(customComponentType));
        if (serializer == null) {
            throw new IllegalArgumentException("Don't know how to turn " + customComponentType + " into a component\n" +
                    "Possible reasons:\n" +
                    "  1. if you're developer, you forgot to register the serializer it with CustomComponentSerializer#registerSerializer\n" +
                    "  2. the mod is outdated and needs to be updated\n" +
                    "  3. the required mod is not installed");
        }
        return serializer.deserialize(element, context);
    }

    @NotNull
    private static Class<?> getCustomComponentClass(@NotNull String customComponentType) {
        try {
            return Class.forName(customComponentType);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Custom component class not found: " + customComponentType + "\n" +
                    "Possible reasons:\n" +
                    "  1. the mod is outdated and needs to be updated\n" +
                    "  2. the required mod is not installed");
        }
    }
}
