
package net.zepalesque.redux.network.packet;

import com.aetherteam.nitrogen.network.BasePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.zepalesque.redux.advancement.trigger.DoubleJumpTrigger;
import net.zepalesque.redux.capability.player.ReduxPlayer;

public record DoubleJumpPacket() implements BasePacket {

    public void encode(FriendlyByteBuf buf) {}

    public static DoubleJumpPacket decode(FriendlyByteBuf buf) {
        return new DoubleJumpPacket();
    }

    public void execute(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        ReduxPlayer.get(serverPlayer).ifPresent(reduxPlayer -> {
            if (reduxPlayer.doubleJump()) {
                DoubleJumpTrigger.INSTANCE.trigger(serverPlayer);
            }
        });
    }
}
