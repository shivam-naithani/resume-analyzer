package com.resumeanalyzer.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record AnalysisResponse(
    Long id,
    Long resumeId,
    String jobDescriptionText,
    Double matchScore,
    List<String> missingKeywords,
    Map<String, Object> sectionScores,
    List<String> suggestions,
    LocalDateTime createdAt
) {}