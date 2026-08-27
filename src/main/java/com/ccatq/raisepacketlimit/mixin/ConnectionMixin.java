package com.ccatq.raisepacketlimit.mixin;

import com.ccatq.raisepacketlimit.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Extends Netty's initial read timeout for slow large-packet transfers. */
@Mixin(targets = "net.minecraft.network.Connection$1")
public abstract class ConnectionMixin {
    @ModifyConstant(method = "initChannel", constant = @Constant(intValue = 30), require = 0)
    private int raisepacketlimit$initialReadTimeout(int original) {
        return Config.connectionTimeoutSeconds();
    }
}
