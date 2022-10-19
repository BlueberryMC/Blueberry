package net.blueberrymc.client.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.blueberrymc.client.gui.components.ScrollableContainer;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.InternalBlueberryModConfig;
import net.blueberrymc.common.bml.config.BooleanVisualConfig;
import net.blueberrymc.common.bml.config.ByteVisualConfig;
import net.blueberrymc.common.bml.config.ClassVisualConfig;
import net.blueberrymc.common.bml.config.CompoundVisualConfig;
import net.blueberrymc.common.bml.config.CycleVisualConfig;
import net.blueberrymc.common.bml.config.DoubleVisualConfig;
import net.blueberrymc.common.bml.config.FloatVisualConfig;
import net.blueberrymc.common.bml.config.IntegerVisualConfig;
import net.blueberrymc.common.bml.config.LongVisualConfig;
import net.blueberrymc.common.bml.config.NumberVisualConfig;
import net.blueberrymc.common.bml.config.RootCompoundVisualConfig;
import net.blueberrymc.common.bml.config.ShortVisualConfig;
import net.blueberrymc.common.bml.config.StringVisualConfig;
import net.blueberrymc.common.bml.config.VisualConfig;
import net.blueberrymc.common.scheduler.BlueberryTask;
import net.blueberrymc.common.text.BlueberryText;
import net.blueberrymc.common.util.ReflectionHelper;
import net.blueberrymc.util.ComponentGetter;
import net.blueberrymc.util.NameGetter;
import net.blueberrymc.util.NumberUtil;
import net.blueberrymc.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Mod config screen, accessible via mod list screen.
 */
public class ModConfigScreen extends BlueberryScreen {
    private static final Component UNKNOWN_TEXT = Component.text("<unknown>", NamedTextColor.GRAY);
    private static final Component BOOLEAN_TRUE = Component.text("true", NamedTextColor.GREEN);
    private static final Component BOOLEAN_FALSE = Component.text("false", NamedTextColor.RED);
    private final List<Consumer<PoseStack>> callbacks = new ArrayList<>();
    private final CompoundVisualConfig compoundVisualConfig;
    private final Screen previousScreen;
    private final Component description;
    private final List<Object> blockers = new ArrayList<>();
    private Button backButton;

    public ModConfigScreen(@NotNull CompoundVisualConfig compoundVisualConfig, @NotNull Screen screen) {
        super(title(compoundVisualConfig));
        this.compoundVisualConfig = compoundVisualConfig;
        RootCompoundVisualConfig root = this.compoundVisualConfig.getRoot();
        this.description = root == null ? null : root.title;
        this.previousScreen = screen;
    }

    @Override
    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(this.previousScreen);
    }

    // TODO: clean-up
    protected void init() {
        this.children().clear();
        callbacks.clear();
        ScrollableContainer<AbstractWidget> container = new ScrollableContainer<>(Objects.requireNonNull(this.minecraft), this.width, this.height, 58, this.height - 50, 20, 2);
        this.addRenderableWidget(this.backButton = Button.builder(CommonComponents.GUI_BACK, (button) -> {
            this.minecraft.setScreen(this.previousScreen);
            if (compoundVisualConfig instanceof RootCompoundVisualConfig root) {
                root.onChanged();
            }
        }).bounds(this.width / 2 - 77, this.height - 38, 154, 20).build());
        int offset = 38;
        int _maxWidth = 50;
        for (VisualConfig<?> config : this.compoundVisualConfig) {
            if (!(config instanceof CompoundVisualConfig)) {
                Component component = config.getComponent();
                if (component != null) {
                    int textWidth = font.width(component);
                    if (textWidth > _maxWidth) _maxWidth = textWidth;
                }
            }
        }
        final int maxWidth = _maxWidth;
        BiConsumer<VisualConfig<?>, Integer> addLabel = (config, finalOffset) -> callbacks.add(poseStack -> {
            int y = finalOffset + 6 - (int) container.getScrollAmount();
            if (y > 60 && y < this.height - 54) {
                drawString(poseStack, font, Util.getOrDefault(config.getComponent(), UNKNOWN_TEXT), this.width / 2 - maxWidth - 6, y, 0xFFFFFF);
            }
        });
        for (VisualConfig<?> config : this.compoundVisualConfig) {
            MutableComponent tooltip = Component.literal("");
            //Function<PoseStack, BiConsumer<Integer, Integer>> onTooltipFunction;
            //Button.OnTooltip onTooltip;
            //Component tooltip = Component.empty();
            // deprecated
            var deprecatedData = config.getDeprecatedData();
            if (deprecatedData.deprecated()) {
                Component text = BlueberryText.text("blueberry", "gui.screens.mod_config.deprecated").withColor(NamedTextColor.RED);
                tooltip = tooltip.append(text).append(Component.newline());
                if (deprecatedData.reason() != null) {
                    tooltip = tooltip.append(Component.text("\"" + deprecatedData.reason() + "\"", NamedTextColor.RED)).append(Component.newline());
                }
            }
            // experimental
            var experimentalData = config.getExperimentalData();
            if (experimentalData.experimental()) {
                MutableComponent text = BlueberryText.text("blueberry", "gui.screens.mod_config.experimental");
                tooltip.append(text.withStyle(ChatFormatting.GOLD)).append("\n");
            }
            // description
            Component desc = config.getDescription();
            if (desc != null) tooltip = tooltip.append(desc.color(NamedTextColor.YELLOW).append(Component.newline()));
            // default value
            Object def = config instanceof CompoundVisualConfig ? null : config.getDefaultValue();
            if (def != null) {
                String s = def.toString();
                if (def instanceof NameGetter ng) {
                    s = ng.getName();
                }
                if (def instanceof ComponentGetter cg) {
                    s = PlainTextComponentSerializer.plainText().serialize(cg.getComponent());
                }
                // if (def instanceof Enum<?>) s = ((Enum<?>) def).name();
                if (def instanceof Class<?> cl) s = cl.getTypeName();
                // TODO: legacy color char
                tooltip = tooltip.append(BlueberryText.text("blueberry", "gui.screens.mod_config.default", s + "\u00a7").withColor(NamedTextColor.AQUA)).append(Component.newline());
            }
            // min/max value of number
            if (config instanceof NumberVisualConfig<?> numberVisualConfig) {
                boolean shouldShowMinMax = !(config instanceof IntegerVisualConfig cfg) || cfg.getMin() != Integer.MIN_VALUE || cfg.getMax() != Integer.MAX_VALUE;
                if (config instanceof LongVisualConfig cfg && cfg.getMin() == Long.MIN_VALUE && cfg.getMax() == Long.MAX_VALUE) {
                    shouldShowMinMax = false;
                }
                if (config instanceof FloatVisualConfig cfg && cfg.getMin() == Float.MIN_VALUE && cfg.getMax() == Float.MAX_VALUE) {
                    shouldShowMinMax = false;
                }
                if (config instanceof DoubleVisualConfig cfg && cfg.getMin() == Double.MIN_VALUE && cfg.getMax() == Double.MAX_VALUE) {
                    shouldShowMinMax = false;
                }
                if (shouldShowMinMax) {
                    tooltip.append(BlueberryText.text("blueberry", "gui.screens.mod_config.number_min_max", numberVisualConfig.getMinAsNumber(), numberVisualConfig.getMaxAsNumber(), NamedTextColor.AQUA)).append(Component.newline());
                }
                tooltip.append(BlueberryText.text("blueberry", "gui.screens.mod_config.precise_control", NamedTextColor.GRAY)).append(Component.newline());
            }
            // pattern
            if (config instanceof StringVisualConfig stringVisualConfig) {
                Pattern pattern = stringVisualConfig.getPattern();
                if (pattern != null) tooltip.append(BlueberryText.text("blueberry", "gui.screens.mod_config.pattern", ChatFormatting.GOLD + pattern.pattern() + ChatFormatting.AQUA, NamedTextColor.AQUA)).append(Component.newline());
            }
            if (config instanceof ClassVisualConfig classVisualConfig) {
                Pattern pattern = classVisualConfig.getPattern();
                if (pattern != null) tooltip.append(BlueberryText.text("blueberry", "gui.screens.mod_config.pattern", ChatFormatting.GOLD + pattern.pattern() + ChatFormatting.AQUA, NamedTextColor.AQUA)).append(Component.newline());
            }
            // requiresRestart
            if (config.isRequiresRestart()) tooltip.append(BlueberryText.text("blueberry", "gui.screens.mod_config.requires_restart", NamedTextColor.RED)).append(Component.newline());
            if (tooltip.getSiblings().size() > 0) tooltip.getSiblings().remove(tooltip.getSiblings().size() - 1); // removes last \n
            Tooltip onTooltip;
            if (tooltip.toString().length() > 0) {
                onTooltip = Tooltip.create(addDebugInfo(config, tooltip));
            } else {
                onTooltip = null;
            }
            if (config instanceof CompoundVisualConfig compoundVisualConfigIn) {
                Component component = Util.getOrDefault(
                        compoundVisualConfigIn.getComponent(),
                        compoundVisualConfigIn.getTitle(),
                        UNKNOWN_TEXT
                );
                container.children().add(Button.builder(component, button -> this.minecraft.setScreen(new ModConfigScreen(compoundVisualConfigIn, this))).tooltip(onTooltip).bounds(this.width / 2 - this.width / 8, (offset += 22), this.width / 4, 20).build());
            } else if (config instanceof BooleanVisualConfig booleanVisualConfig) {
                container.children().add(Button.builder(getButtonMessage(booleanVisualConfig), (button) -> {
                    Boolean curr = booleanVisualConfig.get();
                    booleanVisualConfig.set(!(curr != null && curr));
                    updateBooleanButton(booleanVisualConfig, button);
                    booleanVisualConfig.clicked(button);
                }).bounds(this.width / 2 + 6, (offset += 22), Math.min(maxWidth, this.width / 6), 20).tooltip(onTooltip).build());
                addLabel.accept(config, offset);
            } else if (config instanceof CycleVisualConfig<?> cycleVisualConfig) {
                final int buttonY = offset += 22;
                Button btn;
                container.children().add(
                        btn = Button.builder(Component.literal(cycleVisualConfig.getCurrentName()), (button) -> button.setMessage(Component.literal(cycleVisualConfig.isReverse() ? cycleVisualConfig.getPreviousName() : cycleVisualConfig.getNextName())))
                                .bounds((this.width / 2) + 6 + 23, buttonY, Math.min(maxWidth, this.width / 6 - (24 * 2)), 20)
                                .tooltip(onTooltip)
                                .build()
                );
                container.children().add(
                        Button.builder(Component.text("<-"), (button) -> btn.setMessage(Component.text(cycleVisualConfig.isReverse() ? cycleVisualConfig.getNextName() : cycleVisualConfig.getPreviousName())))
                                .bounds((this.width / 2) + 6, buttonY, 22, 20)
                                .tooltip(onTooltip)
                                .build()
                );
                container.children().add(
                        Button.builder(Component.literal("->"), (button) -> btn.setMessage(Component.literal(cycleVisualConfig.isReverse() ? cycleVisualConfig.getPreviousName() : cycleVisualConfig.getNextName())))
                                .bounds((this.width / 2) + 6 + Math.min(maxWidth + 24, this.width / 6 - 24), buttonY, 22, 20)
                                .tooltip(onTooltip)
                                .build()
                );
                addLabel.accept(config, offset);
            } else if (config instanceof NumberVisualConfig<?> numberVisualConfig) {
                int componentWidth = Math.min(maxWidth, this.width / 6);
                EditBox editBox = new EditBox(font, this.width / 2 + 6, (offset += 22), componentWidth, 20, Component.text(""));
                editBox.setTooltip(onTooltip);
                Number defValue = numberVisualConfig.get();
                if (defValue == null) defValue = numberVisualConfig.getDefaultValue();
                if (defValue == null) defValue = numberVisualConfig.getMinAsNumber();
                editBox.setResponder((s) -> {
                    try {
                        Number number = parseNumber(numberVisualConfig, s);
                        if (NumberUtil.isNumberLessThan(number, numberVisualConfig.getMinAsNumber()) || NumberUtil.isNumberGreaterThan(number, numberVisualConfig.getMaxAsNumber())) throw new NumberFormatException();
                        editBox.setTextColor(0xe0e0e0);
                        numberVisualConfig.set(number);
                        unblock(editBox);
                    } catch (NumberFormatException e) {
                        editBox.setTextColor(0xff0000);
                        block(editBox);
                    }
                });
                editBox.setValue(defValue.toString());
                container.children().add(editBox);
                Supplier<Component> sliderLabel = () -> Component.text(Objects.requireNonNullElse(numberVisualConfig.get(), numberVisualConfig.getMinAsNumber()).toString());
                AbstractSliderButton slider = new AbstractSliderButton(this.width / 2 + 6, offset, componentWidth, 20, Component.empty(), numberVisualConfig.getPercentage()) {
                    @Override
                    protected void updateMessage() {
                        setMessage(sliderLabel.get());
                    }

                    @Override
                    protected void applyValue() {
                        double range = 1.0;
                        if (hasShiftDown()) {
                            range *= 0.25;
                        }
                        if (hasControlDown()) {
                            range *= 0.2; // shift + control = 6.25% = 0.0625
                        }
                        if (hasAltDown()) {
                            // shift + control + alt = 0.5% = 0.005
                            // shift + alt = 2.5% = 0.025
                            // control + alt = 2% = 0.02
                            range *= 0.1;
                        }
                        boolean hasSpaceDown = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_SPACE);
                        if (hasSpaceDown) {
                            // shift + control + alt + space = 0.0125% = 0.000125
                            range *= 0.025;
                        }
                        numberVisualConfig.setPercentage(0.5 - range / 2 + range * value);
                    }
                };
                slider.setTooltip(onTooltip);
                Consumer<Double> valueUpdater = (value) -> {
                    try {
                        ReflectionHelper.setField(AbstractSliderButton.class, slider, "value", value);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                };
                slider.setMessage(sliderLabel.get());
                slider.visible = false;
                container.children().add(slider);
                Button toggleButton = Button.builder(Component.text("☯"), (button) -> {
                    if (editBox.visible) {
                        editBox.visible = false;
                        slider.visible = true;
                        button.setTooltip(Tooltip.create(BlueberryText.text("blueberry", "gui.screens.mod_config.show_editbox")));
                        slider.setMessage(sliderLabel.get());
                        valueUpdater.accept(numberVisualConfig.getPercentage());
                        unblock(editBox);
                    } else {
                        slider.visible = false;
                        editBox.visible = true;
                        button.setTooltip(Tooltip.create(BlueberryText.text("blueberry", "gui.screens.mod_config.show_slider")));
                        editBox.setValue(Objects.requireNonNullElse(numberVisualConfig.get(), numberVisualConfig.getMinAsNumber()).toString());
                    }
                }).bounds(this.width / 2 + 10 + componentWidth, offset, 20, 20).build();
                if (editBox.visible) {
                    toggleButton.setTooltip(Tooltip.create(BlueberryText.text("blueberry", "gui.screens.mod_config.show_slider")));
                } else {
                    toggleButton.setTooltip(Tooltip.create(BlueberryText.text("blueberry", "gui.screens.mod_config.show_editbox")));
                }
                toggleButton.active = true;
                container.children().add(toggleButton);
                addLabel.accept(config, offset);
            } else if (config instanceof StringVisualConfig stringVisualConfig) {
                EditBox editBox = new EditBox(font, this.width / 2 + 6, (offset += 22), Math.min(maxWidth, this.width / 6), 20, Component.text(""));
                editBox.setTooltip(onTooltip);
                String defValue = stringVisualConfig.get();
                if (defValue == null) defValue = stringVisualConfig.getDefaultValue();
                if (defValue == null) defValue = "";
                editBox.setResponder((s) -> {
                    if (stringVisualConfig.isValid(s)) {
                        editBox.setTextColor(0xe0e0e0);
                        stringVisualConfig.set(s);
                        unblock(editBox);
                    } else {
                        editBox.setTextColor(0xff0000);
                        block(editBox);
                    }
                });
                editBox.setValue(defValue);
                container.children().add(editBox);
                addLabel.accept(config, offset);
            } else if (config instanceof ClassVisualConfig classVisualConfig) {
                EditBox editBox = new EditBox(font, this.width / 2 + 6, (offset += 22), Math.min(maxWidth, this.width / 6), 20, Component.text(""));
                editBox.setTooltip(onTooltip);
                Class<?> defValue = classVisualConfig.get();
                if (defValue == null) defValue = classVisualConfig.getDefaultValue();
                editBox.setResponder((s) -> {
                    if (classVisualConfig.isValid(s)) {
                        editBox.setTextColor(0xe0e0e0);
                        classVisualConfig.set(s);
                        unblock(editBox);
                    } else {
                        editBox.setTextColor(0xff0000);
                        block(editBox);
                    }
                });
                editBox.setValue(defValue != null ? defValue.getTypeName() : "");
                container.children().add(editBox);
                addLabel.accept(config, offset);
            }
        }
        this.children().add(container);
        super.init();
    }

    private static Component addDebugInfo(VisualConfig<?> config, Component tooltip) {
        if (!InternalBlueberryModConfig.Debug.debugModConfigScreen) return tooltip;
        Component copy;
        if (tooltip.children().size() == 0) {
            copy = tooltip;
        } else {
            copy = tooltip.append(Component.newline());
        }
        copy = copy.append(Component.text("[Config Type: " + Util.getExtendedSimpleName(config.getClass()) + "]", NamedTextColor.GRAY)).append(Component.newline());
        String valueType;
        Object value = config.get();
        if (value == null) {
            valueType = "null";
        } else {
            valueType = value.getClass().getTypeName();
        }
        copy = copy.append(Component.text("[Type: " + valueType + "]", NamedTextColor.GRAY)).append(Component.newline());
        if (config instanceof CompoundVisualConfig compoundVisualConfig) {
            copy = copy.append(Component.text("[Compound size: " + compoundVisualConfig.size() + "]", NamedTextColor.GRAY)).append(Component.newline());
        } else {
            copy = copy.append(Component.text("[Raw value: ", NamedTextColor.GRAY))
                    .append(Component.text(String.valueOf(config.get())))
                    .append(Component.text("]", NamedTextColor.GRAY))
                    .append(Component.newline());
        }
        if (config instanceof CycleVisualConfig<?> cycleVisualConfig && cycleVisualConfig.get() instanceof Enum<?> e) {
            copy = copy.append(Component.text("[List size: " + cycleVisualConfig.size() + "]", NamedTextColor.GRAY)).append(Component.newline())
                    .append(Component.text("[Enum constant: ", NamedTextColor.GRAY))
                    .append(Component.text(e.name(), NamedTextColor.YELLOW))
                    .append(Component.text("]", NamedTextColor.GRAY))
                    .append(Component.newline())
                    .append(Component.text("[Enum ordinal: ", NamedTextColor.GRAY))
                    .append(Component.text(Integer.toString(e.ordinal()), NamedTextColor.YELLOW))
                    .append(Component.text("]", NamedTextColor.GRAY))
                    .append(Component.newline())
                    .append(Component.text("[Previous value: ", NamedTextColor.GRAY))
                    .append(Component.text(String.valueOf(cycleVisualConfig.isReverse() ? cycleVisualConfig.peekNext() : cycleVisualConfig.peekPrevious())))
                    .append(Component.text("]", NamedTextColor.GRAY))
                    .append(Component.newline())
                    .append(Component.text("[Next value: ", NamedTextColor.GRAY))
                    .append(Component.text(String.valueOf(cycleVisualConfig.isReverse() ? cycleVisualConfig.peekPrevious() : cycleVisualConfig.peekNext())))
                    .append(Component.text("]", NamedTextColor.GRAY))
                    .append(Component.newline());
        }
        List<Component> children = new ArrayList<>(copy.children());
        if (children.get(0).equals(Component.newline())) children.remove(0);
        children.remove(children.size() - 1);
        return copy.children(children);
    }

    @Contract("null, _ -> !null")
    private static Number parseNumber(NumberVisualConfig<?> config, String s) throws NumberFormatException {
        if (config instanceof ByteVisualConfig) {
            return Byte.parseByte(s);
        } else if (config instanceof DoubleVisualConfig) {
            return Double.parseDouble(s);
        } else if (config instanceof FloatVisualConfig) {
            return Float.parseFloat(s);
        } else if (config instanceof IntegerVisualConfig) {
            return Integer.parseInt(s);
        } else if (config instanceof LongVisualConfig) {
            return Long.parseLong(s);
        } else if (config instanceof ShortVisualConfig) {
            return Short.parseShort(s);
        } else {
            return 0;
        }
    }

    private void block(Object blocker) {
        if (!blockers.contains(blocker)) blockers.add(blocker);
        backButton.active = false;
    }

    private void unblock(Object blocker) {
        blockers.remove(blocker);
        backButton.active = blockers.isEmpty();
    }

    private void updateBooleanButton(@NotNull BooleanVisualConfig booleanVisualConfig, @NotNull Button button) {
        button.setMessage(getButtonMessage(booleanVisualConfig));
    }

    @NotNull
    private static Component getButtonMessage(@NotNull BooleanVisualConfig booleanVisualConfig) {
        Boolean bool = booleanVisualConfig.get();
        return bool == null || !bool ? BOOLEAN_FALSE : BOOLEAN_TRUE;
    }

    public void render(@NotNull PoseStack poseStack, int i, int i2, float f) {
        this.renderBackground(poseStack);
        this.children().forEach(e -> {
            if (e instanceof ScrollableContainer) {
                ((ScrollableContainer<?>) e).render(poseStack, i, i2, f);
            }
        });
        for (Consumer<PoseStack> callback : callbacks) callback.accept(poseStack);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 16, 16777215);
        if (this.description != null) {
            drawCenteredString(poseStack, this.font, this.description, this.width / 2, 30, 16777215);
        }
        super.render(poseStack, i, i2, f);
    }

    @NotNull
    private static Component title(@NotNull CompoundVisualConfig compoundVisualConfig) {
        Component text = BlueberryText.text("blueberry", "gui.screens.mod_config.title").asComponent();
        CompoundVisualConfig parent = compoundVisualConfig;
        List<Component> components = new ArrayList<>();
        while ((parent = parent.getParent()) != null) {
            if (parent.getComponent() != null) {
                components.add(parent.getComponent());
            } else if (parent.getTitle() != null) {
                components.add(parent.getTitle());
            }
        }
        Collections.reverse(components);
        components.forEach(component -> text.append(Component.text(" > ")).append(component));
        text.append(Component.text(" > "));
        if (compoundVisualConfig.getComponent() != null) {
            text.append(compoundVisualConfig.getComponent());
        } else if (compoundVisualConfig.getTitle() != null) {
            text.append(compoundVisualConfig.getTitle());
        } else {
            text.append(Component.text("Unknown"));
        }
        return text;
    }

    @SuppressWarnings("unused")
    private void enableRainbowText(@NotNull EditBox editBox) {
        AtomicInteger ticks = new AtomicInteger();
        AtomicReference<BlueberryTask> task = new AtomicReference<>();
        task.set(Blueberry.getUtil().getClientScheduler().runTaskTimer(Objects.requireNonNull(Blueberry.getModLoader().getModById("blueberry")), 1, 1, () -> {
            if (Minecraft.getInstance().screen != this) {
                task.get().cancel();
                task.set(null);
                return;
            }
            int i = 50;
            if (ticks.incrementAndGet() > i) {
                ticks.set(-i);
            }
            final int MAX_COLOR = 360;
            final int MIN_COLOR = 160;
            double jump = (MAX_COLOR-MIN_COLOR) / (i*1.0);
            int color = hsvToRgb((float) (MIN_COLOR + (jump*Math.abs(ticks.get()))));
            editBox.setTextColor(color);
        }));
    }

    private static int hsvToRgb(float hue) {
        int h = (int) (hue / 60);
        float f = hue / 60 - h;
        float q = (1 - f);
        float t = (1 - q);

        return switch (h) {
            case 0 -> rgbToString(1, t, 0);
            case 1 -> rgbToString(q, 1, 0);
            case 2 -> rgbToString(0, 1, t);
            case 3 -> rgbToString(0, q, 1);
            case 4 -> rgbToString(t, 0, 1);
            case 5, 6 -> rgbToString(1, 0, q);
            default -> throw new IllegalArgumentException();
        };
    }

    private static int rgbToString(float r, float g, float b) {
        return new Color((int) (r * 255), (int) (g * 255), (int) (b * 255)).getRGB();
    }
}
