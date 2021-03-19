package com.zues.netstat.sm;

public enum ConnectionQuality {
    /**
     * No Internet is available.
     */
    NO_CONNECTION, // 0
    /**
     * Bandwidth under 150 kbps.
     */
    POOR, // 1
    /**
     * Bandwidth between 150 and 550 kbps.
     */
    MODERATE, // 2
    /**
     * Bandwidth between 550 and 2000 kbps.
     */
    GOOD, // 3
    /**
     * EXCELLENT - Bandwidth over 2000 kbps.
     */
    EXCELLENT, // 4
    /**
     * Placeholder for unknown bandwidth. This is the initial value and will stay at this value
     * if a bandwidth cannot be accurately found.
     */
    UNKNOWN // 5
}
