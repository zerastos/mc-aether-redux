
package net.zepalesque.redux.network.packet;

import com.aetherteam.nitrogen.network.BasePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.zepalesque.redux.capability.player.ReduxPlayer;
import net.zepalesque.redux.util.player.AbilityUtil;

public record ShootFireballPacket() implements BasePacket {

    public void encode(FriendlyByteBuf buf) {}

    public static ShootFireballPacket decode(FriendlyByteBuf buf) {
        return new ShootFireballPacket();
    }

    public void execute(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        ReduxPlayer.get(serverPlayer).ifPresent(reduxPlayer -> {
            if (reduxPlayer.canShootFireball()) {
                reduxPlayer.fireballSetup();
                AbilityUtil.shootFireballs(serverPlayer);
            }
        });
    }
}
