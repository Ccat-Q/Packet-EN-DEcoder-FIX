package com.ccatq.raisepacketlimit;

/**
 * Static holder for the effective packet size limits, read by the mixins.
 * <p>
 * Mixins cannot easily read the config directly (they are applied very early,
 * before mod loading finishes), so {@link Config} pushes the loaded value here
 * on every config load event. The default already matches the config default,
 * so the mixins behave correctly even if the config never loads.
 */
public final class PacketSizeLimits {
    public static final int DEFAULT_MAX_PACKET_SIZE = 64 * 1024 * 1024; // 67108864 = 64 MiB

    /**
     * Slack added to the length-framing layers only, so a packet that is exactly
     * {@code maxPacketSize} on the wire (plus varint length prefixes and the
     * compression flag byte) never trips the frame/compression checks.
     */
    public static final int FRAME_SLACK = 32;

    private static volatile int maxPacketSize = DEFAULT_MAX_PACKET_SIZE;

    private PacketSizeLimits() {
    }

    /** The configured maximum payload size in bytes (before compression). */
    public static int maxPacketSize() {
        return maxPacketSize;
    }

    /** Maximum size allowed for a full wire frame (payload + framing overhead). */
    public static int maxFrameSize() {
        return maxPacketSize + FRAME_SLACK;
    }

    /** Called from {@link Config} when the config is loaded or reloaded. */
    public static void update(int size) {
        maxPacketSize = size;
    }
}