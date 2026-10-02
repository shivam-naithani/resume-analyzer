package com.resumeanalyzer.dto;

import java.time.LocalDateTime;

// Deliberately excludes passwordHash. Controllers should never return the
// User entity directly — always map to this instead.
public record UserResponse(Long id, String name, String email, LocalDateTime createdAt) {}
