package com.resumeanalyzer.entity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Saved timestamps must be UTC even when the server runs in another timezone. */
class TimestampsAreUtcTest {

    private TimeZone originalZone;

    @BeforeEach
    void runInIndiaTime() {
        originalZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }

    @AfterEach
    void restoreTimezone() {
        TimeZone.setDefault(originalZone);
    }

    private static void assertIsNowInUtc(LocalDateTime stored) {
        long secondsApart = Math.abs(Duration.between(stored, LocalDateTime.now(ZoneOffset.UTC)).getSeconds());
        assertTrue(secondsApart < 5, "expected the current UTC time but got " + stored);
    }

    @Test
    void userCreatedAtIsUtc() {
        User user = new User();
        user.onCreate();
        assertIsNowInUtc(user.getCreatedAt());
    }

    @Test
    void resumeUploadedAtIsUtc() {
        Resume resume = new Resume();
        resume.onCreate();
        assertIsNowInUtc(resume.getUploadedAt());
    }

    @Test
    void analysisCreatedAtIsUtc() {
        Analysis analysis = new Analysis();
        analysis.onCreate();
        assertIsNowInUtc(analysis.getCreatedAt());
    }
}