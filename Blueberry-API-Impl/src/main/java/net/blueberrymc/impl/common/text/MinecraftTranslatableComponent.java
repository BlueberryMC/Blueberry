package net.blueberrymc.impl.common.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MinecraftTranslatableComponent extends MinecraftComponent<MinecraftTranslatableComponent> implements TranslatableComponent {
    private final TranslatableContents contents;

    public MinecraftTranslatableComponent(net.minecraft.network.chat.Component component) {
        super(component);
        this.contents = checkContentsType(TranslatableContents.class);
    }

    @Override
    public @NotNull MinecraftTranslatableComponent toAdventure() {
        return this;
    }

    @NotNull
    @Override
    public MinecraftTranslatableComponent mutable() {
        if (isMutable()) {
            return this;
        }
        return new MinecraftTranslatableComponent(MutableComponent.create(contents));
    }

    @Override
    public @NotNull String key() {
        return contents.getKey();
    }

    @Override
    public @NotNull MinecraftTranslatableComponent key(@NotNull String key) {
        return new MinecraftTranslatableComponent(net.minecraft.network.chat.Component.translatable(key, contents.getArgs()));
    }

    @Override
    public @NotNull List<Component> args() {
        List<Component> args = new ArrayList<>();
        for (Object arg : contents.getArgs()) {
            if (arg instanceof Component component) {
                args.add(component);
            } else if (arg instanceof net.minecraft.network.chat.Component component) {
                args.add(toAdventure(component));
            } else {
                args.add(Component.text(String.valueOf(arg)));
            }
        }
        return args;
    }

    @Override
    public @NotNull MinecraftTranslatableComponent args(@NotNull ComponentLike @NotNull ... args) {
        List<Object> list = new ArrayList<>();
        for (ComponentLike arg : args) {
            if (arg instanceof Component component) {
                list.add(toMinecraft(component));
            } else if (arg instanceof net.minecraft.network.chat.Component component) {
                list.add(component);
            } else {
                list.add(arg.toString());
            }
        }
        return new MinecraftTranslatableComponent(net.minecraft.network.chat.Component.translatable(key(), list.toArray()));
    }

    @Override
    public @NotNull MinecraftTranslatableComponent args(@NotNull List<? extends ComponentLike> args) {
        return args(args.toArray(ComponentLike[]::new));
    }

    @Override
    public @NotNull Builder toBuilder() {
        return Component.translatable()
                .key(key())
                .args(args())
                .style(style())
                .append(children());
    }
}
