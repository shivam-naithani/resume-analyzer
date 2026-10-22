package com.resumeanalyzer.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Thin Spring-managed wrapper around ScoringEngine, SectionAnalyzer and SuggestionEngine
 * (all plain Java on purpose, so their logic can be unit tested without booting Spring).
 */
@Service
public class ScoringService {

    public ScoringEngine.MatchResult scoreResumeAgainstJD(String resumeText, String jdText) {
        return ScoringEngine.computeMatch(resumeText, jdText);
    }

    public Map<String, Object> analyzeSections(String resumeText, String jdText) {
        return SectionAnalyzer.analyze(resumeText, jdText);
    }

    public List<String> generateSuggestions(String resumeText,
                                            ScoringEngine.MatchResult match,
                                            Map<String, Object> sectionScores) {
        Map<String, String> sections = SectionAnalyzer.extractSections(resumeText);
        String achievements = sections.get("experience") + "\n" + sections.get("projects");
        return SuggestionEngine.generate(match.score(), match.missingKeywords(), sectionScores, achievements);
    }
}