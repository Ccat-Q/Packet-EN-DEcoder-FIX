package com.ccatq.raisepacketlimit.mixin;

import com.ccatq.raisepacketlimit.Config;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Extends the in-game no-response timeout. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @ModifyConstant(method = "tick", constant = @Constant(longValue = 60L), require = 0)
    private long raisepacketlimit$gameTimeout(long original) {
        return Config.connectionTimeoutSeconds();
    }
}
