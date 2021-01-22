package net.blueberrymc.client.gui.components;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ScrollableContainer<E extends GuiEventListener & Widget> extends AbstractContainerEventHandler implements Widget {
   protected final Minecraft minecraft;
   protected final int itemHeight;
   protected final List<E> children = new ArrayList<>();
   protected int width;
   protected int height;
   protected int y0;
   protected int y1;
   protected int x1;
   protected int x0;
   protected boolean centerListVertically = true;
   private double scrollAmount;
   private boolean renderSelection = true;
   private boolean renderHeader;
   protected int headerHeight;
   private boolean scrolling;
   private boolean renderBackground = true;
   private boolean renderTopAndBottom = true;
   private final int padding;

   public ScrollableContainer(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight, int padding) {
      this.minecraft = minecraft;
      this.width = width;
      this.height = height;
      this.y0 = y0;
      this.y1 = y1;
      this.itemHeight = itemHeight + padding;
      this.x0 = 0;
      this.x1 = width;
      this.padding = padding;
   }

   public void setRenderSelection(boolean flag) {
      this.renderSelection = flag;
   }

   protected void setRenderHeader(boolean flag, int i) {
      this.renderHeader = flag;
      this.headerHeight = i;
      if (!flag) {
         this.headerHeight = 0;
      }

   }

   public int getRowWidth() {
      return this.width / 5 * 4;
   }

   public void setRenderBackground(boolean flag) {
      this.renderBackground = flag;
   }

   public void setRenderTopAndBottom(boolean flag) {
      this.renderTopAndBottom = flag;
   }

   public final List<E> children() {
      return this.children;
   }

   protected final void clearEntries() {
      this.children.clear();
   }

   protected void replaceEntries(Collection<E> collection) {
      this.children.clear();
      this.children.addAll(collection);
   }

   protected E getEntry(int i) {
      return this.children().get(i);
   }

   protected int addEntry(E entry) {
      this.children.add(entry);
      return this.children.size() - 1;
   }

   protected int getItemCount() {
      return this.children().size();
   }

   @Nullable
   protected final E getEntryAtPosition(double d, double d2) {
      int i = this.getRowWidth() / 2;
      int i2 = this.x0 + this.width / 2;
      int i3 = i2 - i;
      int i4 = i2 + i;
      int i5 = Mth.floor(d2 - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
      int i6 = i5 / this.itemHeight;
      return d < (double)this.getScrollbarPosition() && d >= (double)i3 && d <= (double)i4 && i6 >= 0 && i5 >= 0 && i6 < this.getItemCount() ? this.children().get(i6) : null;
   }

   public void updateSize(int i, int i2, int i3, int i4) {
      this.width = i;
      this.height = i2;
      this.y0 = i3;
      this.y1 = i4;
      this.x0 = 0;
      this.x1 = i;
   }

   public void setLeftPos(int i) {
      this.x0 = i;
      this.x1 = i + this.width;
   }

   protected int getMaxPosition() {
      return this.getItemCount() * this.itemHeight + this.headerHeight;
   }

   protected void clickedHeader(int i, int i2) {
   }

   protected void renderHeader(PoseStack poseStack, int i, int i2, Tesselator tesselator) {
   }

   protected void renderBackground(PoseStack poseStack) {
   }

   protected void renderDecorations(PoseStack poseStack, int i, int i2) {
   }

   public void render(PoseStack poseStack, int i, int i2, float f) {
      this.renderBackground(poseStack);
      int i3 = this.getScrollbarPosition();
      int i4 = i3 + 6;
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferBuilder = tesselator.getBuilder();
      if (this.renderBackground) {
         this.minecraft.getTextureManager().bind(GuiComponent.BACKGROUND_LOCATION);
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
         bufferBuilder.vertex(this.x0, this.y1, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
         bufferBuilder.vertex(this.x1, this.y1, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
         bufferBuilder.vertex(this.x1, this.y0, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
         bufferBuilder.vertex(this.x0, this.y0, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
         tesselator.end();
      }

      int i5 = this.getRowLeft();
      int i6 = this.y0 + 4 - (int)this.getScrollAmount();
      if (this.renderHeader) {
         this.renderHeader(poseStack, i5, i6, tesselator);
      }

      this.renderList(poseStack, i5, i6, i, i2, f);
      if (this.renderTopAndBottom) {
         this.minecraft.getTextureManager().bind(GuiComponent.BACKGROUND_LOCATION);
         RenderSystem.enableDepthTest();
         RenderSystem.depthFunc(519);
         float f3 = 32.0F;
         int i7 = -100;
         bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
         bufferBuilder.vertex(this.x0, this.y0, -100.0D).uv(0.0F, (float)this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
         bufferBuilder.vertex(this.x0 + this.width, this.y0, -100.0D).uv((float)this.width / 32.0F, (float)this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
         bufferBuilder.vertex(this.x0 + this.width, 0.0D, -100.0D).uv((float)this.width / 32.0F, 0.0F).color(64, 64, 64, 255).endVertex();
         bufferBuilder.vertex(this.x0, 0.0D, -100.0D).uv(0.0F, 0.0F).color(64, 64, 64, 255).endVertex();
         bufferBuilder.vertex(this.x0, this.height, -100.0D).uv(0.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
         bufferBuilder.vertex(this.x0 + this.width, this.height, -100.0D).uv((float)this.width / 32.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
         bufferBuilder.vertex(this.x0 + this.width, this.y1, -100.0D).uv((float)this.width / 32.0F, (float)this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
         bufferBuilder.vertex(this.x0, this.y1, -100.0D).uv(0.0F, (float)this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
         tesselator.end();
         RenderSystem.depthFunc(515);
         RenderSystem.disableDepthTest();
         RenderSystem.enableBlend();
         RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
         RenderSystem.disableAlphaTest();
         RenderSystem.shadeModel(7425);
         RenderSystem.disableTexture();
         int i8 = 4;
         bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
         bufferBuilder.vertex(this.x0, this.y0 + 4, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 0).endVertex();
         bufferBuilder.vertex(this.x1, this.y0 + 4, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 0).endVertex();
         bufferBuilder.vertex(this.x1, this.y0, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
         bufferBuilder.vertex(this.x0, this.y0, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
         bufferBuilder.vertex(this.x0, this.y1, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
         bufferBuilder.vertex(this.x1, this.y1, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
         bufferBuilder.vertex(this.x1, this.y1 - 4, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 0).endVertex();
         bufferBuilder.vertex(this.x0, this.y1 - 4, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 0).endVertex();
         tesselator.end();
      }

      int i9 = this.getMaxScroll();
      if (i9 > 0) {
         RenderSystem.disableTexture();
         int i10 = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
         i10 = Mth.clamp(i10, 32, this.y1 - this.y0 - 8);
         int i11 = (int)this.getScrollAmount() * (this.y1 - this.y0 - i10) / i9 + this.y0;
         if (i11 < this.y0) {
            i11 = this.y0;
         }

         bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
         bufferBuilder.vertex(i3, this.y1, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
         bufferBuilder.vertex(i4, this.y1, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
         bufferBuilder.vertex(i4, this.y0, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
         bufferBuilder.vertex(i3, this.y0, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
         bufferBuilder.vertex(i3, i11 + i10, 0.0D).uv(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
         bufferBuilder.vertex(i4, i11 + i10, 0.0D).uv(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
         bufferBuilder.vertex(i4, i11, 0.0D).uv(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
         bufferBuilder.vertex(i3, i11, 0.0D).uv(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
         bufferBuilder.vertex(i3, i11 + i10 - 1, 0.0D).uv(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
         bufferBuilder.vertex(i4 - 1, i11 + i10 - 1, 0.0D).uv(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
         bufferBuilder.vertex(i4 - 1, i11, 0.0D).uv(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
         bufferBuilder.vertex(i3, i11, 0.0D).uv(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
         tesselator.end();
      }

      this.renderDecorations(poseStack, i, i2);
      RenderSystem.enableTexture();
      RenderSystem.shadeModel(7424);
      RenderSystem.enableAlphaTest();
      RenderSystem.disableBlend();
   }

   protected void centerScrollOn(E entry) {
      this.setScrollAmount(this.children().indexOf(entry) * this.itemHeight + this.itemHeight / 2 - (this.y1 - this.y0) / 2);
   }

   protected void ensureVisible(E entry) {
      int i = this.getRowTop(this.children().indexOf(entry));
      int i2 = i - this.y0 - 4 - this.itemHeight;
      if (i2 < 0) {
         this.scroll(i2);
      }

      int i3 = this.y1 - i - this.itemHeight - this.itemHeight;
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
      return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
   }

   protected void updateScrollingState(double d, double d2, int i) {
      this.scrolling = i == 0 && d >= (double)this.getScrollbarPosition() && d < (double)(this.getScrollbarPosition() + 6);
   }

   protected int getScrollbarPosition() {
      return this.width / 2 + 124;
   }

   public boolean mouseClicked(double d, double d2, int i) {
      this.updateScrollingState(d, d2, i);
      if (!this.isMouseOver(d, d2)) {
         return false;
      } else {
         E entry = this.getEntryAtPosition(d, d2);
         if (entry != null) {
            if (entry.mouseClicked(d, d2, i)) {
               this.setFocused(entry);
               this.setDragging(true);
               return true;
            }
         } else if (i == 0) {
            this.clickedHeader((int)(d - (double)(this.x0 + this.width / 2 - this.getRowWidth() / 2)), (int)(d2 - (double)this.y0) + (int)this.getScrollAmount() - 4);
            return true;
         }

         return this.scrolling;
      }
   }

   public boolean mouseReleased(double d, double d2, int i) {
      if (this.getFocused() != null) {
         this.getFocused().mouseReleased(d, d2, i);
      }

      return false;
   }

   public boolean mouseDragged(double d, double d2, int i, double d3, double d4) {
      if (super.mouseDragged(d, d2, i, d3, d4)) {
         return true;
      } else if (i == 0 && this.scrolling) {
         if (d2 < (double)this.y0) {
            this.setScrollAmount(0.0D);
         } else if (d2 > (double)this.y1) {
            this.setScrollAmount(this.getMaxScroll());
         } else {
            double d5 = Math.max(1, this.getMaxScroll());
            int i2 = this.y1 - this.y0;
            int i3 = Mth.clamp((int)((float)(i2 * i2) / (float)this.getMaxPosition()), 32, i2 - 8);
            double d6 = Math.max(1.0D, d5 / (double)(i2 - i3));
            this.setScrollAmount(this.getScrollAmount() + d4 * d6);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean mouseScrolled(double d, double d2, double d3) {
      this.setScrollAmount(this.getScrollAmount() - d3 * (double)this.itemHeight / 2.0D);
      return true;
   }

   private E previous;
   @Override
   public void mouseMoved(double d, double d2) {
      E entry = this.getEntryAtPosition(d, d2);
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

   public boolean isMouseOver(double d, double d2) {
      return d2 >= (double)this.y0 && d2 <= (double)this.y1 && d >= (double)this.x0 && d <= (double)this.x1;
   }

   protected void renderList(PoseStack poseStack, int i, int i2, int i3, int i4, float f) {
      int i5 = this.getItemCount();
      for(int i6 = 0; i6 < i5; ++i6) {
         int i7 = this.getRowTop(i6);
         int i8 = this.getRowBottom(i6);
         if (i8 >= this.y0 && i7 <= this.y1) {
            E entry = this.getEntry(i6);
            entry.render(poseStack, this.width, this.height, f);
         }
      }

   }

   public int getRowLeft() {
      return this.x0 + this.width / 2 - this.getRowWidth() / 2 + 2;
   }

   public int getRowRight() {
      return this.getRowLeft() + this.getRowWidth();
   }

   protected int getRowTop(int i) {
      return this.y0 + 4 - (int)this.getScrollAmount() + i * this.itemHeight + this.headerHeight;
   }

   private int getRowBottom(int i) {
      return this.getRowTop(i) + this.itemHeight;
   }

   protected boolean isFocused() {
      return false;
   }

   protected E remove(int i) {
      return this.children.remove(i);
   }

   public enum SelectionDirection {
      UP,
      DOWN
   }
}