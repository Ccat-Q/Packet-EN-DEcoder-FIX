package com.ccatq.raisepacketlimit.mixin;

import net.minecraft.network.Varint21LengthFieldPrepender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Allows the outbound length prefix to use more than 3 varint bytes.
 * <p>
 * Vanilla 1.21.1 frames every packet with a 21-bit varint length prefix
 * (max 2097151 bytes per frame). Any larger frame throws
 * {@code EncoderException("Packet too large: size X is over 8")} - this is the
 * exception that kicks players when AE2 sends a huge container_set_content
 * packet. A 5-byte varint covers up to 2^35-1 bytes, far beyond the maximum
 * configurable packet size, so this is a fixed constant.
 */
@Mixin(Varint21LengthFieldPrepender.class)
public abstract class Varint21LengthFieldPrependerMixin {
    /** Maximum varint bytes needed for any frame up to the config ceiling (512 MiB). */
    private static final int MAX_VARINT_BYTES = 5;

    @ModifyConstant(method = "encode", constant = @Constant(intValue = 3))
    private int raisepacketlimit$allowWiderFrameVarint(int original) {
        return MAX_VARINT_BYTES;
    }
}