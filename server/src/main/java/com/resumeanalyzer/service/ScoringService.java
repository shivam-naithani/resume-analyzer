package com.resumeanalyzer.service;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Thin Spring-managed wrapper around ScoringEngine and SectionAnalyzer (both
 * plain Java on purpose, so their logic can be unit tested without booting Spring).
 */
@Service
public class ScoringService {

    public ScoringEngine.MatchResult scoreResumeAgainstJD(String resumeText, String jdText) {
        return ScoringEngine.computeMatch(resumeText, jdText);
    }

    public Map<String, Object> analyzeSections(String resumeText, String jdText) {
        return SectionAnalyzer.analyze(resumeText, jdText);
    }
}