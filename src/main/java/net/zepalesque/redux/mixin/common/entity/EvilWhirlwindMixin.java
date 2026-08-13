package net.zepalesque.redux.mixin.common.entity;

import com.aetherteam.aether.entity.monster.EvilWhirlwind;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.zepalesque.redux.client.particle.ReduxParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EvilWhirlwind.class)
public abstract class EvilWhirlwindMixin {

    @ModifyArg(
            method = "spawnParticles()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",
                    remap = true
            ),
            index = 1,
            remap = false
    )
    private double redux$addLightningParticle(double originalX, @Local(ordinal = 0) int iteration) {
        // The targeted addParticle invocation is inside Aether's `for (int i = 0; i < 3; i++)` loop.
        //Only add Redux lightning once per spawnParticles() invocation.
        if (iteration != 0) return originalX;

        EvilWhirlwind self = (EvilWhirlwind) (Object) this;

        if (!self.level().isClientSide() || self.tickCount <= 10 || self.deathTime >= 10) return originalX;

        RandomSource random = self.getRandom();

        if (random.nextFloat() >= 0.65F) return originalX;

        final double minRadius = 4.5D / 16.0D;
        final double maxRadius = 22.5D / 16.0D;

        double randomX = random.nextDouble();
        double randomY = random.nextDouble();
        double randomZ = random.nextDouble();

        double radius = Mth.lerp(randomY, minRadius, maxRadius);
        double height = Mth.lerp(randomY,0.5D,3.5D);

        double x = self.getX() - Mth.lerp(randomX, -radius, radius);
        double y = self.getY() + height;
        double z = self.getZ() + Mth.lerp(randomZ, -radius, radius);

        self.level().addParticle(ReduxParticleTypes.WHIRLWIND_LIGHTNING.get(), x, y, z, 0.0D, 0.0D, 0.0D);

        return originalX;
    }
}
