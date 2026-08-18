package com.ccatq.raisepacketlimit;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/**
 * Raise Packet Limit - removes the vanilla 2 MiB network packet size hard limit.
 * <p>
 * Single jar for BOTH client and server. The mixins target classes that exist on
 * both distributions (net.minecraft.network.*), so this is fully dist-safe.
 */
@Mod(RaisePacketLimit.MODID)
public final class RaisePacketLimit {
    public static final String MODID = "raisepacketlimit";
    private static final Logger LOGGER = LogUtils.getLogger();

    public RaisePacketLimit(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC, "raisepacketlimit.toml");
        // Config load/reload -> refresh the limits read by the mixins.
        modEventBus.addListener(Config::onLoad);
        // Game-bus listeners (commands).
        NeoForge.EVENT_BUS.register(ModCommands.class);

        LOGGER.info("RaisePacketLimit loaded. Max packet size: {} bytes ({} MiB). "
                        + "Ensure this value matches on BOTH server and clients.",
                PacketSizeLimits.maxPacketSize(), PacketSizeLimits.maxPacketSize() / (1024 * 1024));
    }
}