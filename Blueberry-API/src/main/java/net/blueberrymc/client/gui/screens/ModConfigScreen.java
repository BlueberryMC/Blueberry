package net.blueberrymc.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blueberrymc.client.gui.components.ScrollableContainer;
import net.blueberrymc.client.resources.BlueberryText;
import net.blueberrymc.common.bml.config.BooleanVisualConfig;
import net.blueberrymc.common.bml.config.ClassVisualConfig;
import net.blueberrymc.common.bml.config.CompoundVisualConfig;
import net.blueberrymc.common.bml.config.CycleVisualConfig;
import net.blueberrymc.common.bml.config.DoubleVisualConfig;
import net.blueberrymc.common.bml.config.IntegerVisualConfig;
import net.blueberrymc.common.bml.config.LongVisualConfig;
import net.blueberrymc.common.bml.config.RootCompoundVisualConfig;
import net.blueberrymc.common.bml.config.StringVisualConfig;
import net.blueberrymc.common.bml.config.VisualConfig;
import net.blueberrymc.util.NameGetter;
import net.blueberrymc.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

@SuppressWarnings("PatternVariableCanBeUsed") // maven is still 8
public class ModConfigScreen extends BlueberryScreen {
    private static final Component UNKNOWN_TEXT = new TextComponent("<unknown>").withStyle(ChatFormatting.GRAY);
    private static final Component BOOLEAN_TRUE = new TextComponent("true").withStyle(ChatFormatting.GREEN);
    private static final Component BOOLEAN_FALSE = new TextComponent("false").withStyle(ChatFormatting.RED);
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
    public void mouseMoved(double x, double y) {
        super.mouseMoved(x, y);
        this.children().forEach(listener -> listener.mouseMoved(x, y));
    }

    protected void init() {
        this.children().clear();
        callbacks.clear();
        ScrollableContainer<AbstractWidget> container = new ScrollableContainer<>(Objects.requireNonNull(this.minecraft), this.width, this.height, 58, this.height - 50, 20, 2);
        this.addRenderableWidget(this.backButton = new Button(this.width / 2 - 77, this.height - 38, 154, 20, CommonComponents.GUI_BACK, (button) -> {
            this.minecraft.setScreen(this.previousScreen);
            if (compoundVisualConfig instanceof RootCompoundVisualConfig) {
                ((RootCompoundVisualConfig) compoundVisualConfig).onChanged();
            }
        }));
        int offset = 38;
        int _maxWidth = 0;
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
            Function<PoseStack, BiConsumer<Integer, Integer>> onTooltipFunction;
            Button.OnTooltip onTooltip;
            MutableComponent tooltip = new TextComponent("");
            Component desc = config.getDescription();
            if (desc != null) tooltip.append(desc.plainCopy().withStyle(ChatFormatting.YELLOW)).append("\n");
            Object def = config instanceof CompoundVisualConfig ? null : config.getDefaultValue();
            if (def != null) {
                String s = def.toString();
                if (def instanceof NameGetter) {
                    s = ((NameGetter) def).getName();
                }
                // if (def instanceof Enum<?>) s = ((Enum<?>) def).name();
                tooltip.append(new BlueberryText("blueberry", "gui.screens.mod_config.default", s + ChatFormatting.AQUA).withStyle(ChatFormatting.AQUA)).append("\n");
            }
            if (config instanceof IntegerVisualConfig) {
                IntegerVisualConfig integerVisualConfig = (IntegerVisualConfig) config;
                tooltip.append(new BlueberryText("blueberry", "gui.screens.mod_config.number_min_max", Integer.toString(integerVisualConfig.getMin()), Integer.toString(integerVisualConfig.getMax())).withStyle(ChatFormatting.AQUA)).append("\n");
            }
            if (config instanceof LongVisualConfig) {
                LongVisualConfig longVisualConfig = (LongVisualConfig) config;
                tooltip.append(new BlueberryText("blueberry", "gui.screens.mod_config.number_min_max", Long.toString(longVisualConfig.getMin()), Long.toString(longVisualConfig.getMax())).withStyle(ChatFormatting.AQUA)).append("\n");
            }
            if (config instanceof DoubleVisualConfig) {
                DoubleVisualConfig doubleVisualConfig = (DoubleVisualConfig) config;
                tooltip.append(new BlueberryText("blueberry", "gui.screens.mod_config.number_min_max", Double.toString(doubleVisualConfig.getMin()), Double.toString(doubleVisualConfig.getMax())).withStyle(ChatFormatting.AQUA)).append("\n");
            }
            if (config instanceof StringVisualConfig) {
                StringVisualConfig stringVisualConfig = (StringVisualConfig) config;
                Pattern pattern = stringVisualConfig.getPattern();
                if (pattern != null) tooltip.append(new BlueberryText("blueberry", "gui.screens.mod_config.pattern", ChatFormatting.GOLD + pattern.pattern() + ChatFormatting.AQUA).withStyle(ChatFormatting.AQUA)).append("\n");
            }
            if (config instanceof ClassVisualConfig) {
                ClassVisualConfig classVisualConfig = (ClassVisualConfig) config;
                Pattern pattern = classVisualConfig.getPattern();
                if (pattern != null) tooltip.append(new BlueberryText("blueberry", "gui.screens.mod_config.pattern", ChatFormatting.GOLD + pattern.pattern() + ChatFormatting.AQUA).withStyle(ChatFormatting.AQUA)).append("\n");
            }
            if (config.isRequiresRestart()) tooltip.append(new BlueberryText("blueberry", "gui.screens.mod_config.requires_restart").withStyle(ChatFormatting.RED)).append("\n");
            if (tooltip.getSiblings().size() > 0) tooltip.getSiblings().remove(tooltip.getSiblings().size() - 1); // removes last \n
            if (tooltip.toString().length() > 0) {
                onTooltipFunction = (poseStack) -> (x, y) -> this.renderTooltip(poseStack, this.minecraft.font.split(tooltip, Math.max(this.width / 3, 170)), x, y);
                onTooltip = (button, poseStack, x, y) -> onTooltipFunction.apply(poseStack).accept(x, y);
            } else {
                onTooltipFunction = (poseStack) -> (x, y) -> {};
                onTooltip = Button.NO_TOOLTIP;
            }
            if (config instanceof CompoundVisualConfig) {
                CompoundVisualConfig compoundVisualConfig = (CompoundVisualConfig) config;
                Component component = Util.getOrDefault(
                        compoundVisualConfig.getComponent(),
                        compoundVisualConfig.getTitle(),
                        UNKNOWN_TEXT
                );
                container.children().add(new Button(this.width / 2 - this.width / 8, (offset += 22), this.width / 4, 20, component, button ->
                        this.minecraft.setScreen(new ModConfigScreen(compoundVisualConfig, this)), onTooltip)
                );
            } else if (config instanceof BooleanVisualConfig) {
                BooleanVisualConfig booleanVisualConfig = (BooleanVisualConfig) config;
                container.children().add(new Button(this.width / 2 + 6, (offset += 22), Math.min(maxWidth, this.width / 6), 20, getButtonMessage(booleanVisualConfig), (button) -> {
                    Boolean curr = booleanVisualConfig.get();
                    booleanVisualConfig.set(!(curr != null && curr));
                    updateBooleanButton(booleanVisualConfig, button);
                }, onTooltip));
                addLabel.accept(config, offset);
            } else if (config instanceof CycleVisualConfig) {
                CycleVisualConfig<?> cycleVisualConfig = (CycleVisualConfig<?>) config;
                container.children().add(
                        new Button(
                                this.width / 2 + 6,
                                (offset += 22),
                                Math.min(maxWidth, this.width / 6),
                                20,
                                new TextComponent(cycleVisualConfig.getCurrentName()),
                                (button) -> button.setMessage(new TextComponent(cycleVisualConfig.getNextName())),
                                onTooltip
                        )
                );
                addLabel.accept(config, offset);
            } else if (config instanceof IntegerVisualConfig) {
                IntegerVisualConfig integerVisualConfig = (IntegerVisualConfig) config;
                EditBox editBox = new EditBox(font, this.width / 2 + 6, (offset += 22), Math.min(maxWidth, this.width / 6), 20, new TextComponent("")) {
                    @Override
                    public void renderToolTip(@NotNull PoseStack poseStack, int x, int y) {
                        onTooltipFunction.apply(poseStack).accept(x, y);
                    }
                };
                Integer defValue = integerVisualConfig.get();
                if (defValue == null) defValue = integerVisualConfig.getDefaultValue();
                if (defValue == null) defValue = integerVisualConfig.getMin();
                editBox.setResponder((s) -> {
                    try {
                        int i = Integer.parseInt(s);
                        if (i < integerVisualConfig.getMin() || i > integerVisualConfig.getMax()) throw new NumberFormatException();
                        editBox.setTextColor(0xe0e0e0);
                        integerVisualConfig.set(i);
                        unblock(editBox);
                    } catch (NumberFormatException e) {
                        editBox.setTextColor(0xff0000);
                        block(editBox);
                    }
                });
                editBox.setValue(Integer.toString(defValue));
                container.children().add(editBox);
                addLabel.accept(config, offset);
            } else if (config instanceof LongVisualConfig) {
                LongVisualConfig longVisualConfig = (LongVisualConfig) config;
                EditBox editBox = new EditBox(font, this.width / 2 + 6, (offset += 22), Math.min(maxWidth, this.width / 6), 20, new TextComponent("")) {
                    @Override
                    public void renderToolTip(@NotNull PoseStack poseStack, int x, int y) {
                        onTooltipFunction.apply(poseStack).accept(x, y);
                    }
                };
                Long defValue = longVisualConfig.get();
                if (defValue == null) defValue = longVisualConfig.getDefaultValue();
                if (defValue == null) defValue = longVisualConfig.getMin();
                editBox.setResponder((s) -> {
                    try {
                        long l = Long.parseLong(s);
                        if (l < longVisualConfig.getMin() || l > longVisualConfig.getMax()) throw new NumberFormatException();
                        editBox.setTextColor(0xe0e0e0);
                        longVisualConfig.set(l);
                        unblock(editBox);
                    } catch (NumberFormatException e) {
                        editBox.setTextColor(0xff0000);
                        block(editBox);
                    }
                });
                editBox.setValue(Long.toString(defValue));
                container.children().add(editBox);
                addLabel.accept(config, offset);
            } else if (config instanceof StringVisualConfig) {
                StringVisualConfig stringVisualConfig = (StringVisualConfig) config;
                EditBox editBox = new EditBox(font, this.width / 2 + 6, (offset += 22), Math.min(maxWidth, this.width / 6), 20, new TextComponent("")) {
                    @Override
                    public void renderToolTip(@NotNull PoseStack poseStack, int x, int y) {
                        onTooltipFunction.apply(poseStack).accept(x, y);
                    }
                };
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
            } else if (config instanceof ClassVisualConfig) {
                ClassVisualConfig classVisualConfig = (ClassVisualConfig) config;
                EditBox editBox = new EditBox(font, this.width / 2 + 6, (offset += 22), Math.min(maxWidth, this.width / 6), 20, new TextComponent("")) {
                    @Override
                    public void renderToolTip(@NotNull PoseStack poseStack, int x, int y) {
                        onTooltipFunction.apply(poseStack).accept(x, y);
                    }
                };
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
                editBox.setValue(defValue != null ? defValue.getCanonicalName() : "");
                container.children().add(editBox);
                addLabel.accept(config, offset);
            } else if (config instanceof DoubleVisualConfig) {
                DoubleVisualConfig doubleVisualConfig = (DoubleVisualConfig) config;
                EditBox editBox = new EditBox(font, this.width / 2 + 6, (offset += 22), Math.min(maxWidth, this.width / 6), 20, new TextComponent("")) {
                    @Override
                    public void renderToolTip(@NotNull PoseStack poseStack, int x, int y) {
                        onTooltipFunction.apply(poseStack).accept(x, y);
                    }
                };
                Double defValue = doubleVisualConfig.get();
                if (defValue == null) defValue = doubleVisualConfig.getDefaultValue();
                if (defValue == null) defValue = doubleVisualConfig.getMin();
                editBox.setResponder((s) -> {
                    try {
                        double i = Double.parseDouble(s);
                        if (i < doubleVisualConfig.getMin() || i > doubleVisualConfig.getMax()) throw new NumberFormatException();
                        editBox.setTextColor(0xe0e0e0);
                        doubleVisualConfig.set(i);
                        unblock(editBox);
                    } catch (NumberFormatException e) {
                        editBox.setTextColor(0xff0000);
                        block(editBox);
                    }
                });
                editBox.setValue(Double.toString(defValue));
                container.children().add(editBox);
                addLabel.accept(config, offset);
            }
        }
        this.children().add(container);
        super.init();
    }

    private void block(Object blocker) {
        if (!blockers.contains(blocker)) blockers.add(blocker);
        backButton.active = blockers.isEmpty();
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
    private static BlueberryText title(@NotNull CompoundVisualConfig compoundVisualConfig) {
        BlueberryText text = new BlueberryText("blueberry", "gui.screens.mod_config.title");
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
        components.forEach(component -> text.append(" > ").append(component));
        text.append(" > ");
        if (compoundVisualConfig.getComponent() != null) {
            text.append(compoundVisualConfig.getComponent());
        } else if (compoundVisualConfig.getTitle() != null) {
            text.append(compoundVisualConfig.getTitle());
        } else {
            text.append("Unknown");
        }
        return text;
    }
}