package net.zepalesque.redux.event.hook;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.zepalesque.redux.capability.arrow.SubzeroArrow;
import net.zepalesque.redux.client.particle.ReduxParticleTypes;

public class WeaponHooks {
    public static void subzeroArrowHit(HitResult result, Projectile projectile) {
        if (!(projectile instanceof AbstractArrow arrow)) return;
        if (!(arrow.level() instanceof ServerLevel serverLevel)) return;

        SubzeroArrow.get(arrow).ifPresent(subzeroArrow -> {
            if (!subzeroArrow.isSubzeroArrow()) {
                return;
            }

            if (result instanceof EntityHitResult entityHitResult) {
                Entity impactedEntity = entityHitResult.getEntity();

                if (impactedEntity.getType() == EntityType.ENDERMAN) {
                    return;
                }

                if (subzeroArrow.getSlownessTime() > 0
                        && impactedEntity instanceof LivingEntity living
                        && living != arrow.getOwner()) {
                    living.addEffect(new MobEffectInstance(
                            MobEffects.MOVEMENT_SLOWDOWN,
                            subzeroArrow.getSlownessTime(),
                            2
                    ));
                }
            }

            serverLevel.sendParticles(
                    ReduxParticleTypes.ICE_SHARD.get(),
                    arrow.getX(),
                    arrow.getY(),
                    arrow.getZ(),
                    10,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        });
    }
}
