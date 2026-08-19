package com.ccatq.raisepacketlimit.mixin;

import net.minecraft.network.Varint21FrameDecoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Allows the inbound frame length varint to be wider than 21 bits.
 * <p>
 * Vanilla 1.21.1 reads the inbound frame length with a 3-byte varint
 * ({@code CorruptedFrameException("length wider than 21-bit")} otherwise) and
 * sizes its helper buffer to 3 bytes. Both limits are raised to 5 bytes so
 * frames larger than 2 MiB (e.g. the AE2 container_set_content case) can be
 * received. 5 bytes covers up to 2^35-1 bytes, far beyond the config ceiling.
 */
@Mixin(value = Varint21FrameDecoder.class, priority = 1100)
public abstract class Varint21FrameDecoderMixin {
    /** Maximum varint bytes accepted for an inbound frame length. */
    private static final int MAX_VARINT_BYTES = 5;

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 3))
    private int raisepacketlimit$largerHelperBuffer(int original) {
        return MAX_VARINT_BYTES;
    }

    // copyVarint is a private STATIC method, so this callback must be static too.
    @ModifyConstant(method = "copyVarint", constant = @Constant(intValue = 3))
    private static int raisepacketlimit$widerFrameVarint(int original) {
        return MAX_VARINT_BYTES;
    }
}