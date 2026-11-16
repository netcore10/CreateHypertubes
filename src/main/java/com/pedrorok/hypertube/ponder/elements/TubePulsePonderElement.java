package com.pedrorok.hypertube.ponder.elements;

import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.utils.TubePulseEffect;
import com.pedrorok.hypertube.utils.TubePulseRenderer;
import com.zurrtum.create.client.ponder.api.element.PonderSceneElement;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.element.PonderElementBase;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. 29/07/2026
 * @project Create Hypertube
 */
public class TubePulsePonderElement extends PonderElementBase implements PonderSceneElement {

    // 1.21.1 used a custom translucent line type; 26.2 has it built in
    private static final RenderType LINES = RenderTypes.linesTranslucent();

    private static final int RING_COUNT = 4;
    private static final float RING_SPACING = 0.08f;
    private static final float SPEED = 0.2f;
    private static final float FADE_OUT_DISTANCE = 5;
    private static final float RING_RADIUS = 0.72f;
    private static final int SPAWN_INTERVAL = 10;

    private final BlockPos origin;
    private final List<Vec3> relativePoints;
    private final int color;
    private final List<TubePulseEffect> effects = new ArrayList<>();

    private int remainingTicks;
    private int spawnCooldown;

    private TubePulsePonderElement(BlockPos origin, List<Vec3> relativePoints, int color, int durationTicks) {
        this.origin = origin;
        this.relativePoints = relativePoints;
        this.color = color;
        this.remainingTicks = durationTicks;
        setVisible(true);
    }

    @Nullable
    public static TubePulsePonderElement of(BezierConnection connection, BlockPos referencePos, int color, int durationTicks) {
        BlockPos origin = connection.getFromPos().pos();
        List<Vec3> points = connection.getRelativeBezierPoints(origin);
        if (points.size() < 2) return null;
        if (connection.isInverted(referencePos)) {
            points = points.reversed();
        }
        return new TubePulsePonderElement(origin, points, color, durationTicks);
    }

    @Override
    public void tick(PonderScene scene) {
        if (remainingTicks > 0) {
            remainingTicks--;
            if (spawnCooldown <= 0) {
                spawnCooldown = SPAWN_INTERVAL;
                effects.add(new TubePulseEffect(origin, relativePoints, RING_COUNT, RING_SPACING, SPEED, color,
                        0, FADE_OUT_DISTANCE, RING_RADIUS));
            }
            spawnCooldown--;
        }
        effects.forEach(effect -> effect.tick(1));
        effects.removeIf(TubePulseEffect::isFinished);
        if (remainingTicks <= 0 && effects.isEmpty()) {
            setVisible(false);
        }
    }

    @Override
    public void reset(PonderScene scene) {
        effects.clear();
        setVisible(false);
    }

    @Override
    public void renderFirst(BlockEntityRenderDispatcher blockEntities, ModelManager models, PonderLevel level, SubmitNodeCollector collector,
                            CameraRenderState camera, PoseStack poseStack, float partialTicks) {
    }

    @Override
    public void renderLast(EntityRenderDispatcher entities, ItemModelResolver items, PonderLevel level, SubmitNodeCollector collector,
                           CameraRenderState camera, PoseStack poseStack, float partialTicks) {
        if (effects.isEmpty()) return;

        Vec3 renderOrigin = new Vec3(origin.getX(), origin.getY(), origin.getZ());
        List<TubePulseEffect> active = effects.stream().filter(effect -> !effect.isFinished()).toList();
        collector.submitCustomGeometry(poseStack, LINES, (pose, consumer) -> {
            PoseStack local = new PoseStack();
            local.last().pose().set(pose.pose());
            local.last().normal().set(pose.normal());
            for (TubePulseEffect effect : active) {
                TubePulseRenderer.renderEffectAt(effect, local, consumer, renderOrigin, partialTicks);
            }
        });
    }
}
