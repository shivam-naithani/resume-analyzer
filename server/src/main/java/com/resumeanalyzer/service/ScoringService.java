package com.resumeanalyzer.service;

import org.springframework.stereotype.Service;

/**
 * Thin Spring-managed wrapper around ScoringEngine (which is plain Java on
 * purpose — no framework dependency — so its logic can be unit tested in
 * total isolation, without booting Spring at all).
 */
@Service
public class ScoringService {
    public ScoringEngine.MatchResult scoreResumeAgainstJD(String resumeText, String jdText) {
        return ScoringEngine.computeMatch(resumeText, jdText);
    }
}
