package com.ccatq.raisepacketlimit.mixin;

import com.ccatq.raisepacketlimit.Config;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Raises clientbound custom-payload limits. */
@Mixin(ClientboundCustomPayloadPacket.class)
public abstract class ClientboundCustomPayloadPacketMixin {
    @ModifyConstant(method = "lambda$static$0", constant = @Constant(intValue = 1_048_576), require = 0)
    private static int raisepacketlimit$payloadLimit(int original) {
        return Config.maxCustomPayloadBytes();
    }

    @ModifyConstant(method = "lambda$static$2", constant = @Constant(intValue = 1_048_576), require = 0)
    private static int raisepacketlimit$payloadLimit2(int original) {
        return Config.maxCustomPayloadBytes();
    }
}
