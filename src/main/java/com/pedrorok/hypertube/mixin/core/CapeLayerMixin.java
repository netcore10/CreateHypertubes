package com.pedrorok.hypertube.mixin.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pedrorok.hypertube.events.ClientEvents;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.2 moved the cape out of PlayerModel (where PlayerModelMixin used to hide its "cloak" part) into CapeLayer:
 * don't draw the cape while the player travels in a tube.
 */
@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin {
    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V", at = @At("HEAD"), cancellable = true)
    private void createHypertube$hideCapeWhileTravelling(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot, CallbackInfo ci) {
        if (state.getRenderData(ClientEvents.TRAVELLING_ENTITY) != null) {
            ci.cancel();
        }
    }
}
