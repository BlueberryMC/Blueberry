package net.blueberrymc.client.gui.components;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;
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
public class ScrollableContainer<E extends GuiEventListener & Widget> extends AbstractContainerEventHandler implements Widget {
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

    protected void renderHeader(@NotNull PoseStack poseStack, int i, int i2, @NotNull Tesselator tesselator) {
    }

    protected void renderBackground(@NotNull PoseStack poseStack) {
    }

    protected void renderDecorations(@NotNull PoseStack poseStack, int i, int i2) {
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
        int scrollbarPosition = this.getScrollbarPosition();
        int i4 = scrollbarPosition + 6;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        if (this.renderBackground) {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferBuilder.vertex(this.left, this.bottom, 0.0D).uv((float)this.left / 32.0F, (float)(this.bottom + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            bufferBuilder.vertex(this.right, this.bottom, 0.0D).uv((float)this.right / 32.0F, (float)(this.bottom + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            bufferBuilder.vertex(this.right, this.top, 0.0D).uv((float)this.right / 32.0F, (float)(this.top + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            bufferBuilder.vertex(this.left, this.top, 0.0D).uv((float)this.left / 32.0F, (float)(this.top + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            tesselator.end();
        }

        int rowLeft = this.getRowLeft();
        int i6 = this.top + 4 - (int)this.getScrollAmount();
        if (this.renderHeader) {
            this.renderHeader(poseStack, rowLeft, i6, tesselator);
        }

        this.renderList(poseStack, rowLeft, i6, mouseX, mouseY, deltaFrameTime);
        if (this.renderTopAndBottom) {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(519);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferBuilder.vertex(this.left, this.top, -100.0D).uv(0.0F, (float)this.top / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferBuilder.vertex(this.left + this.width, this.top, -100.0D).uv((float)this.width / 32.0F, (float)this.top / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferBuilder.vertex(this.left + this.width, 0.0D, -100.0D).uv((float)this.width / 32.0F, 0.0F).color(64, 64, 64, 255).endVertex();
            bufferBuilder.vertex(this.left, 0.0D, -100.0D).uv(0.0F, 0.0F).color(64, 64, 64, 255).endVertex();
            bufferBuilder.vertex(this.left, this.height, -100.0D).uv(0.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferBuilder.vertex(this.left + this.width, this.height, -100.0D).uv((float)this.width / 32.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferBuilder.vertex(this.left + this.width, this.bottom, -100.0D).uv((float)this.width / 32.0F, (float)this.bottom / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferBuilder.vertex(this.left, this.bottom, -100.0D).uv(0.0F, (float)this.bottom / 32.0F).color(64, 64, 64, 255).endVertex();
            tesselator.end();
            RenderSystem.depthFunc(515);
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            RenderSystem.disableTexture();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, WHITE_TEXTURE_LOCATION);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferBuilder.vertex(this.left, this.top + 4, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 0).endVertex();
            bufferBuilder.vertex(this.right, this.top + 4, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 0).endVertex();
            bufferBuilder.vertex(this.right, this.top, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(this.left, this.top, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(this.left, this.bottom, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(this.right, this.bottom, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(this.right, this.bottom - 4, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 0).endVertex();
            bufferBuilder.vertex(this.left, this.bottom - 4, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 0).endVertex();
            tesselator.end();
        }

        int maxScroll = this.getMaxScroll();
        if (maxScroll > 0) {
            RenderSystem.disableTexture();
            int i10 = (int)((float)((this.bottom - this.top) * (this.bottom - this.top)) / (float)this.getMaxPosition());
            i10 = Mth.clamp(i10, 32, this.bottom - this.top - 8);
            int i11 = (int)this.getScrollAmount() * (this.bottom - this.top - i10) / maxScroll + this.top;
            if (i11 < this.top) {
                i11 = this.top;
            }

            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferBuilder.vertex(scrollbarPosition, this.bottom, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(i4, this.bottom, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(i4, this.top, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(scrollbarPosition, this.top, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(scrollbarPosition, i11 + i10, 0.0D).uv(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
            bufferBuilder.vertex(i4, i11 + i10, 0.0D).uv(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
            bufferBuilder.vertex(i4, i11, 0.0D).uv(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
            bufferBuilder.vertex(scrollbarPosition, i11, 0.0D).uv(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
            bufferBuilder.vertex(scrollbarPosition, i11 + i10 - 1, 0.0D).uv(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
            bufferBuilder.vertex(i4 - 1, i11 + i10 - 1, 0.0D).uv(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
            bufferBuilder.vertex(i4 - 1, i11, 0.0D).uv(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
            bufferBuilder.vertex(scrollbarPosition, i11, 0.0D).uv(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
            tesselator.end();
        }

        this.renderDecorations(poseStack, mouseX, mouseY);
        children.forEach(e -> {
            if (e instanceof AbstractWidget) {
                if (((AbstractWidget) e).isHoveredOrFocused()) {
                    ((AbstractWidget) e).renderToolTip(poseStack, mouseX, mouseY);
                }
            }
        });
        RenderSystem.enableTexture();
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
                if (previous instanceof AbstractWidget) {
                    if (((AbstractWidget) previous).isFocused()) {
                        previous.changeFocus(true);
                    }
                } else {
                    previous.changeFocus(true);
                }
            }
            if (entry != null) {
                if (entry instanceof AbstractWidget) {
                    if (!((AbstractWidget) entry).isFocused()) {
                        entry.changeFocus(true);
                    }
                } else {
                    entry.changeFocus(true);
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
                if (entry instanceof AbstractWidget aw) {
                    if (prevY == aw.y) {
                        offset -= 22;
                    }
                    prevY = aw.y;
                    aw.y = (int) (offset - getScrollAmount());
                }
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

    protected boolean isFocused() {
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
