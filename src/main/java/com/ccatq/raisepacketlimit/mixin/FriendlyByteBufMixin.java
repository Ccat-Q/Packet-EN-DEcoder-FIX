package com.ccatq.raisepacketlimit.mixin;

import com.ccatq.raisepacketlimit.Config;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Applies Packet Fixer's NBT and string guards to general network buffers. */
@Mixin(FriendlyByteBuf.class)
public abstract class FriendlyByteBufMixin {
    @ModifyConstant(method = "readNbt(Lio/netty/buffer/ByteBuf;)Lnet/minecraft/nbt/CompoundTag;",
            constant = @Constant(longValue = 2_097_152L), require = 0)
    private static long raisepacketlimit$nbtLimit(long original) {
        return Config.maxNbtBytes();
    }

    @ModifyConstant(method = "readUtf()Ljava/lang/String;", constant = @Constant(intValue = 32_767), require = 0)
    private int raisepacketlimit$readStringLimit(int original) {
        return Config.maxStringLength();
    }

    @ModifyConstant(method = "writeUtf(Ljava/lang/String;)Lnet/minecraft/network/FriendlyByteBuf;",
            constant = @Constant(intValue = 32_767), require = 0)
    private int raisepacketlimit$writeStringLimit(int original) {
        return Config.maxStringLength();
    }

    @ModifyConstant(method = "readResourceLocation", constant = @Constant(intValue = 32_767), require = 0)
    private int raisepacketlimit$resourceLocationLimit(int original) {
        return Config.maxStringLength();
    }
}
