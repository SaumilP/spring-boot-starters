package io.github.saumilp.starters.utils;

import java.time.Duration;

/**
 * Common time-to-live constants used when caching values in Redis.
 *
 * @since 1.0.0
 */
public class RedisConstant {

    /** Thirty, expressed in minutes, as a raw value. */
    public static final Long MINUTES = 30L;
    /** One, expressed in days, as a raw value. */
    public static final Long DAY = 1L;
    /** Seven-day duration. */
    public static final Duration WEEK_DAY = Duration.ofDays(7);
    /** Five-day duration. */
    public static final Duration FIVE_DAY = Duration.ofDays(5);
    /** Three-day duration. */
    public static final Duration THREE_DAY = Duration.ofDays(3);
    /** One-day (24-hour) duration. */
    public static final Duration ONE_DAY = Duration.ofHours(24);
    /** Sixty-minute duration. */
    public static final Duration SIXTY_MINUTES = Duration.ofMinutes(60);
    /** Thirty-minute duration. */
    public static final Duration THIRTY_MINUTES = Duration.ofMinutes(30);
    /** Fifteen-minute duration. */
    public static final Duration FIFTEEN_MINUTES = Duration.ofMinutes(15);
    /** Ten-minute duration. */
    public static final Duration TEN_MINUTES = Duration.ofMinutes(10);
    /** Five-minute duration. */
    public static final Duration FIVE_MINUTES = Duration.ofMinutes(5);
    /** Three-minute duration. */
    public static final Duration THREEE_MINUTES = Duration.ofMinutes(3);
    /** Sixty-second duration. */
    public static final Duration SIXTS_SECONDS = Duration.ofSeconds(60);

    /** Creates a new instance; this class only exposes constants. */
    public RedisConstant() {
    }
}
