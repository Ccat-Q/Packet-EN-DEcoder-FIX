package com.ccatq.raisepacketlimit.mixin;

import com.ccatq.raisepacketlimit.Config;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Raises the allocation guard while reading large chunk packets. */
@Mixin(ClientboundLevelChunkPacketData.class)
public abstract class ClientboundLevelChunkPacketDataMixin {
    @ModifyConstant(method = "<init>(Lnet/minecraft/network/RegistryFriendlyByteBuf;II)V",
            constant = @Constant(intValue = 2_097_152), require = 0)
    private int raisepacketlimit$chunkDataLimit(int original) {
        return Config.maxChunkDataBytes();
    }
}
