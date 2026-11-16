package com.pedrorok.hypertube.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.client.TubePathOutline;
import com.pedrorok.hypertube.core.camera.CameraSmoothing;
import com.pedrorok.hypertube.core.camera.DetachedCameraController;
import com.pedrorok.hypertube.core.camera.DetachedPlayerDirController;
import com.pedrorok.hypertube.core.escape.TubeEscapeHandler;
import com.pedrorok.hypertube.core.placement.TubePlacement;
import com.pedrorok.hypertube.core.sound.TubeSoundManager;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.pedrorok.hypertube.core.travel.TravellerEntity;
import com.pedrorok.hypertube.core.travel.client.ClientTravelPathMover;
import com.pedrorok.hypertube.core.travel.client.ClientTravelPathRender;
import com.pedrorok.hypertube.utils.TubePulseRenderer;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.placement.PlacementClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.minecraft.util.context.ContextKey;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * @author Rok, Pedro Lucas nmm. Created on 23/04/2025
 * @project Create Hypertube
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = HypertubeMod.MOD_ID)
public class ClientEvents {

    private static long lastFrameTime = 0;

    @SubscribeEvent
    public static void onTickPre(ClientTickEvent.Pre event) {
        onTick(true);
    }

    @SubscribeEvent
    public static void onTickPost(ClientTickEvent.Post event) {
        onTick(false);
    }

    @SubscribeEvent
    public static void onRenderHighlight(ExtractBlockOutlineRenderStateEvent event) {
        TubePathOutline.renderTubeOutline(event);
    }

    private static void onTick(boolean isPreEvent) {
        if (!isGameActive()) return;

        if (isPreEvent) {
            TubeSoundManager.tickClientPlayerSounds();
            TubeEscapeHandler.onClientTick();
            return;
        }
        TubePlacement.clientTick();
        DetachedCameraController.tickCamera();
        ClientTravelPathMover.onClientTick();
    }

    @SubscribeEvent
    public static void renderFrame(RenderFrameEvent.Pre event) {
        DeltaTracker partialTick = event.getPartialTick();

        long currentTime = System.nanoTime();
        float deltaSeconds = lastFrameTime == 0 ? 1 / CameraSmoothing.REFERENCE_RATE : (currentTime - lastFrameTime) / 1_000_000_000f;
        lastFrameTime = currentTime;

        DetachedPlayerDirController.tickPlayer(deltaSeconds);

        ClientTravelPathMover.onRenderTick(partialTick);
    }

    @SubscribeEvent
    public static void afterRenderOverlayLayer(RenderGuiLayerEvent.Post event) {
        if (event.getName() == VanillaGuiLayers.CROSSHAIR) {
            ClientTravelPathRender.renderOverlay(event.getGuiGraphics(), AnimationTickHolder.getPartialTicksUI(event.getPartialTick()));
        }
        if (event.getName() == VanillaGuiLayers.HOTBAR) {
            TubeEscapeHandler.onRenderGuiOverlay(event.getGuiGraphics(), event.getPartialTick());
        }
    }

    // 26.2: world overlays (placement preview, tube pulses) are submitted with the frame's custom geometry
    @SubscribeEvent
    public static void onRenderWorld(SubmitCustomGeometryEvent event) {
        PoseStack ms = event.getPoseStack();
        Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;

        ms.pushPose();
        TubePlacement.drawCustomBlockSelection(ms, event.getSubmitNodeCollector(), camera);
        ms.popPose();

        TubePulseRenderer.onRenderLevelStage(ms, event.getSubmitNodeCollector(),
                Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks(), camera);
    }

    protected static boolean isGameActive() {
        return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
    }

    /**
     * 26.2 renders living entities from a render state, not the entity. Hypertube's travel pose needs the entity (persistent
     * data, path data), so the entity is attached to its render state while it travels.
     */
    public static final ContextKey<LivingEntity> TRAVELLING_ENTITY = new ContextKey<>(HypertubeMod.of("travelling_entity"));

    @SubscribeEvent
    public static void onRenderEntity(RenderLivingEvent.Pre<?, ?, ?> event) {
        LivingEntity entity = event.getRenderState().getRenderData(TRAVELLING_ENTITY);
        if (entity == null || !entity.getPersistentData().getBooleanOr(TravelConstants.TRAVEL_TAG, false)) return;
        PoseStack poseStack = event.getPoseStack();
        TravellerEntity travellerEntity = TravelConstants.Client.ENTITIES_RENDER
                .get(entity.getType());
        if (travellerEntity == null) travellerEntity = TravellerEntity.ofAny(0.5f);
        travellerEntity
                .renderEntityOnTube()
                .accept(entity, poseStack);
    }

    @SubscribeEvent
    public static void onRenderEntityPost(RenderLivingEvent.Post<?, ?, ?> event) {
        LivingEntity entity = event.getRenderState().getRenderData(TRAVELLING_ENTITY);
        if (entity == null || !entity.getPersistentData().getBooleanOr(TravelConstants.TRAVEL_TAG, false)) return;
        event.getPoseStack().popPose();
    }
}
