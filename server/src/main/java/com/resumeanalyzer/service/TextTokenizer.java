package com.resumeanalyzer.service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Turns raw text into a clean list of words: lowercase, strip punctuation,
 * drop common "stopwords" (the, and, of...) that carry no matching signal,
 * and drop very short tokens (stray single letters from stripped punctuation).
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

    private TextTokenizer() {}

    public static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) return List.of();

        String cleaned = NON_LETTERS.matcher(text.toLowerCase()).replaceAll(" ");
        String[] rawTokens = cleaned.split("\\s+");

        List<String> tokens = new ArrayList<>();
        for (String token : rawTokens) {
            if (token.length() > 1 && !STOPWORDS.contains(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }
}
