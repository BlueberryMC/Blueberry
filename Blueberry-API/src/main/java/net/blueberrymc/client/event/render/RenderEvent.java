package net.blueberrymc.client.event.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.event.Event;
import net.blueberrymc.common.bml.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class RenderEvent extends Event {
    public RenderEvent() {
        super();
    }

    public RenderEvent(boolean async) {
        super(async);
    }

    public static class Pre extends RenderEvent {
        private static final HandlerList handlerList = new HandlerList();
        private final PoseStack poseStack;
        private final RenderType renderType;

        public Pre(@NotNull PoseStack poseStack, @NotNull RenderType renderType) {
            super(!Blueberry.getUtil().isOnGameThread());
            this.poseStack = poseStack;
            this.renderType = renderType;
        }

        @NotNull
        public PoseStack getPoseStack() {
            return poseStack;
        }

        @NotNull
        public RenderType getRenderType() {
            return renderType;
        }

        @NotNull
        public static HandlerList getHandlerList() {
            return handlerList;
        }
    }

    public static class Post extends RenderEvent {
        private static final HandlerList handlerList = new HandlerList();
        private final PoseStack poseStack;
        private final RenderType renderType;

        public Post(@NotNull PoseStack poseStack, @NotNull RenderType renderType) {
            super(!Blueberry.getUtil().isOnGameThread());
            this.poseStack = poseStack;
            this.renderType = renderType;
        }

        @NotNull
        public PoseStack getPoseStack() {
            return poseStack;
        }

        @NotNull
        public RenderType getRenderType() {
            return renderType;
        }

        @NotNull
        public static HandlerList getHandlerList() {
            return handlerList;
        }
    }

    public enum RenderType {

    }
}
