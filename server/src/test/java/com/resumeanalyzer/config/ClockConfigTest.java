package com.resumeanalyzer.config;

import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClockConfigTest {

    @Test
    void theClockIsUtcToMatchTheStoredTimestamps() {
        assertEquals(ZoneOffset.UTC, new ClockConfig().clock().getZone());
    }
}