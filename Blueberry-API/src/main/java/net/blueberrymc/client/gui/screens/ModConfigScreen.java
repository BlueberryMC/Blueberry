package net.blueberrymc.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blueberrymc.client.gui.components.ScrollableContainer;
import net.blueberrymc.client.resources.BlueberryText;
import net.blueberrymc.common.bml.config.BooleanVisualConfig;
import net.blueberrymc.common.bml.config.CompoundVisualConfig;
import net.blueberrymc.common.bml.config.RootCompoundVisualConfig;
import net.blueberrymc.common.bml.config.VisualConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModConfigScreen extends Screen {
    private static final Component BOOLEAN_TRUE = new TextComponent("true").withStyle(ChatFormatting.GREEN);
    private static final Component BOOLEAN_FALSE = new TextComponent("false").withStyle(ChatFormatting.RED);
    private final CompoundVisualConfig compoundVisualConfig;
    private final Screen previousScreen;
    private final Component description;

    public ModConfigScreen(CompoundVisualConfig compoundVisualConfig, Screen screen) {
        super(title(compoundVisualConfig));
        this.compoundVisualConfig = compoundVisualConfig;
        RootCompoundVisualConfig root = this.compoundVisualConfig.getRoot();
        this.description = root == null ? null : root.title;
        this.previousScreen = screen;
    }

    @Override
    public void mouseMoved(double d, double d2) {
        super.mouseMoved(d, d2);
        this.children.forEach(listener -> listener.mouseMoved(d, d2));
    }

    protected void init() {
        ScrollableContainer<Button> container = new ScrollableContainer<>(this.minecraft, this.width, this.height, 58, this.height - 50, 20, 2);
        this.addButton(new Button(this.width / 2 - 77, this.height - 38, 154, 20, CommonComponents.GUI_BACK, (button) -> {
            this.minecraft.setScreen(this.previousScreen);
            if (compoundVisualConfig instanceof RootCompoundVisualConfig) {
                ((RootCompoundVisualConfig) compoundVisualConfig).onChanged();
            }
        }));
        int offset = 38;
        for (VisualConfig<?> config : this.compoundVisualConfig) {
            if (config instanceof CompoundVisualConfig) {
                CompoundVisualConfig compoundVisualConfig = (CompoundVisualConfig) config;
                Component component;
                if (compoundVisualConfig.getComponent() != null) {
                    component = compoundVisualConfig.getComponent();
                } else if (compoundVisualConfig.getTitle() != null) {
                    component = compoundVisualConfig.getTitle();
                } else {
                    component = new TextComponent("<unknown>");
                }
                container.children().add(new Button(this.width / 2 - this.width / 8, (offset += 22), this.width / 4, 20, component, button -> {
                    this.minecraft.setScreen(new ModConfigScreen(compoundVisualConfig, this));
                }));
            } else if (config instanceof BooleanVisualConfig) {
                BooleanVisualConfig booleanVisualConfig = (BooleanVisualConfig) config;
                container.children().add(new Button(this.width / 2 - this.width / 8, (offset += 22), this.width / 4, 20, getButtonMessage(booleanVisualConfig), (button) -> {
                    Boolean curr = booleanVisualConfig.get();
                    booleanVisualConfig.set(!(curr != null && curr));
                    updateBooleanButton(booleanVisualConfig, button);
                }));
            }
        }
        this.children.add(container);
        super.init();
    }

    private void updateBooleanButton(BooleanVisualConfig booleanVisualConfig, Button button) {
        button.setMessage(getButtonMessage(booleanVisualConfig));
    }

    private static Component getButtonMessage(BooleanVisualConfig booleanVisualConfig) {
        Boolean bool = booleanVisualConfig.get();
        MutableComponent component = new TextComponent("").withStyle(ChatFormatting.YELLOW);
        if (booleanVisualConfig.getComponent() != null) {
            component.append(booleanVisualConfig.getComponent());
        } else {
            component.append("<unknown>");
        }
        component.append(new TextComponent(": ").withStyle(ChatFormatting.WHITE)).append(bool == null || !bool ? BOOLEAN_FALSE : BOOLEAN_TRUE);
        return component;
    }

    public void render(PoseStack poseStack, int i, int i2, float f) {
        this.renderBackground(poseStack);
        this.children.forEach(e -> {
            if (e instanceof ScrollableContainer) {
                ((ScrollableContainer<?>) e).render(poseStack, i, i2, f);
            }
        });
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 16, 16777215);
        if (this.description != null) {
            drawCenteredString(poseStack, this.font, this.description, this.width / 2, 30, 16777215);
        }
        super.render(poseStack, i, i2, f);
    }

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