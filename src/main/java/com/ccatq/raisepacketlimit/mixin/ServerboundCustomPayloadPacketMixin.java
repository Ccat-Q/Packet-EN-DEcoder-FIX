package com.ccatq.raisepacketlimit.mixin;

import com.ccatq.raisepacketlimit.Config;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Raises serverbound custom-payload limits. */
@Mixin(ServerboundCustomPayloadPacket.class)
public abstract class ServerboundCustomPayloadPacketMixin {
    @ModifyConstant(method = "lambda$static$0", constant = @Constant(intValue = 32_767), require = 0)
    private static int raisepacketlimit$payloadLimit(int original) {
        return Config.maxCustomPayloadBytes();
    }

    @ModifyConstant(method = "lambda$static$2", constant = @Constant(intValue = 32_767), require = 0)
    private static int raisepacketlimit$payloadLimit2(int original) {
        return Config.maxCustomPayloadBytes();
    }
}
