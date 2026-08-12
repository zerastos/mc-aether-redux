
package net.zepalesque.redux.event.listener;

import com.aetherteam.aether.entity.monster.Cockatrice;
import com.aetherteam.aether.entity.monster.Swet;
import com.aetherteam.aether.item.EquipmentUtil;
import com.aetherteam.nitrogen.capability.INBTSynchable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zepalesque.redux.Redux;
import net.zepalesque.redux.capability.arrow.SubzeroArrow;
import net.zepalesque.redux.capability.cockatrice.CockatriceExtension;
import net.zepalesque.redux.capability.living.VampireAmulet;
import net.zepalesque.redux.capability.player.ReduxPlayer;
import net.zepalesque.redux.config.ReduxConfig;
import net.zepalesque.redux.config.enums.QuicksoilSetting;
import net.zepalesque.redux.effect.ReduxEffects;
import net.zepalesque.redux.event.hook.MobHooks;
import net.zepalesque.redux.event.hook.SwetHooks;
import net.zepalesque.redux.item.ReduxItems;
import net.zepalesque.redux.misc.ReduxTags;
import net.zepalesque.redux.network.ReduxPacketHandler;
import net.zepalesque.redux.network.packet.SubzeroArrowPacket;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = Redux.MODID)
public class MobListener {


    @SubscribeEvent
    public static void modifyAI(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Cockatrice cockatrice && ReduxConfig.COMMON.improved_cockatrice_behavior.get()) {
            MobHooks.modifyCockatriceAI(cockatrice);
        }
        if (event.getEntity() instanceof Swet swet && ReduxConfig.COMMON.pl_swet_behavior.get()) {
            SwetHooks.modifySwetAI(swet);
        }
    }



    @SubscribeEvent
    public static void livingUpdate(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        MobHooks.updateCapabilities(entity);
        if (entity instanceof Swet swet) {
            SwetHooks.swetTick(swet);
        }
    }

    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (event.getTarget() instanceof Cockatrice cockatrice) {
            CockatriceExtension.get(cockatrice).ifPresent(extension ->
                    extension.setSynched(INBTSynchable.Direction.PLAYER, "shooting", extension.isShooting(), player)
            );
        } else if (event.getTarget() instanceof AbstractArrow arrow) {
            SubzeroArrow.get(arrow).ifPresent(subzeroArrow -> {
                if (subzeroArrow.isSubzeroArrow()) {
                    ReduxPacketHandler.sendToPlayer(new SubzeroArrowPacket(arrow.getId(), true), player);
                }
            });
        }
    }

    @SubscribeEvent
    public static void attack(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity entity && EquipmentUtil.hasCurio(entity, ReduxItems.VAMPIRE_AMULET.get())) {
            VampireAmulet.get(entity).ifPresent(vampireAmulet ->
            {
                if (vampireAmulet.canUseAbility() && !vampireAmulet.getMob().level().isDay()) {

                    float life = getLifeSteal(event.getAmount());
                    vampireAmulet.getMob().heal(life);
                }
            });
        }

        if (EquipmentUtil.hasCurio(event.getEntity(), ReduxItems.VAMPIRE_AMULET.get()))
        {
            if (event.getEntity().level().isDay()) {
                float hurt = event.getAmount() * 1.25F;
                event.setAmount(hurt);
            }
        }
    }

    @SubscribeEvent
    public static void adrenalineDeplete(MobEffectEvent.Expired event) {
        if (event.getEffectInstance().getEffect() == ReduxEffects.ADRENALINE_RUSH.get()) {
            event.getEntity().addEffect(new MobEffectInstance(ReduxEffects.ADRENAL_FATIGUE.get(), 600, 1, false, false, true));
            if (event.getEntity() instanceof Player player) {
                ReduxPlayer.get(player).ifPresent(redux -> redux.getAdrenalineModule().beginCooldown());
            }
        }
    }

    private static float getLifeSteal(float damage) {
        return (damage <= 0F ? 0F : damage <= 20F ? (0.5F * damage) - (0.01F * damage * damage) : (damage * 0.1F) + 4F) * 0.5F;
    }



    @SubscribeEvent
    public static void movementHandling(LivingEvent.LivingTickEvent event) {
        if (ReduxConfig.COMMON.quicksoil_movement_system.get() == QuicksoilSetting.highlands) {
            final LivingEntity entity = event.getEntity();
            if (!entity.level().isLoaded(entity.getBlockPosBelowThatAffectsMyMovement())) { return; }
            if (!entity.onGround()) { return; }
            if (entity.isInWater()) { return; }
            if (Math.abs(entity.getDeltaMovement().x + entity.getDeltaMovement().y + entity.getDeltaMovement().z) < 0.001D) { return; }
            if (entity instanceof Player player && player.isSpectator())  { return; }

            if (entity.level().getBlockState(entity.getBlockPosBelowThatAffectsMyMovement()).is(ReduxTags.Blocks.QUICKSOIL_BEHAVIOR)) {
                MobHooks.modifyEntityQuicksoil(entity);
            }
        }
    }


}

