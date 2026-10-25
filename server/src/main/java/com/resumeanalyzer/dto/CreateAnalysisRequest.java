package com.resumeanalyzer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAnalysisRequest(
    @NotNull Long resumeId,
    @NotBlank @Size(max = 20000, message = "Job description must be at most 20,000 characters")
    String jobDescriptionText
) {}