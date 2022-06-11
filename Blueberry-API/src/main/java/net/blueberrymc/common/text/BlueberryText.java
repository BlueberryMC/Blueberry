package net.blueberrymc.common.text;

import net.blueberrymc.common.internal.util.ImplGetter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlueberryText extends ComponentLike {
    @Contract("_, _, _ -> new")
    @NotNull
    static BlueberryText text(@NotNull String namespace, @NotNull String path, @Nullable Object @Nullable ... arguments) {
        return (BlueberryText) ImplGetter.byConstructor(String.class, String.class, Object[].class).apply(namespace, path, arguments);
    }

    @NotNull
    String getPath();

    @NotNull
    String getNamespace();

    /**
     * Returns the resulting text of this component.
     * @return the text
     */
    @NotNull
    String getContents();

    @Contract("_ -> new")
    @NotNull
    BlueberryText cloneWithArgs(@Nullable Object @Nullable ... args);

    @Contract(pure = true)
    @NotNull
    default Component withColor(@NotNull TextColor color) {
        return asComponent().color(color);
    }
}
