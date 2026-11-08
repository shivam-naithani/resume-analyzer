package com.resumeanalyzer.service;

import com.resumeanalyzer.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UnusedResumeCleanupServiceTest {

    // Fixed "now": 8 Nov 2026, 10:00.
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-08T10:00:00Z"), ZoneOffset.UTC);

    private ResumeRepository resumeRepository;

    @BeforeEach
    void setUp() {
        resumeRepository = mock(ResumeRepository.class);
    }

    @Test
    void removesUnusedResumesOlderThanTheMaxAgeInOneBatch() {
        // Cutoff is exactly 24 hours before "now".
        when(resumeRepository.findUnusedIdsUploadedBefore(LocalDateTime.of(2026, 11, 7, 10, 0)))
                .thenReturn(List.of(3L, 4L));

        new UnusedResumeCleanupService(resumeRepository, CLOCK, 24).removeUnusedResumes();

        verify(resumeRepository).deleteAllByIdInBatch(List.of(3L, 4L));
    }

    @Test
    void doesNothingWhenEveryResumeIsUsedOrRecent() {
        when(resumeRepository.findUnusedIdsUploadedBefore(any())).thenReturn(List.of());

        new UnusedResumeCleanupService(resumeRepository, CLOCK, 24).removeUnusedResumes();

        verify(resumeRepository, never()).deleteAllByIdInBatch(any());
    }

    @Test
    void usesTheConfiguredMaxAge() {
        when(resumeRepository.findUnusedIdsUploadedBefore(LocalDateTime.of(2026, 11, 6, 10, 0)))
                .thenReturn(List.of(7L));

        new UnusedResumeCleanupService(resumeRepository, CLOCK, 48).removeUnusedResumes();

        verify(resumeRepository).deleteAllByIdInBatch(List.of(7L));
    }
}