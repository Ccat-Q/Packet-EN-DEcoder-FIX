package com.ccatq.raisepacketlimit.mixin;

import net.minecraft.network.VarInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Keeps Packet Fixer compatibility around VarInt guards without violating the
 * five-byte width of Minecraft's signed 32-bit VarInt format.
 */
@Mixin(VarInt.class)
public abstract class VarIntMixin {
    @ModifyConstant(method = "getByteSize", constant = @Constant(intValue = 5), require = 0)
    private static int raisepacketlimit$maxVarIntBytes(int original) {
        return 5;
    }

    @ModifyConstant(method = "read", constant = @Constant(intValue = 5), require = 0)
    private static int raisepacketlimit$readMaxVarIntBytes(int original) {
        return 5;
    }
}
