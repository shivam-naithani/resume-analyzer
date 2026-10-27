package com.resumeanalyzer.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JobDescriptionFillerTest {

    @Test
    void recognisesCommonJobPostingFiller() {
        for (String word : List.of("required", "strong", "knowledge", "familiarity", "looking",
                "include", "preferred", "efficient", "candidate", "responsibilities", "developer", "skills")) {
            assertTrue(JobDescriptionFiller.isFiller(word), word + " should be filler");
        }
    }

    @Test
    void neverTreatsRealSkillsAsFiller() {
        for (String skill : List.of("java", "spring", "boot", "react", "docker", "kubernetes", "aws",
                "sql", "mysql", "jwt", "git", "python", "javascript", "microservices", "postgresql",
                "communication", "testing", "agile", "linux", "api", "rest")) {
            assertFalse(JobDescriptionFiller.isFiller(skill), skill + " is a real keyword");
        }
    }

    @Test
    void ignoresCaseAndHandlesNull() {
        assertTrue(JobDescriptionFiller.isFiller("REQUIRED"));
        assertFalse(JobDescriptionFiller.isFiller(null));
        assertFalse(JobDescriptionFiller.isFiller(""));
    }
}