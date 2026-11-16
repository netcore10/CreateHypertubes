package com.pedrorok.hypertube.utils;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.*;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.ponder.enums.PonderConfig;
import com.zurrtum.create.client.ponder.enums.PonderGuiTextures;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;

/**
 * @author Rok, Pedro Lucas nmm. Created on 19/11/2025
 * @project Create Hypertube
 */
public class RenderUtils {

    public static void rotateAroundCenterVertical(SuperByteBuffer buffer, int degreesRotated) {
        buffer.translate(0.5f, 0.5f, 0.5f);
        buffer.rotateY((float) Math.toRadians(degreesRotated));
        buffer.translate(-0.5f, -0.5f, -0.5f);
    }

    public static void rotateAroundCenterHorizontalZ(SuperByteBuffer buffer, int degreesRotated) {
        buffer.translate(0.5f, 0.5f, 0.5f);
        buffer.rotateZ((float) Math.toRadians(degreesRotated));
        buffer.translate(-0.5f, -0.5f, -0.5f);
    }

    public static void rotateToFace(SuperByteBuffer model, Direction tubeFacing, Direction attachmentDirection, boolean isTubeVertical) {

        switch (attachmentDirection) {
            case NORTH -> rotateAroundCenterVertical(model, -90);
            case EAST -> rotateAroundCenterVertical(model, 180);
            case SOUTH -> rotateAroundCenterVertical(model, 90);
            case WEST -> rotateAroundCenterVertical(model, 0);
            case UP -> rotateAroundCenterHorizontalZ(model, -90);
            case DOWN -> rotateAroundCenterHorizontalZ(model, 90);
        }

        if (!isTubeVertical) {
            if ((attachmentDirection == Direction.UP || attachmentDirection == Direction.DOWN) && tubeFacing != Direction.NORTH && tubeFacing != Direction.SOUTH) return;
            model.rotateXCenteredDegrees(90);
        }
    }

    /**
     * Create's placement direction arrow, tinted. 26.2 draws GUI through render states, so this mirrors Create Fly's
     * PlacementClient#textured (which is white only) with the original tint.
     */
    public static void directionArrow(GuiGraphicsExtractor graphics, float centerX, float centerY, float alpha, int color, float snappedAngle) {
        Matrix3x2fStack ms = graphics.pose();
        ms.pushMatrix();
        ms.translate(centerX, centerY);
        float scale = PonderConfig.client().indicatorScale.get().floatValue() * .75f;
        ms.scale(scale, scale);
        ms.scale(12, 12);

        float index = snappedAngle / 22.5f;
        float tex_size = 16f / 256f;

        TextureSetup texture = PonderGuiTextures.PLACEMENT_INDICATOR_SHEET.bind();
        int size = (int) (36 * scale);
        Matrix3x2f pose = new Matrix3x2f(ms);
        graphics.submitGuiElementRenderState(new TintedArrowRenderState(pose, alpha, color, 0, index * tex_size, 1f, tex_size,
                texture, new ScreenRectangle(0, 0, size, size).transformMaxBounds(pose)));
        ms.popMatrix();
    }

    private record TintedArrowRenderState(Matrix3x2f pose, float alpha, int color, float tx, float ty, float tw, float th,
                                          TextureSetup textureSetup, ScreenRectangle bounds) implements GuiElementRenderState {
        @Override
        public RenderPipeline pipeline() {
            return RenderPipelines.GUI_TEXTURED;
        }

        @Override
        public void buildVertices(VertexConsumer consumer) {
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            consumer.addVertexWith2DPose(pose, -1, -1).setColor(r, g, b, alpha).setUv(tx, ty);
            consumer.addVertexWith2DPose(pose, -1, 1).setColor(r, g, b, alpha).setUv(tx, ty + th);
            consumer.addVertexWith2DPose(pose, 1, 1).setColor(1f, g, b, alpha).setUv(tx + tw, ty + th);
            consumer.addVertexWith2DPose(pose, 1, -1).setColor(r, g, b, alpha).setUv(tx + tw, ty);
        }

        @Override
        public @Nullable ScreenRectangle scissorArea() {
            return null;
        }
    }
}