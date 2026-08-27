package com.ccatq.raisepacketlimit.mixin;

import com.ccatq.raisepacketlimit.Config;
import net.minecraft.network.codec.ByteBufCodecs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Raises the standard NBT decoder quota used by {@link ByteBufCodecs}. */
@Mixin(ByteBufCodecs.class)
public interface ByteBufCodecsMixin {
    @ModifyConstant(method = "*()Lnet/minecraft/nbt/NbtAccounter;", constant = @Constant(longValue = 2_097_152L))
    private static long raisepacketlimit$maxNbtBytes(long original) {
        return Config.maxNbtBytes();
    }
}
