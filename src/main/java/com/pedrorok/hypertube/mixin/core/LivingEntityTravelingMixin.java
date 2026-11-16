package com.pedrorok.hypertube.mixin.core;

import com.pedrorok.hypertube.core.travel.TravelConstants;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.2 port of EntityTravelingMixin's hurt / isInvulnerableTo hooks: entities travelling in a tube take no damage.
 * Damage now goes through LivingEntity#hurtServer, which first checks LivingEntity#isInvulnerableTo(ServerLevel, ...)
 * (Player's override calls super).
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityTravelingMixin {

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void createHypertube$cancelHurt(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (((LivingEntity) (Object) this).getPersistentData().getBooleanOr(TravelConstants.TRAVEL_TAG, false)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void createHypertube$cancelInvulnerableTo(ServerLevel level, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (((LivingEntity) (Object) this).getPersistentData().getBooleanOr(TravelConstants.TRAVEL_TAG, false)) {
            cir.setReturnValue(true);
        }
    }
}
