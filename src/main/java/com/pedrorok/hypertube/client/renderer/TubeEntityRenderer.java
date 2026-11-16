package com.pedrorok.hypertube.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pedrorok.hypertube.blocks.HyperAcceleratorBlock;
import com.pedrorok.hypertube.blocks.blockentities.HyperAcceleratorBlockEntity;
import com.pedrorok.hypertube.blocks.blockentities.HyperEntranceBlockEntity;
import com.pedrorok.hypertube.blocks.blockentities.HyperJunctionBlockEntity;
import com.pedrorok.hypertube.blocks.blockentities.parent.ActionTubeBlockEntity;
import com.pedrorok.hypertube.blocks.blockentities.parent.TubeBlockEntity;
import com.pedrorok.hypertube.client.BezierTextureRenderer;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.registry.ModPartialModels;
import com.pedrorok.hypertube.utils.RenderUtils;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 26.2 / Create Fly port of Hypertube's four block entity renderers (HypertubeBlockEntityRenderer, AcceleratorEntityRenderer,
 * EntranceBlockEntityRenderer, JunctionEntityRenderer), which all drew the same parts:
 * <ul>
 *     <li>the curved tube connections (BezierTextureRenderer),</li>
 *     <li>tube attachments (redstone detector, scanner) on entrances, accelerators and junctions,</li>
 *     <li>the spinning cogwheel on entrances and accelerators.</li>
 * </ul>
 * 26.2 renders in two steps: extract what's needed from the block entity into a render state, then submit it.
 */
public class TubeEntityRenderer<T extends TubeBlockEntity> implements BlockEntityRenderer<T, TubeEntityRenderer.TubeRenderState> {

    private final BezierTextureRenderer tubeRenderer = BezierTextureRenderer.get();

    public TubeEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static class TubeRenderState extends BlockEntityRenderState {
        final List<BezierConnection> connections = new ArrayList<>();
        final List<SuperByteBufferRenderState> attachments = new ArrayList<>();
        @Nullable SuperByteBufferRenderState cogwheel;
    }

    @Override
    public TubeRenderState createRenderState() {
        return new TubeRenderState();
    }

    @Override
    public void extractRenderState(T be, TubeRenderState state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.connections.clear();
        state.attachments.clear();
        state.cogwheel = null;

        for (IConnection connection : be.getConnections()) {
            if (connection instanceof BezierConnection bezier) {
                state.connections.add(bezier);
            }
        }

        BlockState blockState = be.getBlockState();
        if (!blockState.hasProperty(HyperAcceleratorBlock.FACING)) return;
        Direction facing = blockState.getValue(HyperAcceleratorBlock.FACING);
        boolean hasCogwheel = be instanceof HyperEntranceBlockEntity || be instanceof HyperAcceleratorBlockEntity;

        if (be instanceof ActionTubeBlockEntity actionTube) {
            // Junctions always attach as if horizontal, like the original JunctionEntityRenderer
            boolean isTubeOnVertical = !(be instanceof HyperJunctionBlockEntity) && facing.getAxis().isVertical();
            actionTube.getTubeAttachments().forEach((direct, attachment) -> {
                SuperByteBuffer model = CachedBuffers.partial(attachment.getPartialModel(blockState, actionTube, direct), blockState);
                RenderUtils.rotateToFace(model, facing, direct.getOpposite(), isTubeOnVertical);
                model.light(state.lightCoords);
                state.attachments.add(model.extractRenderState());
            });
        }

        if (hasCogwheel && be instanceof com.zurrtum.create.content.kinetics.base.KineticBlockEntity kinetic) {
            SuperByteBuffer cogwheel = CachedBuffers.partialFacingVertical(ModPartialModels.COGWHEEL_HOLE, blockState, facing);
            float angle = KineticBlockEntityRenderer.getAngleForBe(kinetic, be.getBlockPos(), facing.getAxis());
            Direction.Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(kinetic);
            // Create's kineticRotationTransform: light, then spin around the rotation axis
            cogwheel.light(state.lightCoords)
                .rotateCentered(angle, Direction.get(Direction.AxisDirection.POSITIVE, axis));
            state.cogwheel = cogwheel.extractRenderState();
        }
    }

    @Override
    public void submit(TubeRenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector collector, @NotNull CameraRenderState camera) {
        for (SuperByteBufferRenderState attachment : state.attachments) {
            attachment.submit(RenderTypes.translucentMovingBlock(), poseStack, collector);
        }
        if (state.cogwheel != null) {
            state.cogwheel.submit(RenderTypes.solidMovingBlock(), poseStack, collector);
        }
        for (BezierConnection connection : state.connections) {
            tubeRenderer.renderBezierConnection(state.blockPos, connection, poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY);
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public boolean shouldRender(@NotNull T blockEntity, @NotNull Vec3 pos) {
        return true;
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(@NotNull T blockEntity) {
        return AABB.INFINITE;
    }
}
