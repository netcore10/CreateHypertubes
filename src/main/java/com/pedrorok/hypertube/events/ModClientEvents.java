package com.pedrorok.hypertube.events;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.client.particles.SuctionParticle;
import com.pedrorok.hypertube.client.renderer.TubeEntityRenderer;
import com.pedrorok.hypertube.ponder.HypertubesPonderPlugin;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import com.pedrorok.hypertube.registry.ModParticles;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import com.pedrorok.hypertube.core.travel.TravelConstants;

/**
 * 26.2 / Create Fly port: block render layers come from texture transparency now (no ItemBlockRenderTypes), and block
 * entity renderers are registered here instead of through Registrate.
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = HypertubeMod.MOD_ID)
public class ModClientEvents {

    @SubscribeEvent
    public static void forgeLoad(FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new HypertubesPonderPlugin());
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.HYPERTUBE.get(), TubeEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HYPERTUBE_ENTRANCE.get(), TubeEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HYPER_ACCELERATOR.get(), TubeEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HYPER_JUNCTION.get(), TubeEntityRenderer::new);
    }

    /** Attaches travelling entities to their render state for ClientEvents' travel pose (26.2 renders from render states). */
    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier((Class) LivingEntityRenderer.class, (entity, state) -> {
            LivingEntity living = (LivingEntity) entity;
            boolean travelling = living.getPersistentData().getBooleanOr(TravelConstants.TRAVEL_TAG, false);
            ((EntityRenderState) state).setRenderData(ClientEvents.TRAVELLING_ENTITY, travelling ? living : null);
        });
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.SUCTION_PARTICLE.get(), SuctionParticle.Provider::new);
    }
}
