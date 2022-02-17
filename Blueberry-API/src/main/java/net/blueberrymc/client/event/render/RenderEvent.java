package net.blueberrymc.client.event.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class RenderEvent extends Event {
    protected RenderEvent() {
        super();
    }

    protected RenderEvent(boolean async) {
        super(async);
    }

    /**
     * @deprecated this event is never fired
     */
    @Deprecated
    public static class Pre extends RenderEvent {
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
     * @deprecated this event is never fired
     */
    @Deprecated
    public static class Post extends RenderEvent {
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
