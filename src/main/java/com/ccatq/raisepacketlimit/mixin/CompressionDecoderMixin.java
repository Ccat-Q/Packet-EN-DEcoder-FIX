package com.ccatq.raisepacketlimit.mixin;

import com.ccatq.raisepacketlimit.PacketSizeLimits;
import net.minecraft.network.CompressionDecoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Raises the maximum declared uncompressed size accepted by
 * {@link CompressionDecoder} when decompressing a received packet.
 * <p>
 * Vanilla 1.21.1 rejects any compressed packet whose declared uncompressed size
 * exceeds 8388608 (8 MiB) with "Badly compressed packet - size of X is larger
 * than protocol maximum of 8388608". Replaced with the configured max packet
 * size (default 67108864 = 64 MiB).
 */
@Mixin(CompressionDecoder.class)
public abstract class CompressionDecoderMixin {
    @ModifyConstant(method = "decode", constant = @Constant(intValue = 8388608))
    private int raisepacketlimit$maxDecompressedSize(int original) {
        return PacketSizeLimits.maxPacketSize();
    }
}