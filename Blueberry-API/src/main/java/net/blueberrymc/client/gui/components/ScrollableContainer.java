package net.blueberrymc.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A scrollable screen
 */
public class ScrollableContainer<E extends AbstractWidget & GuiEventListener> extends AbstractContainerEventHandler {
    public static final Identifier WHITE_TEXTURE_LOCATION = Identifier.parse("minecraft:textures/misc/white.png");
    protected final Minecraft minecraft;
    protected final int itemHeight;
    protected final List<E> children = new ArrayList<>();
    protected int width;
    protected int height;
    protected int top;
    protected int bottom;
    protected int right;
    protected int left;
    private double scrollAmount;
    private boolean renderHeader;
    protected int headerHeight;
    private boolean scrolling;
    private boolean renderBackground = true;
    private boolean renderTopAndBottom = true;

    public ScrollableContainer(@NotNull Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight, int padding) {
        this.minecraft = minecraft;
        this.width = width;
        this.height = height;
        this.top = top;
        this.bottom = bottom;
        this.itemHeight = itemHeight + padding;
        this.left = 0;
        this.right = width;
    }

    protected void setRenderHeader(boolean flag, int i) {
        this.renderHeader = flag;
        this.headerHeight = i;
        if (!flag) {
            this.headerHeight = 0;
        }
    }

    /**
     * Gets the width of the row.
     * @return width
     */
    public int getRowWidth() {
        return this.width / 5 * 4;
    }

    /**
     * Sets whether the background should be rendered.
     * @param flag true if background should be rendered; false otherwise
     */
    public void setRenderBackground(boolean flag) {
        this.renderBackground = flag;
    }

    /**
     * Sets whether the top and bottom should be rendered.
     * @param flag true if top and bottom should be rendered; false otherwise
     */
    public void setRenderTopAndBottom(boolean flag) {
        this.renderTopAndBottom = flag;
    }

    /**
     * Gets the children of this container that have been added to the container.
     * @return children
     */
    @NotNull
    public final List<E> children() {
        return this.children;
    }

    /**
     * Removes all children from this container.
     */
    protected final void clearEntries() {
        this.children.clear();
    }

    /**
     * Removes all children from this container and adds the given children.
     * @param collection collection of children to add
     */
    protected void replaceEntries(@NotNull Collection<E> collection) {
        this.children.clear();
        this.children.addAll(collection);
    }

    /**
     * Gets the children by index.
     * @param i index
     * @return children at index
     * @throws NullPointerException if children is null
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    @NotNull
    protected E getEntry(int i) {
        return Objects.requireNonNull(this.children().get(i));
    }

    /**
     * Adds a child to this container.
     * @param entry child to add
     * @return max index of children (children.length - 1)
     */
    protected int addEntry(@NotNull E entry) {
        this.children.add(entry);
        return this.children.size() - 1;
    }

    /**
     * Gets the number of children in this container.
     * @return number of children
     */
    protected int getItemCount() {
        return this.children().size();
    }

    /**
     * Gets the children at given x and y position.
     * @param x x position
     * @param y y position
     * @return children at x and y position; null if no children at x and y position
     */
    @Nullable
    protected final E getEntryAtPosition(double x, double y) {
        for (E e : this.children()) if (e.isMouseOver(x, y)) return e;
        return null;
    }

    /**
     * Updates the size of this container.
     * @param width width
     * @param height height
     * @param top top
     * @param bottom bottom
     */
    public void updateSize(int width, int height, int top, int bottom) {
        this.width = width;
        this.height = height;
        this.top = top;
        this.bottom = bottom;
        this.left = 0;
        this.right = width;
    }

    public void setLeftPos(int i) {
        this.left = i;
        this.right = i + this.width;
    }

    /**
     * Gets the maximum height of the container.
     * @return maximum height
     */
    protected int getMaxPosition() {
        return this.getItemCount() * this.itemHeight + this.headerHeight;
    }

    protected void clickedHeader(int i, int i2) {
    }

    protected void extractHeader(@NotNull GuiGraphicsExtractor graphics, int i, int i2) {
    }

    protected void extractDecorations(@NotNull GuiGraphicsExtractor graphics, int i, int i2) {
    }

    public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        if (this.isInGameUi()) {
            this.extractTransparentBackground(graphics);
        } else {
            if (this.minecraft.level == null) {
                this.extractPanorama(graphics, a);
            }

            this.extractBlurredBackground(graphics);
            this.extractMenuBackground(graphics);
        }

        this.minecraft.gui.extractDeferredSubtitles();
    }

    public boolean isInGameUi() {
        return false;
    }

    protected void extractBlurredBackground(final GuiGraphicsExtractor graphics) {
        float blurRadius = this.minecraft.options.getMenuBackgroundBlurriness();
        if (blurRadius >= 1.0F) {
            graphics.blurBeforeThisStratum();
        }
    }

    protected void extractPanorama(final GuiGraphicsExtractor graphics, final float a) {
        this.minecraft.gameRenderer.getPanorama().extractRenderState(graphics, this.width, this.height, this.panoramaShouldSpin());
    }

    protected void extractMenuBackground(final GuiGraphicsExtractor graphics) {
        this.extractMenuBackground(graphics, 0, 0, this.width, this.height);
    }

    protected void extractMenuBackground(final GuiGraphicsExtractor graphics, final int x, final int y, final int width, final int height) {
        extractMenuBackgroundTexture(graphics, this.minecraft.level == null ? Screen.MENU_BACKGROUND : Screen.INWORLD_MENU_BACKGROUND, x, y, 0.0F, 0.0F, width, height);
    }

    protected boolean panoramaShouldSpin() {
        return true;
    }

    public static void extractMenuBackgroundTexture(
            final GuiGraphicsExtractor graphics,
            final Identifier menuBackground,
            final int x,
            final int y,
            final float u,
            final float v,
            final int width,
            final int height
    ) {
        int size = 32;
        graphics.blit(RenderPipelines.GUI_TEXTURED, menuBackground, x, y, u, v, width, height, 32, 32);
    }

    public void extractTransparentBackground(final GuiGraphicsExtractor graphics) {
        graphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
    }

    /**
     * Renders the container.
     * @param graphics gui graphics
     * @param mouseX mouse x position
     * @param mouseY mouse y position
     * @param deltaFrameTime delta frame time
     */
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaFrameTime) {
        int i3 = this.getScrollbarPosition();
        int i4 = i3 + 6;
        //this.hovered = this.isMouseOver((double)mouseX, (double)mouseX) ? this.getEntryAtPosition((double)mouseX, (double)mouseX) : null;
        if (this.renderBackground) {
            this.extractBackground(graphics, mouseX, mouseY, deltaFrameTime);
        }

        int i6 = this.getRowLeft();
        int i7 = this.top + 4 - (int)this.getScrollAmount();
        if (this.renderHeader) {
            this.extractHeader(graphics, i6, i7);
        }

        this.extractList(graphics, getRowLeft(), i6, mouseX, mouseY, deltaFrameTime);
        if (this.renderTopAndBottom) {
//            RenderSystem.setShaderTexture(0, Screen.MENU_BACKGROUND);
//            int i8 = 32;
//            RenderSystem.setShaderColor(0.25F, 0.25F, 0.25F, 1.0F);
//            graphics.blit(Screen.MENU_BACKGROUND, this.left, 0, 0.0F, 0.0F, this.width, this.top, 32, 32);
//            graphics.blit(Screen.MENU_BACKGROUND, this.left, this.bottom, 0.0F, (float)this.bottom, this.width, this.height - this.bottom, 32, 32);
//            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//            int i9 = 4;
//            graphics.fillGradient(this.left, this.top, this.right, this.top + 4, -16777216, 0);
//            graphics.fillGradient(this.left, this.bottom - 4, this.right, this.bottom, 0, -16777216);
        }

        int i10 = this.getMaxScroll();
        if (i10 > 0) {
            int i11 = (int)((float)((this.bottom - this.top) * (this.bottom - this.top)) / (float)this.getMaxPosition());
            i11 = Mth.clamp(i11, 32, this.bottom - this.top - 8);
            int i12 = (int)this.getScrollAmount() * (this.bottom - this.top - i11) / i10 + this.top;
            if (i12 < this.top) {
                i12 = this.top;
            }

            graphics.fill(i3, this.top, i4, this.bottom, -16777216);
            graphics.fill(i3, i12, i4, i12 + i11, -8355712);
            graphics.fill(i3, i12, i4 - 1, i12 + i11 - 1, -4144960);
        }

        this.extractDecorations(graphics, mouseX, mouseX);

        children.forEach(e -> e.extractRenderState(graphics, mouseX, mouseY, deltaFrameTime));
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    protected void centerScrollOn(@NotNull E entry) {
        this.setScrollAmount(this.children().indexOf(entry) * this.itemHeight + this.itemHeight / 2 - (this.bottom - this.top) / 2);
    }

    protected void ensureVisible(@NotNull E entry) {
        int i = this.getRowTop(this.children().indexOf(entry));
        int i2 = i - this.top - 4 - this.itemHeight;
        if (i2 < 0) {
            this.scroll(i2);
        }
        int i3 = this.bottom - i - this.itemHeight - this.itemHeight;
        if (i3 < 0) {
            this.scroll(-i3);
        }
    }

    private void scroll(int i) {
        this.setScrollAmount(this.getScrollAmount() + (double)i);
    }

    public double getScrollAmount() {
        return this.scrollAmount;
    }

    public void setScrollAmount(double d) {
        this.scrollAmount = Mth.clamp(d, 0.0D, this.getMaxScroll());
    }

    public int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4));
    }

    protected void updateScrollingState(double x, double y, int i) {
        this.scrolling = i == 0 && x >= (double)this.getScrollbarPosition() && x < (double)(this.getScrollbarPosition() + 6);
    }

    protected int getScrollbarPosition() {
        return this.width / 2 + 124;
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        this.updateScrollingState(event.x(), event.y(), event.button());
        if (!this.isMouseOver(event.x(), event.y())) {
            return false;
        } else {
            E entry = this.getEntryAtPosition(event.x(), event.y());
            if (entry != null) {
                if (entry.mouseClicked(event, doubleClick)) {
                    this.setFocused(entry);
                    this.setDragging(true);
                    return true;
                }
            } else if (event.button() == 0) {
                this.clickedHeader((int)(event.x() - (double)(this.left + this.width / 2 - this.getRowWidth() / 2)), (int)(event.y() - (double)this.top) + (int)this.getScrollAmount() - 4);
                return true;
            }

            return this.scrolling;
        }
    }

    @Override
    public boolean mouseReleased(@NotNull MouseButtonEvent event) {
        if (this.getFocused() != null) {
            this.getFocused().mouseReleased(event);
        }

        return false;
    }

    @Override
    public boolean mouseDragged(@NonNull MouseButtonEvent event, double dx, double dy) {
        if (super.mouseDragged(event, dx, dy)) {
            return true;
        } else if (event.button() == 0 && this.scrolling) {
            if (event.y() < (double)this.top) {
                this.setScrollAmount(0.0D);
            } else if (event.y() > (double)this.bottom) {
                this.setScrollAmount(this.getMaxScroll());
            } else {
                double d5 = Math.max(1, this.getMaxScroll());
                int i2 = this.bottom - this.top;
                int i3 = Mth.clamp((int)((float)(i2 * i2) / (float)this.getMaxPosition()), 32, i2 - 8);
                double d6 = Math.max(1.0D, d5 / (double)(i2 - i3));
                this.setScrollAmount(this.getScrollAmount() + dy * d6);
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean mouseScrolled(double x, double y, double amount) {
        this.setScrollAmount(this.getScrollAmount() - amount * (double)this.itemHeight / 2.0D);
        return true;
    }

    private E previous;
    @Override
    public void mouseMoved(double x, double y) {
        E entry = this.getEntryAtPosition(x, y);
        if (previous != entry) {
            if (previous != null) {
                if (previous.isFocused()) {
                    previous.setFocused(false);
                }
            }
            if (entry != null) {
                if (!entry.isFocused()) {
                    entry.setFocused(true);
                }
            }
        }
        previous = entry;
    }

    public boolean isMouseOver(double x, double y) {
        return y >= (double)this.top && y <= (double)this.bottom && x >= (double)this.left && x <= (double)this.right;
    }

    protected void extractList(@NotNull GuiGraphicsExtractor guiGraphics, int rowLeft, int adjustedScrollAmount, int mouseX, int mouseY, float deltaFrameTime) {
        int itemCount = this.getItemCount();
        int offset = 38;
        int prevY = Integer.MIN_VALUE;
        for (int i = 0; i < itemCount; ++i) {
            offset += 22;
            int rowTop = this.getRowTop(i);
            int rowBottom = this.getRowBottom(i);
            if (rowBottom >= this.top && rowTop <= this.bottom) {
                E entry = this.getEntry(i);
                entry.extractRenderState(guiGraphics, mouseX, mouseY, deltaFrameTime);
                if (prevY == entry.getY()) {
                    offset -= 22;
                }
                prevY = entry.getY();
                entry.setY((int) (offset - getScrollAmount()));
            }
        }

    }

    public int getRowLeft() {
        return this.left + this.width / 2 - this.getRowWidth() / 2 + 2;
    }

    public int getRowRight() {
        return this.getRowLeft() + this.getRowWidth();
    }

    protected int getRowTop(int i) {
        return this.top + 4 - (int)this.getScrollAmount() + i * this.itemHeight + this.headerHeight;
    }

    private int getRowBottom(int i) {
        return this.getRowTop(i) + this.itemHeight;
    }

    public boolean isFocused() {
        return false;
    }

    @Nullable
    protected E remove(int i) {
        return this.children.remove(i);
    }

    public enum SelectionDirection {
        UP,
        DOWN
    }
}
