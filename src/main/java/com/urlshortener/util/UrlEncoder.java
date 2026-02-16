package com.urlshortener.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class UrlEncoder {

    private static final String BASE62_CHARS =
        "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = 62;
    private static final int DEFAULT_LENGTH = 7;
    private static final SecureRandom RANDOM = new SecureRandom();

    // Counter-based encoding for uniqueness under high concurrency
    private static final AtomicLong COUNTER = new AtomicLong(
        System.currentTimeMillis() % 1_000_000
    );

    /**
     * Generates a unique short code using a mix of timestamp + random to
     * guarantee low collision probability under concurrent load.
     */
    public String generateShortCode() {
        long value = COUNTER.incrementAndGet();
        // XOR with random bits for extra uniqueness
        value ^= (long) RANDOM.nextInt(1000) << 20;
        return encodeBase62(Math.abs(value), DEFAULT_LENGTH);
    }

    /**
     * Encodes a long value to a Base62 string of the given length.
     */
    public String encodeBase62(long value, int length) {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length) {
            sb.append(BASE62_CHARS.charAt((int) (value % BASE)));
            value /= BASE;
        }
        return sb.reverse().toString();
    }

    /**
     * Decodes a Base62 string back to a long.
     */
    public long decodeBase62(String code) {
        long result = 0;
        for (char c : code.toCharArray()) {
            result = result * BASE + BASE62_CHARS.indexOf(c);
        }
        return result;
    }

    /**
     * Validates that a string is a valid short code (alphanumeric, 5–10 chars).
     */
    public boolean isValidShortCode(String code) {
        return code != null && code.matches("^[a-zA-Z0-9]{5,10}$");
    }
}
