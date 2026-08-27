package com.ccatq.raisepacketlimit.mixin;

import com.ccatq.raisepacketlimit.Config;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Extends the login timeout for clients receiving large registries or payloads. */
@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin {
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 600), require = 0)
    private static int raisepacketlimit$loginTimeout(int original) {
        return Config.connectionTimeoutSeconds() * 20;
    }
}
