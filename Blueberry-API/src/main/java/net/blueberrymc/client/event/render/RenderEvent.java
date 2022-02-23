package net.blueberrymc.client.event.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when the client renders something.
 */
public abstract class RenderEvent extends Event {
    protected RenderEvent() {
        super();
    }

    protected RenderEvent(boolean async) {
        super(async);
    }

    /**
     * Fired before the client renders something.
     * @deprecated unimplemented event
     */
    @Deprecated
    public static abstract class Pre extends RenderEvent {
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
    }

    /**
     * Fired after the client renders something.
     * @deprecated unimplemented event
     */
    @Deprecated
    public static abstract class Post extends RenderEvent {
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
    }

    public enum RenderType {

    }
}
