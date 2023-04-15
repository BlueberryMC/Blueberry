package net.blueberrymc.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A scrollable screen
 */
public class ScrollableContainer<E extends AbstractWidget & GuiEventListener> extends AbstractContainerEventHandler {
    public static final ResourceLocation WHITE_TEXTURE_LOCATION = new ResourceLocation("textures/misc/white.png");
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

    protected void renderHeader(PoseStack poseStack, int i, int i2) {
    }

    protected void renderBackground(@NotNull PoseStack poseStack) {
    }

    protected void renderDecorations(@NotNull PoseStack poseStack, int i, int i2) {
    }

    protected void enableScissor() {
        enableScissor(this.left, this.bottom, this.right, this.top);
    }

    /**
     * Renders the container.
     * @param poseStack pose stack
     * @param mouseX mouse x position
     * @param mouseY mouse y position
     * @param deltaFrameTime delta frame time
     */
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float deltaFrameTime) {
        this.renderBackground(poseStack);
        int i3 = this.getScrollbarPosition();
        int i4 = i3 + 6;
        //this.hovered = this.isMouseOver((double)mouseX, (double)mouseX) ? this.getEntryAtPosition((double)mouseX, (double)mouseX) : null;
        if (this.renderBackground) {
            RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
            RenderSystem.setShaderColor(0.125F, 0.125F, 0.125F, 1.0F);
            int i5 = 32;
            blit(poseStack, this.left, this.top, (float)this.right, (float)(this.bottom + (int)this.getScrollAmount()), this.right - this.left, this.bottom - this.top, 32, 32);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        int i6 = this.getRowLeft();
        int i7 = this.top + 4 - (int)this.getScrollAmount();
        this.enableScissor();
        if (this.renderHeader) {
            this.renderHeader(poseStack, i6, i7);
        }

        this.renderList(poseStack, getRowLeft(), i6, mouseX, mouseY, deltaFrameTime);
        disableScissor();
        if (this.renderTopAndBottom) {
            RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
            int i8 = 32;
            RenderSystem.setShaderColor(0.25F, 0.25F, 0.25F, 1.0F);
            blit(poseStack, this.left, 0, 0.0F, 0.0F, this.width, this.top, 32, 32);
            blit(poseStack, this.left, this.bottom, 0.0F, (float)this.bottom, this.width, this.height - this.bottom, 32, 32);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int i9 = 4;
            fillGradient(poseStack, this.left, this.top, this.right, this.top + 4, -16777216, 0);
            fillGradient(poseStack, this.left, this.bottom - 4, this.right, this.bottom, 0, -16777216);
        }

        int i10 = this.getMaxScroll();
        if (i10 > 0) {
            int i11 = (int)((float)((this.bottom - this.top) * (this.bottom - this.top)) / (float)this.getMaxPosition());
            i11 = Mth.clamp(i11, 32, this.bottom - this.top - 8);
            int i12 = (int)this.getScrollAmount() * (this.bottom - this.top - i11) / i10 + this.top;
            if (i12 < this.top) {
                i12 = this.top;
            }

            fill(poseStack, i3, this.top, i4, this.bottom, -16777216);
            fill(poseStack, i3, i12, i4, i12 + i11, -8355712);
            fill(poseStack, i3, i12, i4 - 1, i12 + i11 - 1, -4144960);
        }

        this.renderDecorations(poseStack, mouseX, mouseX);

        children.forEach(e -> e.render(poseStack, mouseX, mouseY, deltaFrameTime));

        RenderSystem.disableBlend();
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

    public boolean mouseClicked(double x, double y, int i) {
        this.updateScrollingState(x, y, i);
        if (!this.isMouseOver(x, y)) {
            return false;
        } else {
            E entry = this.getEntryAtPosition(x, y);
            if (entry != null) {
                if (entry.mouseClicked(x, y, i)) {
                    this.setFocused(entry);
                    this.setDragging(true);
                    return true;
                }
            } else if (i == 0) {
                this.clickedHeader((int)(x - (double)(this.left + this.width / 2 - this.getRowWidth() / 2)), (int)(y - (double)this.top) + (int)this.getScrollAmount() - 4);
                return true;
            }

            return this.scrolling;
        }
    }

    public boolean mouseReleased(double x, double y, int i) {
        if (this.getFocused() != null) {
            this.getFocused().mouseReleased(x, y, i);
        }

        return false;
    }

    public boolean mouseDragged(double x1, double y1, int i, double x2, double y2) {
        if (super.mouseDragged(x1, y1, i, x2, y2)) {
            return true;
        } else if (i == 0 && this.scrolling) {
            if (y1 < (double)this.top) {
                this.setScrollAmount(0.0D);
            } else if (y1 > (double)this.bottom) {
                this.setScrollAmount(this.getMaxScroll());
            } else {
                double d5 = Math.max(1, this.getMaxScroll());
                int i2 = this.bottom - this.top;
                int i3 = Mth.clamp((int)((float)(i2 * i2) / (float)this.getMaxPosition()), 32, i2 - 8);
                double d6 = Math.max(1.0D, d5 / (double)(i2 - i3));
                this.setScrollAmount(this.getScrollAmount() + y2 * d6);
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

    protected void renderList(@NotNull PoseStack poseStack, int rowLeft, int adjustedScrollAmount, int i3, int i4, float deltaFrameTime) {
        int itemCount = this.getItemCount();
        int offset = 38;
        int prevY = Integer.MIN_VALUE;
        for (int i = 0; i < itemCount; ++i) {
            offset += 22;
            int rowTop = this.getRowTop(i);
            int rowBottom = this.getRowBottom(i);
            if (rowBottom >= this.top && rowTop <= this.bottom) {
                E entry = this.getEntry(i);
                entry.render(poseStack, i3, i4, deltaFrameTime);
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
