package com.resumeanalyzer.service;

import com.resumeanalyzer.repository.ResumeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Removes resumes that were uploaded but never analyzed. The age limit keeps a recent upload
 * available, because the analyze page reuses it when a failed analysis is retried.
 */
@Component
public class UnusedResumeCleanupService {

    private static final Logger log = LoggerFactory.getLogger(UnusedResumeCleanupService.class);

    private final ResumeRepository resumeRepository;
    private final Clock clock;
    private final Duration maxAge;

    public UnusedResumeCleanupService(
            ResumeRepository resumeRepository,
            Clock clock,
            @Value("${cleanup.unused-resume-max-age-hours:24}") long maxAgeHours) {
        this.resumeRepository = resumeRepository;
        this.clock = clock;
        this.maxAge = Duration.ofHours(maxAgeHours);
    }

    // A run shortly after startup matters on hosts that stop idle apps, where a fixed
    // schedule alone would rarely fire.
    @Scheduled(
            initialDelayString = "${cleanup.initial-delay-ms:60000}",
            fixedDelayString = "${cleanup.interval-ms:3600000}")
    @Transactional
    public void removeUnusedResumes() {
        LocalDateTime cutoff = LocalDateTime.now(clock).minus(maxAge);
        List<Long> unusedIds = resumeRepository.findUnusedIdsUploadedBefore(cutoff);
        if (unusedIds.isEmpty()) {
            return;
        }

        resumeRepository.deleteAllByIdInBatch(unusedIds);
        log.info("Removed {} unused resume(s) uploaded before {}", unusedIds.size(), cutoff);
    }
}
