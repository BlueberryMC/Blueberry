package net.blueberrymc.impl.common.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import org.jetbrains.annotations.NotNull;

public class MinecraftTextComponent extends MinecraftComponent<MinecraftTextComponent> implements TextComponent {
    private final LiteralContents contents;

    public MinecraftTextComponent(@NotNull net.minecraft.network.chat.Component component) {
        super(component);
        this.contents = checkContentsType(LiteralContents.class);
    }

    @Override
    public @NotNull MinecraftTextComponent toAdventure() {
        return this;
    }

    @Override
    public @NotNull MinecraftTextComponent mutable() {
        if (isMutable()) {
            return this;
        }
        return new MinecraftTextComponent(MutableComponent.create(contents));
    }

    @Override
    public @NotNull String content() {
        return contents.text();
    }

    @Override
    public @NotNull TextComponent content(@NotNull String content) {
        return new MinecraftTextComponent(net.minecraft.network.chat.Component.literal(content));
    }

    @Override
    public @NotNull Builder toBuilder() {
        return Component.text()
                .content(content())
                .style(style())
                .append(children());
    }
}
