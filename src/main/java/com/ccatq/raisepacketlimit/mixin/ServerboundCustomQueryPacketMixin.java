package com.ccatq.raisepacketlimit.mixin;

import com.ccatq.raisepacketlimit.Config;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Raises login-query payload limits received by the server. */
@Mixin(ServerboundCustomQueryAnswerPacket.class)
public abstract class ServerboundCustomQueryPacketMixin {
    @ModifyConstant(method = "readUnknownPayload", constant = @Constant(intValue = 1_048_576), require = 0)
    private static int raisepacketlimit$payloadLimit(int original) {
        return Config.maxCustomPayloadBytes();
    }
}
