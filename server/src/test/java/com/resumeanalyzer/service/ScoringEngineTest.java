package com.resumeanalyzer.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Plain JUnit 5 test — no Spring context needed, since ScoringEngine has zero
// framework dependencies. This is intentional: fast, isolated unit tests.
class ScoringEngineTest {

    @Test
    void identicalTextsScoreMaximum() {
        String text = "Java Spring Boot developer with REST API experience";
        var result = ScoringEngine.computeMatch(text, text);
        assertEquals(100.0, result.score());
        assertTrue(result.missingKeywords().isEmpty());
    }

    @Test
    void emptyResumeScoresZero() {
        var result = ScoringEngine.computeMatch("", "Java Spring Boot developer needed");
        assertEquals(0.0, result.score());
        assertFalse(result.missingKeywords().isEmpty());
    }

    @Test
    void relevantResumeScoresHigherThanIrrelevantOne() {
        String jd = "Backend developer with Spring Boot, Java, REST APIs, MySQL, Docker, AWS";
        String relevantResume = "Experienced in Spring Boot, Java, REST API design, and MySQL";
        String irrelevantResume = "Graphic designer skilled in Photoshop, Illustrator, branding";

        double relevantScore = ScoringEngine.computeMatch(relevantResume, jd).score();
        double irrelevantScore = ScoringEngine.computeMatch(irrelevantResume, jd).score();

        assertTrue(relevantScore > irrelevantScore);
    }

    @Test
    void missingKeywordsOnlyContainsWordsAbsentFromResume() {
        String jd = "Requires Docker and Kubernetes experience";
        String resume = "Experienced with Docker and CI/CD pipelines";

        var result = ScoringEngine.computeMatch(resume, jd);

        assertFalse(result.missingKeywords().contains("docker")); // present in resume -> not missing
        assertTrue(result.missingKeywords().contains("kubernetes")); // absent from resume -> missing
    }
}
