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
    /** Maximum safe NBT, payload, and chunk allocation accepted by this mod. */
    public static final int MAX_DATA_SIZE = 512 * 1024 * 1024;
    private static final int DEFAULT_DATA_SIZE = 64 * 1024 * 1024;

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

    private static final ModConfigSpec.LongValue MAX_NBT_BYTES_VALUE = BUILDER
            .comment("Maximum NBT bytes accepted while decoding. Packet Fixer compatibility setting.",
                    "Higher values permit larger item/block-entity data but increase memory pressure.")
            .defineInRange("maxNbtBytes", (long) DEFAULT_DATA_SIZE, MIN_PACKET_SIZE, (long) MAX_DATA_SIZE);

    private static final ModConfigSpec.IntValue MAX_CUSTOM_PAYLOAD_BYTES_VALUE = BUILDER
            .comment("Maximum custom payload bytes in either direction. Packet Fixer compatibility setting.")
            .defineInRange("maxCustomPayloadBytes", DEFAULT_DATA_SIZE, 32_767, MAX_DATA_SIZE);

    private static final ModConfigSpec.IntValue MAX_CHUNK_DATA_BYTES_VALUE = BUILDER
            .comment("Maximum bytes accepted for a chunk packet. Packet Fixer compatibility setting.")
            .defineInRange("maxChunkDataBytes", DEFAULT_DATA_SIZE, MIN_PACKET_SIZE, MAX_DATA_SIZE);

    private static final ModConfigSpec.IntValue MAX_STRING_LENGTH_VALUE = BUILDER
            .comment("Maximum UTF-8 string length accepted by network buffers. Packet Fixer compatibility setting.")
            .defineInRange("maxStringLength", 32_767, 32_767, 16 * 1024 * 1024);

    private static final ModConfigSpec.IntValue CONNECTION_TIMEOUT_SECONDS_VALUE = BUILDER
            .comment("Connection and keep-alive timeout in seconds. Packet Fixer compatibility setting.")
            .defineInRange("connectionTimeoutSeconds", 120, 30, 600);

    private static final ModConfigSpec.BooleanValue FORCE_UNLIMITED_NBT_VALUE = BUILDER
            .comment("DANGEROUS: replaces per-instance NBT quotas with maxNbtBytes. Disabled by default.")
            .define("forceUnlimitedNbt", false);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static int maxPacketSize = MAX_PACKET_SIZE_VALUE.getDefault();
    private static long maxNbtBytes = MAX_NBT_BYTES_VALUE.getDefault();
    private static int maxCustomPayloadBytes = MAX_CUSTOM_PAYLOAD_BYTES_VALUE.getDefault();
    private static int maxChunkDataBytes = MAX_CHUNK_DATA_BYTES_VALUE.getDefault();
    private static int maxStringLength = MAX_STRING_LENGTH_VALUE.getDefault();
    private static int connectionTimeoutSeconds = CONNECTION_TIMEOUT_SECONDS_VALUE.getDefault();
    private static boolean forceUnlimitedNbt = FORCE_UNLIMITED_NBT_VALUE.getDefault();

    private Config() {
    }

    public static int maxPacketSize() {
        return maxPacketSize;
    }

    public static long maxNbtBytes() {
        return maxNbtBytes;
    }

    public static int maxCustomPayloadBytes() {
        return maxCustomPayloadBytes;
    }

    public static int maxChunkDataBytes() {
        return maxChunkDataBytes;
    }

    public static int maxStringLength() {
        return maxStringLength;
    }

    public static int connectionTimeoutSeconds() {
        return connectionTimeoutSeconds;
    }

    public static boolean forceUnlimitedNbt() {
        return forceUnlimitedNbt;
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        maxPacketSize = MAX_PACKET_SIZE_VALUE.get();
        maxNbtBytes = MAX_NBT_BYTES_VALUE.get();
        maxCustomPayloadBytes = MAX_CUSTOM_PAYLOAD_BYTES_VALUE.get();
        maxChunkDataBytes = MAX_CHUNK_DATA_BYTES_VALUE.get();
        maxStringLength = MAX_STRING_LENGTH_VALUE.get();
        connectionTimeoutSeconds = CONNECTION_TIMEOUT_SECONDS_VALUE.get();
        forceUnlimitedNbt = FORCE_UNLIMITED_NBT_VALUE.get();
        PacketSizeLimits.update(maxPacketSize);
    }
}
