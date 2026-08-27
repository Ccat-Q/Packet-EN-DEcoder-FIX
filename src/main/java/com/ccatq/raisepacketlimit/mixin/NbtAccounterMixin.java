package com.ccatq.raisepacketlimit.mixin;

import com.ccatq.raisepacketlimit.Config;
import net.minecraft.nbt.NbtAccounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Optional compatibility switch for mods that create overly small NBT quotas. */
@Mixin(NbtAccounter.class)
public abstract class NbtAccounterMixin {
    @Shadow @Final private long quota;

    @Redirect(method = "accountBytes(J)V", at = @At(value = "FIELD", target = "Lnet/minecraft/nbt/NbtAccounter;quota:J"), require = 0)
    private long raisepacketlimit$quota(NbtAccounter instance) {
        return Config.forceUnlimitedNbt() ? Config.maxNbtBytes() : quota;
    }
}
