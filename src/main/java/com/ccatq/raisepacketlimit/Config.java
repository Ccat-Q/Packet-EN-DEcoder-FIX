package com.ccatq.raisepacketlimit;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration for Raise Packet Limit.
 * <p>
 * The key {@code maxPacketSize} (bytes, default 67108864 = 64 MiB) replaces the
 * vanilla hard-coded 2 MiB (2097152) packet size limit in all network layers:
 * packet encode/decode, compression, and varint length framing.
 * <p>
 * Written to {@code config/raisepacketlimit.toml} on both client and server.
 */
public final class Config {
    /** Vanilla hard limit, kept as the floor so the config can never make things stricter than vanilla. */
    public static final int MIN_PACKET_SIZE = 2 * 1024 * 1024;
    /** Safety ceiling to avoid absurd values (512 MiB). */
    public static final int MAX_PACKET_SIZE = 512 * 1024 * 1024;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue MAX_PACKET_SIZE_VALUE = BUILDER
            .comment(
                    "Maximum size in bytes for a single network packet (both directions, before compression).",
                    "Vanilla hard-codes this to 2 MiB (2097152). Packets larger than that (e.g. AE2 wireless",
                    "terminals with thousands of patterns, which send a huge container_set_content packet)",
                    "kick the player with 'Failed to encode packet ...'.",
                    "Must be set to the SAME value on BOTH the server and every client.",
                    "Requires a restart to take effect.",
                    "Default: 67108864 (64 MiB).")
            .defineInRange("maxPacketSize", 64 * 1024 * 1024, MIN_PACKET_SIZE, MAX_PACKET_SIZE);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static int maxPacketSize = MAX_PACKET_SIZE_VALUE.getDefault();

    private Config() {
    }

    public static int maxPacketSize() {
        return maxPacketSize;
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        maxPacketSize = MAX_PACKET_SIZE_VALUE.get();
        PacketSizeLimits.update(maxPacketSize);
    }
}