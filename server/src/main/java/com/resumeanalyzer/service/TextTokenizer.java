package com.resumeanalyzer.service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns raw text into a clean list of words: lowercase, strip punctuation,
 * drop common "stopwords" (the, and, of...) that carry no matching signal,
 * and drop very short tokens (stray single letters from stripped punctuation).
 *
 * A short curated list of terms is pulled out first and kept as single keywords, because plain
 * punctuation stripping would wreck them: "C++" would vanish, "CI/CD" would become "ci" and "cd",
 * and "problem solving" would become two separate keywords. Spelling variants such as "Node.js"
 * and "NodeJS" are mapped to one form so they match each other.
 */
public final class TextTokenizer {

    private static final Pattern NON_LETTERS = Pattern.compile("[^a-z\\s]");

    // A deliberately small, common-sense stopword list — enough to remove noise
    // without needing an external NLP library.
    private static final Set<String> STOPWORDS = Set.of(
        "a", "an", "the", "and", "or", "but", "if", "then", "so", "of", "to", "in",
        "on", "at", "for", "with", "by", "from", "as", "is", "are", "was", "were",
        "be", "been", "being", "this", "that", "these", "those", "it", "its",
        "into", "will", "shall", "can", "may", "should", "would", "could", "we",
        "you", "your", "our", "their", "they", "he", "she", "his", "her", "i",
        "us", "not", "no", "do", "does", "did", "has", "have", "had", "such",
        "than", "also", "each", "any", "all", "both", "more", "most", "other",
        "some", "about", "up", "out", "over", "under", "again", "further"
    );

    private record Term(String keyword, Pattern pattern) {}

    // Add a term only when stripping punctuation would break it, or would split one concept into
    // pieces that mean little alone. Do not add phrases whose words are useful on their own
    // (such as "spring boot"): matching the whole phrase would lose partial matches.
    private static final List<Term> TERMS = List.of(
        term("c++", "c\\+\\+(?:\\d{2})?"),
        term("c#", "c#"),
        term("f#", "f#"),
        term(".net", "\\.net"),
        term("node.js", "node[\\s.]?js"),
        term("react", "react[\\s.]?js"),
        term("vue", "vue[\\s.]?js"),
        term("next.js", "next[\\s.]?js"),
        term("express", "express[\\s.]?js"),
        term("ci/cd", "ci[\\s/-]*cd"),
        term("pl/sql", "pl\\s*/\\s*sql"),
        term("a/b testing", "a\\s*/\\s*b\\s+testing"),
        term("s3", "s3"),
        term("ec2", "ec2"),
        term("kubernetes", "k8s"),
        term("backend", "back[\\s-]?end"),
        term("frontend", "front[\\s-]?end"),
        term("full stack", "full[\\s-]?stack"),
        term("problem solving", "problem[\\s-]+solving"),
        term("machine learning", "machine[\\s-]+learning"),
        term("deep learning", "deep[\\s-]+learning"),
        term("data structures", "data[\\s-]+structures?"),
        term("object oriented", "object[\\s-]+oriented"),
        term("version control", "version[\\s-]+control")
    );

    private TextTokenizer() {}

    // The boundaries keep a term from matching inside a longer word ("feedback end", "abc++").
    private static Term term(String keyword, String variants) {
        return new Term(keyword, Pattern.compile("(?<![a-z0-9])(?:" + variants + ")(?![a-z0-9])"));
    }

    public static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) return List.of();

        String remaining = text.toLowerCase();
        List<String> tokens = new ArrayList<>();

        // Take the curated terms out first, so the generic cleanup below can't break them.
        for (Term term : TERMS) {
            Matcher matcher = term.pattern().matcher(remaining);
            int found = 0;
            while (matcher.find()) found++;
            if (found > 0) {
                tokens.addAll(Collections.nCopies(found, term.keyword()));
                remaining = term.pattern().matcher(remaining).replaceAll(" ");
            }
        }

        String cleaned = NON_LETTERS.matcher(remaining).replaceAll(" ");
        for (String token : cleaned.split("\\s+")) {
            if (token.length() > 1 && !STOPWORDS.contains(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }
}
