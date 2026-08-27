package com.ccatq.raisepacketlimit.mixin;

import com.ccatq.raisepacketlimit.Config;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Extends the server's keep-alive interval for slow transfers. */
@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin {
    @ModifyConstant(method = "keepConnectionAlive", constant = @Constant(longValue = 15_000L), require = 0)
    private long raisepacketlimit$keepAliveInterval(long original) {
        return Config.connectionTimeoutSeconds() * 1_000L;
    }
}
