package com.resumeanalyzer.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Words that fill job descriptions ("strong knowledge required", "looking for a team player")
 * but are not skills a candidate could add to a resume. They are ignored everywhere keywords
 * are compared: the match score, keyword coverage, missing keywords and suggestions.
 * Keep this list to genuine filler. Adding a real skill here would hide a keyword the
 * candidate should see.
 */
public final class JobDescriptionFiller {

    private static final Set<String> FILLER = Set.of(
            // qualifiers and praise
            "strong", "excellent", "good", "great", "solid", "proven", "demonstrated", "extensive",
            "relevant", "similar", "related", "highly", "well", "efficient", "efficiently",
            "effective", "effectively", "various", "multiple", "basic", "deep", "key", "new",
            // requirement phrasing
            "required", "requires", "require", "requirements", "requirement", "preferred",
            "preferably", "desired", "ideal", "ideally", "needed", "needs", "must", "plus",
            "bonus", "advantage", "nice", "minimum", "least", "equivalent", "qualifications",
            // knowledge and experience phrasing
            "knowledge", "familiarity", "understanding", "awareness", "exposure", "background",
            "experience", "experienced", "ability", "able", "hands", "years", "year", "skills", "skill",
            // job-posting vocabulary
            "looking", "seeking", "join", "include", "includes", "including", "candidate",
            "candidates", "applicant", "applicants", "role", "position", "job", "company",
            "opportunity", "team", "teams", "responsible", "responsibilities", "duties",
            "benefits", "work", "working", "etc",
            // generic verbs
            "using", "use", "develop", "developing", "build", "building", "implement",
            "implementing", "maintain", "maintaining", "ensure", "ensuring", "collaborate",
            "collaborating", "provide", "providing", "support", "supporting", "create",
            "creating", "manage", "managing", "perform", "performing", "participate",
            "contribute", "contributing", "deliver", "delivering",
            // generic role words
            "developer", "engineer", "software", "systems", "degree");

    private JobDescriptionFiller() {}

    public static boolean isFiller(String term) {
        return term != null && FILLER.contains(term.toLowerCase(Locale.ROOT));
    }

    public static List<String> withoutFiller(List<String> tokens) {
        return tokens.stream().filter(token -> !isFiller(token)).toList();
    }
}