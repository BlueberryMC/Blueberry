package net.blueberrymc.impl.common.text;

import net.blueberrymc.impl.util.BinaryTagHolderUtil;
import net.blueberrymc.impl.util.KeyUtil;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class MinecraftComponent<C extends MinecraftComponent<C>> {
    protected final net.minecraft.network.chat.Component component;

    @Contract(pure = true)
    public MinecraftComponent(@NotNull net.minecraft.network.chat.Component component) {
        Objects.requireNonNull(component);
        this.component = component;
    }

    public abstract @NotNull Component toAdventure();

    public abstract @NotNull C mutable();

    @SuppressWarnings("unchecked")
    public @NotNull C style(@NotNull Style style) {
        if (component instanceof MutableComponent mutableComponent) {
            mutableComponent.setStyle(toMinecraft(style));
            return (C) this;
        } else {
            return mutable().style(style);
        }
    }

    @SuppressWarnings("unchecked")
    public @NotNull C children(@NotNull List<? extends ComponentLike> children) {
        if (component instanceof MutableComponent mutableComponent) {
            mutableComponent.getSiblings().clear();
            mutableComponent.getSiblings().addAll(children.stream().map(MinecraftComponent::toMinecraft).toList());
            return (C) this;
        } else {
            return mutable().children(children);
        }
    }

    @NotNull
    public net.minecraft.network.chat.Component toMinecraft() {
        return component;
    }

    @NotNull
    protected final <T extends ComponentContents> T checkContentsType(@NotNull Class<T> typeOfT) {
        if (typeOfT.isInstance(component.getContents())) {
            return typeOfT.cast(component.getContents());
        }
        throw new IllegalArgumentException("Contents of component is not of type " + typeOfT.getTypeName());
    }

    public boolean isMutable() {
        return component instanceof MutableComponent;
    }

    public @Unmodifiable @NotNull List<Component> children() {
        return component.getSiblings().stream().map(MinecraftComponent::toAdventure).toList();
    }

    public @NotNull Style style() {
        return toAdventure(component.getStyle());
    }

    @Contract("_ -> new")
    @NotNull
    public static MinecraftTextComponent text(@NotNull String content) {
        return new MinecraftTextComponent(net.minecraft.network.chat.Component.literal(content));
    }

    @Contract("_, _ -> new")
    @NotNull
    public static MinecraftTranslatableComponent translatable(@NotNull String key, @Nullable Object @NotNull ... args) {
        return new MinecraftTranslatableComponent(net.minecraft.network.chat.Component.translatable(key, args));
    }

    @NotNull
    public static Component toAdventure(@NotNull net.minecraft.network.chat.Component component) {
        return create(component).toAdventure();
    }

    @NotNull
    public static net.minecraft.network.chat.Component toMinecraft(@NotNull ComponentLike component) {
        return create(component.asComponent()).toMinecraft();
    }

    @NotNull
    public static net.minecraft.network.chat.Component toMinecraft(@NotNull Component component) {
        return create(component).toMinecraft();
    }

    @Contract("_ -> new")
    @NotNull
    public static MinecraftComponent<?> create(@NotNull net.minecraft.network.chat.Component component) {
        if (component.getContents() instanceof LiteralContents) {
            return new MinecraftTextComponent(component);
        } else if (component.getContents() instanceof TranslatableContents) {
            return new MinecraftTranslatableComponent(component);
        } else {
            throw new IllegalArgumentException("Unsupported component contents type: " + component.getContents().getClass().getTypeName());
        }
    }

    @NotNull
    public static MinecraftComponent<?> create(@NotNull Component component) {
        if (component instanceof TextComponent c) {
            return text(c.content()).style(c.style()).children(c.children());
        } else if (component instanceof TranslatableComponent c) {
            return translatable(c.key(), c.args().stream().map(MinecraftComponent::toMinecraft).toArray()).style(c.style()).children(c.children());
        } else {
            throw new IllegalArgumentException("Unsupported component type: " + component.getClass().getTypeName());
        }
    }

    @NotNull
    public static Style toAdventure(@NotNull net.minecraft.network.chat.Style style) {
        if (style.isEmpty()) {
            return Style.empty();
        }
        Style.Builder builder = Style.style();
        List<TextDecoration> decorations = new ArrayList<>();
        if (style.isBold()) {
            decorations.add(TextDecoration.BOLD);
        }
        if (style.isItalic()) {
            decorations.add(TextDecoration.ITALIC);
        }
        if (style.isObfuscated()) {
            decorations.add(TextDecoration.OBFUSCATED);
        }
        if (style.isStrikethrough()) {
            decorations.add(TextDecoration.STRIKETHROUGH);
        }
        if (style.isUnderlined()) {
            decorations.add(TextDecoration.UNDERLINED);
        }
        if (!decorations.isEmpty()) {
            builder.decorate(decorations.toArray(TextDecoration[]::new));
        }
        if (style.getColor() != null) {
            builder.color(TextColor.color(style.getColor().getValue()));
        }
        builder.font(KeyUtil.toAdventure(style.getFont()));
        if (style.getClickEvent() != null) {
            builder.clickEvent(toAdventure(style.getClickEvent()));
        }
        if (style.getHoverEvent() != null) {
            builder.hoverEvent(toAdventure(style.getHoverEvent()));
        }
        if (style.getInsertion() != null) {
            builder.insertion(style.getInsertion());
        }
        return builder.build();
    }

    @NotNull
    public static ClickEvent toAdventure(@NotNull net.minecraft.network.chat.ClickEvent clickEvent) {
        return switch (clickEvent.getAction()) {
            case SUGGEST_COMMAND -> ClickEvent.suggestCommand(clickEvent.getValue());
            case OPEN_URL -> ClickEvent.openUrl(clickEvent.getValue());
            case OPEN_FILE -> ClickEvent.openFile(clickEvent.getValue());
            case CHANGE_PAGE -> ClickEvent.changePage(clickEvent.getValue());
            case RUN_COMMAND -> ClickEvent.runCommand(clickEvent.getValue());
            case COPY_TO_CLIPBOARD -> ClickEvent.copyToClipboard(clickEvent.getValue());
        };
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <V> HoverEvent<V> toAdventure(@NotNull net.minecraft.network.chat.HoverEvent hoverEvent) {
        if (hoverEvent.getAction() == net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT) {
            net.minecraft.network.chat.Component value = hoverEvent.getValue(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT);
            if (value != null) {
                return (HoverEvent<V>) HoverEvent.showText(MinecraftComponent.toAdventure(value));
            } else {
                return (HoverEvent<V>) HoverEvent.showText(Component.empty());
            }
        } else if (hoverEvent.getAction() == net.minecraft.network.chat.HoverEvent.Action.SHOW_ENTITY) {
            net.minecraft.network.chat.HoverEvent.EntityTooltipInfo value = hoverEvent.getValue(net.minecraft.network.chat.HoverEvent.Action.SHOW_ENTITY);
            if (value != null) {
                ResourceLocation location = Registry.ENTITY_TYPE.getKey(value.type);
                if (value.name != null) {
                    return (HoverEvent<V>) HoverEvent.showEntity(KeyUtil.toAdventure(location), value.id, toAdventure(value.name));
                } else {
                    return (HoverEvent<V>) HoverEvent.showEntity(KeyUtil.toAdventure(location), value.id);
                }
            } else {
                return null;
            }
        } else if (hoverEvent.getAction() == net.minecraft.network.chat.HoverEvent.Action.SHOW_ITEM) {
            net.minecraft.network.chat.HoverEvent.ItemStackInfo value = hoverEvent.getValue(net.minecraft.network.chat.HoverEvent.Action.SHOW_ITEM);
            if (value != null) {
                ItemStack stack = value.getItemStack();
                if (stack.getTag() != null) {
                    return (HoverEvent<V>) HoverEvent.showItem(KeyUtil.toAdventure(Registry.ITEM.getKey(stack.getItem())), stack.getCount(), BinaryTagHolderUtil.toAdventure(stack.getTag()));
                } else {
                    return (HoverEvent<V>) HoverEvent.showItem(KeyUtil.toAdventure(Registry.ITEM.getKey(stack.getItem())), stack.getCount());
                }
            } else {
                return null;
            }
        } else {
            return null; // unknown action
        }
    }

    @Contract(pure = true)
    @NotNull
    public static net.minecraft.network.chat.Style toMinecraft(@NotNull Style style) {
        net.minecraft.network.chat.Style mc = net.minecraft.network.chat.Style.EMPTY;
        if (style.hasDecoration(TextDecoration.BOLD)) {
            mc = mc.withBold(true);
        }
        if (style.hasDecoration(TextDecoration.ITALIC)) {
            mc = mc.withItalic(true);
        }
        if (style.hasDecoration(TextDecoration.OBFUSCATED)) {
            mc = mc.withObfuscated(true);
        }
        if (style.hasDecoration(TextDecoration.STRIKETHROUGH)) {
            mc = mc.withStrikethrough(true);
        }
        if (style.hasDecoration(TextDecoration.UNDERLINED)) {
            mc = mc.withUnderlined(true);
        }
        TextColor color = style.color();
        if (color != null) {
            mc = mc.withColor(net.minecraft.network.chat.TextColor.fromRgb(color.value()));
        }
        Key font = style.font();
        if (font != null) {
            mc = mc.withFont(KeyUtil.toMinecraft(font));
        }
        ClickEvent clickEvent = style.clickEvent();
        if (clickEvent != null) {
            mc = mc.withClickEvent(toMinecraft(clickEvent));
        }
        HoverEvent<?> hoverEvent = style.hoverEvent();
        if (hoverEvent != null) {
            mc = mc.withHoverEvent(toMinecraft(hoverEvent));
        }
        String insertion = style.insertion();
        if (insertion != null) {
            mc = mc.withInsertion(insertion);
        }
        return mc;
    }

    @NotNull
    public static net.minecraft.network.chat.ClickEvent toMinecraft(@NotNull ClickEvent clickEvent) {
        return switch (clickEvent.action()) {
            case CHANGE_PAGE -> new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.CHANGE_PAGE, clickEvent.value());
            case OPEN_FILE -> new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.OPEN_FILE, clickEvent.value());
            case OPEN_URL -> new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.OPEN_URL, clickEvent.value());
            case COPY_TO_CLIPBOARD -> new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.COPY_TO_CLIPBOARD, clickEvent.value());
            case SUGGEST_COMMAND -> new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.SUGGEST_COMMAND, clickEvent.value());
            case RUN_COMMAND -> new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND, clickEvent.value());
        };
    }

    @Nullable
    public static net.minecraft.network.chat.HoverEvent toMinecraft(@NotNull HoverEvent<?> hoverEvent) {
        if (hoverEvent.action() == HoverEvent.Action.SHOW_TEXT) {
            Component value = (Component) hoverEvent.value();
            return new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, toMinecraft(value));
        } else if (hoverEvent.action() == HoverEvent.Action.SHOW_ENTITY) {
            HoverEvent.ShowEntity value = (HoverEvent.ShowEntity) hoverEvent.value();
            net.minecraft.network.chat.Component name = null;
            if (value.name() != null) {
                name = toMinecraft(value.name());
            }
            net.minecraft.network.chat.HoverEvent.EntityTooltipInfo info =
                    new net.minecraft.network.chat.HoverEvent.EntityTooltipInfo(Registry.ENTITY_TYPE.get(KeyUtil.toMinecraft(value.type())), value.id(), name);
            return new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_ENTITY, info);
        } else if (hoverEvent.action() == HoverEvent.Action.SHOW_ITEM) {
            HoverEvent.ShowItem value = (HoverEvent.ShowItem) hoverEvent.value();
            ItemStack stack = new ItemStack(Registry.ITEM.get(KeyUtil.toMinecraft(value.item())), value.count());
            if (value.nbt() != null) {
                stack.setTag(BinaryTagHolderUtil.toMinecraft(value.nbt()));
            }
            net.minecraft.network.chat.HoverEvent.ItemStackInfo info =
                    new net.minecraft.network.chat.HoverEvent.ItemStackInfo(stack);
            return new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_ITEM, info);
        } else {
            return null;
        }
    }
}
