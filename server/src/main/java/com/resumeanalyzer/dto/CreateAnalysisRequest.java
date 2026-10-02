package com.resumeanalyzer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAnalysisRequest(
    @NotNull Long resumeId,
    @NotBlank String jobDescriptionText
) {}
