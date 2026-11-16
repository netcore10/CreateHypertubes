package com.pedrorok.hypertube.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.pedrorok.hypertube.blocks.blockentities.TubePathBlockEntity;
import com.pedrorok.hypertube.core.collision.TubeCollision;
import com.pedrorok.hypertube.core.collision.TubeCollision.Polyline;
import com.pedrorok.hypertube.core.collision.TubeFiller;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;

import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm.
 * @project Create Hypertube
 */
public final class TubePathOutline {

    private static final float STEP = 0.5f;

    private static final float R = 0f;
    private static final float G = 0f;
    private static final float B = 0f;
    private static final float A = 0.4f;

    private TubePathOutline() {
    }

    // 26.2: outlines are extracted, then drawn through a custom outline renderer (returning true replaces vanilla's)
    public static void renderTubeOutline(ExtractBlockOutlineRenderStateEvent event) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        BlockPos hit = event.getBlockPos();
        if (!(level.getBlockEntity(hit) instanceof TubePathBlockEntity)) {
            return;
        }
        BezierConnection bezier = TubeFiller.ownerBezier(level, hit);
        if (bezier == null) {
            return;
        }

        List<Vec3> points = bezier.getBezierPoints(level, bezier.getFromPos().pos());
        if (points.size() < 2) {
            return;
        }
        event.addCustomRenderer((outline, collector, poseStack, levelState) -> {
            Vec3 camera = levelState.cameraRenderState.pos;
            float width = Minecraft.getInstance().getWindow().getAppropriateLineWidth();
            collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, consumer) -> draw(consumer, pose, points, camera, width));
            return true;
        });
    }

    private static void draw(VertexConsumer consumer, PoseStack.Pose pose, List<Vec3> points, Vec3 camera, float width) {

        Polyline path = Polyline.of(points);
        float length = (float) path.length();
        int steps = Math.max(2, Math.round(length / STEP));

        Vec3[] previous = null;
        for (int i = 0; i <= steps; i++) {
            Vec3[] corners = TubeCollision.crossSectionCorners(path, (double) length * i / steps);
            for (int c = 0; c < 4; c++) {
                corners[c] = corners[c].subtract(camera);
            }
            if (i == 0 || i == steps) {
                loop(consumer, pose, corners, width);
            }
            if (previous != null) {
                for (int c = 0; c < 4; c++) {
                    line(consumer, pose, previous[c], corners[c], width);
                }
            }
            previous = corners;
        }
    }

    private static void loop(VertexConsumer consumer, PoseStack.Pose pose, Vec3[] corners, float width) {
        for (int c = 0; c < 4; c++) {
            line(consumer, pose, corners[c], corners[(c + 1) % 4], width);
        }
    }

    private static void line(VertexConsumer consumer, PoseStack.Pose pose, Vec3 a, Vec3 b, float width) {
        Vec3 normal = b.subtract(a);
        if (normal.lengthSqr() < 1.0e-9) {
            return;
        }
        normal = normal.normalize();
        consumer.addVertex(pose.pose(), (float) a.x, (float) a.y, (float) a.z)
                .setColor(R, G, B, A)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z)
                .setLineWidth(width);
        consumer.addVertex(pose.pose(), (float) b.x, (float) b.y, (float) b.z)
                .setColor(R, G, B, A)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z)
                .setLineWidth(width);
    }
}
