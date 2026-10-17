package com.resumeanalyzer.dto;

import java.time.LocalDateTime;

public record AnalysisSummary(
    Long id,
    Long resumeId,
    String resumeFilename,
    Double matchScore,
    LocalDateTime createdAt
) {}
