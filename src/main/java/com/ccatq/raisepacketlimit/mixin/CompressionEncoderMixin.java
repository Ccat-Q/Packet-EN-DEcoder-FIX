package com.ccatq.raisepacketlimit.mixin;

import com.ccatq.raisepacketlimit.PacketSizeLimits;
import net.minecraft.network.CompressionEncoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Raises the pre-compression input size cap in {@link CompressionEncoder}.
 * <p>
 * Vanilla 1.21.1 rejects any packet larger than 8388608 (8 MiB) before
 * compression with "Packet too big (is X, should be less than 8388608)".
 * Replaced with the configured max packet size (default 67108864 = 64 MiB).
 */
@Mixin(value = CompressionEncoder.class, priority = 1100)
public abstract class CompressionEncoderMixin {
    @ModifyConstant(method = "encode", constant = @Constant(intValue = 8388608))
    private int raisepacketlimit$maxPreCompressionSize(int original) {
        return PacketSizeLimits.maxPacketSize();
    }
}