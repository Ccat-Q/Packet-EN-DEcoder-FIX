package com.ccatq.raisepacketlimit.mixin;

import net.minecraft.network.VarLong;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Keeps VarLong validation bounded by its ten-byte signed 64-bit wire format. */
@Mixin(VarLong.class)
public abstract class VarLongMixin {
    @ModifyConstant(method = "getByteSize", constant = @Constant(intValue = 10), require = 0)
    private static int raisepacketlimit$maxVarLongBytes(int original) {
        return 10;
    }

    @ModifyConstant(method = "read", constant = @Constant(intValue = 10), require = 0)
    private static int raisepacketlimit$readMaxVarLongBytes(int original) {
        return 10;
    }
}
