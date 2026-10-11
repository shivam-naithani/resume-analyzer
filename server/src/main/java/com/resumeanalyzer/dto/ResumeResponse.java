package com.resumeanalyzer.dto;

import java.time.LocalDateTime;

public record ResumeResponse(Long id, String originalFilename, LocalDateTime uploadedAt) {}