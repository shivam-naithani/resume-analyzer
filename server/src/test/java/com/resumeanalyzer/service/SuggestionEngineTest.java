package com.resumeanalyzer.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SuggestionEngineTest {

    private static Map<String, Object> section(boolean present, double coverage) {
        return Map.of("present", present, "wordCount", present ? 20 : 0, "keywordCoverage", coverage);
    }

    private static Map<String, Object> allPresent(double skillsCoverage) {
        return Map.of(
                "education", Map.of("present", true, "wordCount", 10),
                "skills", section(true, skillsCoverage),
                "experience", section(true, 20.0),
                "projects", section(true, 20.0));
    }

    private static boolean anyContains(List<String> tips, String text) {
        return tips.stream().anyMatch(tip -> tip.contains(text));
    }

    @Test
    void healthyResumeProducesNoSuggestions() {
        var tips = SuggestionEngine.generate(45.0, List.of(), allPresent(60.0),
                "Cut page load time by 30 percent across 4 services");

        assertTrue(tips.isEmpty());
    }

    @Test
    void lowOverlapScoreAddsATailoringTip() {
        var tips = SuggestionEngine.generate(8.0, List.of(), allPresent(60.0), "Improved speed by 30%");

        assertTrue(anyContains(tips, "very little overlap"));
    }

    @Test
    void undetectedSectionIsPhrasedAsCouldntDetect() {
        Map<String, Object> scores = Map.of(
                "education", Map.of("present", true, "wordCount", 10),
                "skills", section(true, 60.0),
                "experience", section(true, 20.0),
                "projects", section(false, 0.0));

        var tips = SuggestionEngine.generate(45.0, List.of(), scores, "Cut load time by 30 percent");

        assertEquals(1, tips.size());
        assertTrue(tips.get(0).startsWith("We couldn't detect a Projects section"));
        assertFalse(anyContains(tips, "doesn't exist"));
    }

    @Test
    void lowSkillsCoverageMentionsTheRoundedPercentage() {
        var tips = SuggestionEngine.generate(45.0, List.of(), allPresent(13.3), "Cut load time by 30 percent");

        assertTrue(anyContains(tips, "covers only 13%"));
    }

    @Test
    void missingKeywordsDropGenericWordsAndAreCappedAtFive() {
        var keywords = List.of("required", "kubernetes", "strong", "docker", "jwt", "aws",
                "terraform", "kafka", "redis");

        var tips = SuggestionEngine.generate(45.0, keywords, allPresent(60.0), "Cut load time by 30 percent");

        assertEquals(1, tips.size());
        assertTrue(tips.get(0).contains("kubernetes, docker, jwt, aws, terraform"));
        assertFalse(tips.get(0).contains("required"));
        assertFalse(tips.get(0).contains("strong"));
        assertFalse(tips.get(0).contains("kafka")); // sixth useful keyword is cut off
    }

    @Test
    void noKeywordTipWhenOnlyGenericWordsAreMissing() {
        var tips = SuggestionEngine.generate(45.0, List.of("required", "strong", "familiarity"),
                allPresent(60.0), "Cut load time by 30 percent");

        assertTrue(tips.isEmpty());
    }

    @Test
    void textWithOnlyYearsCountsAsNotQuantified() {
        var tips = SuggestionEngine.generate(45.0, List.of(), allPresent(60.0),
                "Backend intern at Acme Corp May 2026 to July 2026 building REST APIs");

        assertTrue(anyContains(tips, "couldn't find any numbers"));
    }

    @Test
    void anyNonYearNumberCountsAsQuantified() {
        var tips = SuggestionEngine.generate(45.0, List.of(), allPresent(60.0),
                "Built 5 REST endpoints in 2026");

        assertFalse(anyContains(tips, "couldn't find any numbers"));
    }

    @Test
    void noQuantifyTipWhenNeitherExperienceNorProjectsWasDetected() {
        Map<String, Object> scores = Map.of(
                "education", Map.of("present", true, "wordCount", 10),
                "skills", section(true, 60.0),
                "experience", section(false, 0.0),
                "projects", section(false, 0.0));

        var tips = SuggestionEngine.generate(45.0, List.of(), scores, "");

        assertFalse(anyContains(tips, "couldn't find any numbers"));
        assertTrue(anyContains(tips, "Experience section"));
        assertTrue(anyContains(tips, "Projects section"));
    }

    @Test
    void emptyOrNullSectionDataSkipsSectionRulesWithoutFailing() {
        for (Map<String, Object> scores : java.util.Arrays.asList(Map.<String, Object>of(), null)) {
            var tips = SuggestionEngine.generate(45.0, List.of("kubernetes"), scores, null);

            assertFalse(anyContains(tips, "couldn't detect"));
            assertTrue(anyContains(tips, "kubernetes"));
        }
    }

    @Test
    void nullMissingKeywordsIsHandled() {
        var tips = SuggestionEngine.generate(45.0, null, allPresent(60.0), "Cut load time by 30 percent");

        assertTrue(tips.isEmpty());
    }
}