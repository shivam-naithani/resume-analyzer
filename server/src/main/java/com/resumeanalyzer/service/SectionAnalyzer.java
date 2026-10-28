package com.resumeanalyzer.service;

import java.util.*;

/**
 * Splits resume text into its main sections (education, skills, experience,
 * projects) by recognising heading lines, then reports for each section whether
 * it is present, how long it is, and (except for education) what share of the
 * job description's keywords it contains.
 *
 * Education is checked for presence only: degrees rarely contain job keywords,
 * so a keyword score there would just be noise.
 */
public final class SectionAnalyzer {

    private static final String OTHER = "other";
    private static final int MIN_WORDS_FOR_PRESENT = 3;
    private static final List<String> SECTIONS = List.of("education", "skills", "experience", "projects");

    // Normalised heading text -> canonical section. "other" headings exist only to
    // mark where a section ends (e.g. Certifications after Projects).
    private static final Map<String, String> HEADINGS = new HashMap<>();

    // A short ALL-CAPS line containing one of these words is treated as a heading
    // that ends the current section, even if the exact title isn't in HEADINGS
    // (e.g. "AWARDS & HONOURS"). Title Case lines are deliberately excluded: they are
    // often job titles ("Volunteer Coordinator") and wrongly cutting a section deletes real content.
    private static final Set<String> HEADING_WORDS = Set.of(
            "awards", "honours", "honors", "achievements", "activities", "hobbies",
            "interests", "certifications", "certificates", "volunteer", "volunteering",
            "leadership", "coursework", "publications", "references", "declaration",
            "workshops", "competitions", "responsibility", "extracurricular", "courses");

    // Bullet characters. A bulleted line is never treated as an *unknown* heading.
    private static final java.util.regex.Pattern BULLET = java.util.regex.Pattern.compile("^[•●▪■◦·*\\-–—?]\\s+\\S.*");

    static {
        register("education", "education", "academic qualifications", "academic qualification",
                "academic background", "academic details", "academics", "educational qualifications",
                "educational qualification", "education and training", "qualifications");
        register("skills", "skills", "technical skills", "core skills", "key skills",
                "core competencies", "technical proficiency", "technologies", "skills and tools",
                "skill set", "soft skills", "technical expertise", "professional skills", "tech stack", "programming languages", "tools and technologies",
                "technical skills and tools");
        register("experience", "experience", "work experience", "professional experience",
                "internships", "internship experience", "employment history", "work history",
                "relevant experience", "internship", "experience internships",
                "experience and internships", "work experience and internships");
        register("projects", "projects", "academic projects", "personal projects",
                "key projects", "selected projects", "project experience");
        register(OTHER, "summary", "professional summary", "career objective", "objective",
                "profile", "certifications", "certificates", "achievements", "awards",
                "achievements and activities", "awards and achievements", "achievements and awards",
                "honors and awards", "hobbies", "interests", "languages", "references",
                "extracurricular activities", "extra curricular activities", "extracurriculars", "strengths",
                "key strengths", "personal strengths", "cocurricular activities", "co curricular activities",
                "volunteering", "volunteer experience", "positions of responsibility",
                "publications", "declaration", "personal details", "contact");
    }

    private SectionAnalyzer() {}

    private static void register(String section, String... aliases) {
        for (String alias : aliases) {
            HEADINGS.put(alias, section);
        }
    }

    public static Map<String, Object> analyze(String resumeText, String jdText) {
        Map<String, StringBuilder> sectionText = splitIntoSections(resumeText);
        Set<String> jdTerms = new HashSet<>(JobDescriptionFiller.withoutFiller(TextTokenizer.tokenize(jdText)));

        Map<String, Object> result = new LinkedHashMap<>();
        for (String section : SECTIONS) {
            String text = sectionText.containsKey(section) ? sectionText.get(section).toString() : "";
            int wordCount = countWords(text);

            Map<String, Object> details = new LinkedHashMap<>();
            details.put("present", wordCount >= MIN_WORDS_FOR_PRESENT);
            details.put("wordCount", wordCount);
            if (!section.equals("education")) {
                details.put("keywordCoverage", keywordCoverage(text, jdTerms));
            }
            result.put(section, details);
        }
        return result;
    }

    /** Raw text of each of the four sections; a section that wasn't found maps to an empty string. */
    public static Map<String, String> extractSections(String resumeText) {
        Map<String, StringBuilder> found = splitIntoSections(resumeText);
        Map<String, String> result = new LinkedHashMap<>();
        for (String section : SECTIONS) {
            result.put(section, found.containsKey(section) ? found.get(section).toString() : "");
        }
        return result;
    }

    private static Map<String, StringBuilder> splitIntoSections(String resumeText) {
        Map<String, StringBuilder> sections = new HashMap<>();
        if (resumeText == null || resumeText.isBlank()) return sections;

        String current = null; 

        for (String rawLine : resumeText.split("\\R")) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            String[] headingAndRest = matchHeading(line);
            if (headingAndRest != null) {
                current = headingAndRest[0].equals(OTHER) ? null : headingAndRest[0];
                // "Skills: Java, React" -> the text after the colon belongs to the section.
                if (current != null && !headingAndRest[1].isEmpty()) {
                    sections.computeIfAbsent(current, k -> new StringBuilder())
                            .append(headingAndRest[1]).append('\n');
                }
                continue;
            }

            // Unknown heading (e.g. "AWARDS & HONOURS"): end the current section so
            // its text doesn't leak into the section above. A bulleted line is content,
            // never a heading ("• Leadership" is a skill, not a section).
            if (!BULLET.matcher(line).matches() && looksLikeUnknownHeading(line)) {
                current = null;
                continue;
            }

            if (current != null) {
                sections.computeIfAbsent(current, k -> new StringBuilder()).append(line).append('\n');
            }
        }
        return sections;
    }

    /** Returns {canonicalSection, textAfterColon} if the line is a heading, otherwise null. */
    private static String[] matchHeading(String line) {
        String section = HEADINGS.get(normalize(line));
        if (section != null) return new String[] {section, ""};

        int colon = line.indexOf(':');
        if (colon > 0 && colon <= 40) {
            String prefix = normalize(line.substring(0, colon));
            // "Languages: Java, Python" is a skills line, not a Languages section.
            // Only a standalone "Languages" line (handled above) ends a section.
            if (prefix.equals("languages")) return null;
            section = HEADINGS.get(prefix);
            if (section != null) return new String[] {section, line.substring(colon + 1).trim()};
        }
        return null;
    }

    /**
     * A short ALL-CAPS line made only of letters that contains a section-like word.
     * Deliberately not "any ALL-CAPS line" (company names and one-per-line skills are
     * often capitalised) and not Title Case (job titles such as "Volunteer Coordinator").
     */
    private static boolean looksLikeUnknownHeading(String line) {
        String stripped = line.replaceAll("^[^A-Za-z]+|[^A-Za-z]+$", ""); // dashes, underscores, etc.
        if (stripped.isEmpty() || stripped.length() > 40) return false;
        if (!stripped.matches("[A-Za-z &/-]+")) return false; // headings have no digits or other punctuation
        if (!stripped.equals(stripped.toUpperCase())) return false; // ALL-CAPS only

        String[] words = stripped.split("[ &/-]+");
        if (words.length > 5) return false;

        for (String word : words) {
            if (HEADING_WORDS.contains(word.toLowerCase())) return true;
        }
        return false;
    }

    private static String normalize(String text) {
        // PDF extraction sometimes spaces letters out: "E X P E R I E N C E" or "W O R K   E X P E R I E N C E".
        // Remove single spaces between single letters; the wider gap between words survives.
        String t = text.trim().replaceAll("(?<=\\b[A-Za-z]) (?=[A-Za-z]\\b)", "");
        return t.toLowerCase()
                .replaceAll("(?<=[a-z])-(?=[a-z])", "") // "Extra-Curricular" -> "extracurricular"
                .replace("&", " and ")
                .replace("/", " and ")                  // "Skills/Tools" -> "skills and tools"
                .replaceAll("[^a-z ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static int countWords(String text) {
        String trimmed = text.trim();
        return trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
    }

    /** Percentage (one decimal) of the job description's distinct keywords found in this section. */
    private static double keywordCoverage(String sectionText, Set<String> jdTerms) {
        if (jdTerms.isEmpty()) return 0.0;
        Set<String> sectionTerms = new HashSet<>(TextTokenizer.tokenize(sectionText));
        long matched = jdTerms.stream().filter(sectionTerms::contains).count();
        return Math.round(matched * 1000.0 / jdTerms.size()) / 10.0;
    }
}