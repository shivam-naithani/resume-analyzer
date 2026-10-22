package com.resumeanalyzer.service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Turns an analysis (match score, missing keywords, section scores) into short,
 * actionable tips. Rule-based on purpose: every suggestion can be traced to one
 * rule and one threshold.
 *
 * Section detection is heuristic, so tips about sections say "we couldn't detect"
 * rather than claiming the section doesn't exist.
 */
public final class SuggestionEngine {

    // Raw cosine-similarity scores run low, so this only flags clearly unrelated resumes.
    private static final double LOW_OVERLAP_SCORE = 15.0;
    private static final double LOW_SKILLS_COVERAGE = 25.0;
    private static final int MAX_KEYWORDS_SHOWN = 5;

    private static final List<String> SECTION_ORDER = List.of("education", "skills", "experience", "projects");

    private static final Map<String, String> SECTION_NAMES = Map.of(
            "education", "Education",
            "skills", "Skills",
            "experience", "Experience",
            "projects", "Projects");

    private static final Map<String, String> SECTION_TIPS = Map.of(
            "education", "add your degree, institution and graduation year",
            "skills", "list the tools and technologies relevant to the job",
            "experience", "include internships or work experience, even short ones",
            "projects", "add two or three projects that use the technologies the job asks for");

    // Job-description filler that shows up as a "missing keyword" but isn't a skill.
    private static final Set<String> GENERIC_TERMS = Set.of(
            // original list
            "required", "strong", "familiarity", "knowledge", "experience", "plus", "ability",
            "skills", "excellent", "looking", "must", "preferred", "understanding", "working",
            "responsible", "team", "years", "good", "candidate", "role", "using", "including",
            "related", "degree", "proven", "similar", "basic", "hands", "systems", "developer",
            "engineer", "work", "etc",
            // plural / verb forms and other filler seen in real job descriptions
            "developers", "developing", "responsibilities", "graduate", "collaborating",
            "unit", "object", "oriented", "include", "writing", "services", "engineers",
            "engineering", "implementing", "intern", "internship", "join", "opportunity",
            "seeking", "ideal", "eager", "learn", "building", "support", "help");

    private static final Pattern YEAR = Pattern.compile("\\b(?:19|20)\\d{2}\\b");

    private SuggestionEngine() {}

    public static List<String> generate(double matchScore,
                                        List<String> missingKeywords,
                                        Map<String, Object> sectionScores,
                                        String experienceAndProjectsText) {
        List<String> tips = new ArrayList<>();

        if (matchScore < LOW_OVERLAP_SCORE) {
            tips.add("Your resume has very little overlap with this job description. Tailor it to the role: "
                    + "mirror the skills and tools the job asks for, wherever they genuinely apply to you.");
        }

        boolean hasSectionData = sectionScores != null && !sectionScores.isEmpty();
        if (hasSectionData) {
            for (String section : SECTION_ORDER) {
                if (!isPresent(sectionScores, section)) {
                    tips.add("We couldn't detect a " + SECTION_NAMES.get(section) + " section. If you have one, "
                            + "give it a standard heading such as \"" + SECTION_NAMES.get(section) + "\"; if not, "
                            + SECTION_TIPS.get(section) + ".");
                }
            }

            Double skillsCoverage = coverage(sectionScores, "skills");
            if (isPresent(sectionScores, "skills") && skillsCoverage != null && skillsCoverage < LOW_SKILLS_COVERAGE) {
                tips.add("Your Skills section covers only " + Math.round(skillsCoverage)
                        + "% of this job's keywords. List the relevant tools and technologies explicitly.");
            }
        }

        List<String> keywords = usefulKeywords(missingKeywords);
        if (!keywords.isEmpty()) {
            tips.add("These keywords from the job description don't appear in your resume: "
                    + String.join(", ", keywords) + ". If you have genuine experience with them, "
                    + "mention them in your Skills, Experience or Projects.");
        }

        boolean hasAchievementSection = hasSectionData
                && (isPresent(sectionScores, "experience") || isPresent(sectionScores, "projects"));
        if (hasAchievementSection && !containsNumbers(experienceAndProjectsText)) {
            tips.add("We couldn't find any numbers in your Experience or Projects. Where you can, quantify results: "
                    + "the percentage you improved something by, how many users or requests a system handled, "
                    + "or how much time you saved.");
        }

        return tips;
    }

    private static List<String> usefulKeywords(List<String> missingKeywords) {
        if (missingKeywords == null) return List.of();
        return missingKeywords.stream()
                .filter(keyword -> !GENERIC_TERMS.contains(keyword))
                .limit(MAX_KEYWORDS_SHOWN)
                .toList();
    }

    /** True if the text has any number other than a year ("2026", "1999"). */
    static boolean containsNumbers(String text) {
        if (text == null) return false;
        return YEAR.matcher(text).replaceAll(" ").chars().anyMatch(Character::isDigit);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> details(Map<String, Object> sectionScores, String section) {
        Object value = sectionScores.get(section);
        return value instanceof Map ? (Map<String, Object>) value : Map.of();
    }

    private static boolean isPresent(Map<String, Object> sectionScores, String section) {
        return Boolean.TRUE.equals(details(sectionScores, section).get("present"));
    }

    private static Double coverage(Map<String, Object> sectionScores, String section) {
        Object value = details(sectionScores, section).get("keywordCoverage");
        return value instanceof Number ? ((Number) value).doubleValue() : null;
    }
}