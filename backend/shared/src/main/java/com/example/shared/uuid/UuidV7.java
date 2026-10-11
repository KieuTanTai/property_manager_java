package com.example.shared.uuid;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Generates UUIDs according to RFC 9562, version 7.
 */
public final class UuidV7 {
    private static final SecureRandom RANDOM = new SecureRandom();

    private UuidV7() {
    }

    public static UUID generate() {
        long timestamp = System.currentTimeMillis();
        long mostSignificantBits = (timestamp & 0x0000FFFFFFFFFFFFL) << 16;
        mostSignificantBits |= (RANDOM.nextLong() >>> 12) & 0x0FFFL;
        mostSignificantBits &= 0xFFFFFFFFFFFF0FFFL;
        mostSignificantBits |= 0x0000000000007000L;

        long leastSignificantBits = RANDOM.nextLong();
        leastSignificantBits &= 0x3FFFFFFFFFFFFFFFL;
        leastSignificantBits |= 0x8000000000000000L;
        return new UUID(mostSignificantBits, leastSignificantBits);
    }
}
