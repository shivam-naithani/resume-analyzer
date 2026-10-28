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

    @Test
    void missingKeywordsLeaveOutJobDescriptionFiller() {
        String jd = "We are looking for strong knowledge of Docker and Kubernetes. "
                + "Familiarity with AWS is preferred and must include testing. Candidates required.";

        var result = ScoringEngine.computeMatch("Java developer", jd);

        assertTrue(result.missingKeywords().containsAll(java.util.List.of("docker", "kubernetes", "aws", "testing")));
        for (String filler : java.util.List.of("looking", "strong", "knowledge", "familiarity",
                "preferred", "include", "candidates", "required")) {
            assertFalse(result.missingKeywords().contains(filler), filler + " should not be listed");
        }
    }

    @Test
    void fillerWordsCannotCrowdRealKeywordsOutOfTheCap() {
        // 17 filler words, each repeated 3 times, outrank "aws" and "docker" (said once) by frequency.
        String filler = "required strong knowledge familiarity preferred looking include candidate excellent "
                + "proven ability responsible understanding working desired ideal efficient ";
        String jd = filler.repeat(3) + "aws docker";

        var result = ScoringEngine.computeMatch("Java developer", jd);

        assertEquals(2, result.missingKeywords().stream()
                .filter(k -> k.equals("aws") || k.equals("docker")).count());
        assertTrue(result.missingKeywords().stream().noneMatch(JobDescriptionFiller::isFiller));
    }

    @Test
    void fillerWordsDoNotAffectTheScore() {
        double plain = ScoringEngine.computeMatch("Java Spring Docker", "Java Spring Kubernetes").score();

        double withFiller = ScoringEngine.computeMatch(
                "Java Spring Docker strong knowledge required",
                "Java Spring Kubernetes looking preferred experience").score();

        assertEquals(plain, withFiller);
    }

    @Test
    void textMadeOnlyOfFillerScoresZero() {
        var result = ScoringEngine.computeMatch("strong knowledge required", "strong knowledge required");

        assertEquals(0.0, result.score());
        assertTrue(result.missingKeywords().isEmpty());
    }

}
