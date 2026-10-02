package com.resumeanalyzer.service;

import java.util.*;

/**
 * Compares a resume against a job description using TF-IDF weighted cosine
 * similarity, treating the two texts as a tiny 2-document corpus.
 *
 * Why TF-IDF over plain word-overlap: a word that appears in BOTH texts but is
 * generic (e.g. "team", "experience") should count for less than a word that's
 * rare across the two documents but present in both (e.g. "kubernetes"). IDF is
 * what down-weights the generic word and up-weights the specific one.
 */
public final class ScoringEngine {

    private ScoringEngine() {}

    public record MatchResult(double score, List<String> missingKeywords) {}

    public static MatchResult computeMatch(String resumeText, String jdText) {
        List<String> resumeTokens = TextTokenizer.tokenize(resumeText);
        List<String> jdTokens = TextTokenizer.tokenize(jdText);

        Set<String> vocabulary = new HashSet<>();
        vocabulary.addAll(resumeTokens);
        vocabulary.addAll(jdTokens);

        Map<String, Double> resumeTf = termFrequency(resumeTokens);
        Map<String, Double> jdTf = termFrequency(jdTokens);
        Map<String, Double> idf = inverseDocumentFrequency(vocabulary, resumeTokens, jdTokens);

        double dot = 0, resumeNorm = 0, jdNorm = 0;
        for (String term : vocabulary) {
            double resumeWeight = resumeTf.getOrDefault(term, 0.0) * idf.get(term);
            double jdWeight = jdTf.getOrDefault(term, 0.0) * idf.get(term);

            dot += resumeWeight * jdWeight;
            resumeNorm += resumeWeight * resumeWeight;
            jdNorm += jdWeight * jdWeight;
        }

        double cosineSimilarity = (resumeNorm == 0 || jdNorm == 0)
                ? 0.0
                : dot / (Math.sqrt(resumeNorm) * Math.sqrt(jdNorm));

        double score = Math.round(cosineSimilarity * 1000.0) / 10.0; // -> one decimal place, out of 100

        List<String> missingKeywords = findMissingKeywords(jdTf, resumeTf, idf);

        return new MatchResult(score, missingKeywords);
    }

    private static Map<String, Double> termFrequency(List<String> tokens) {
        if (tokens.isEmpty()) return Map.of();
        Map<String, Double> counts = new HashMap<>();
        for (String token : tokens) {
            counts.merge(token, 1.0, Double::sum);
        }
        counts.replaceAll((term, count) -> count / tokens.size());
        return counts;
    }

    private static Map<String, Double> inverseDocumentFrequency(
            Set<String> vocabulary, List<String> resumeTokens, List<String> jdTokens) {

        Set<String> resumeSet = new HashSet<>(resumeTokens);
        Set<String> jdSet = new HashSet<>(jdTokens);
        int totalDocs = 2;

        Map<String, Double> idf = new HashMap<>();
        for (String term : vocabulary) {
            int docFrequency = (resumeSet.contains(term) ? 1 : 0) + (jdSet.contains(term) ? 1 : 0);
            // Smoothed IDF (like scikit-learn's default): avoids a zero weight
            // for terms that happen to appear in both documents.
            double value = Math.log((double) (totalDocs + 1) / (docFrequency + 1)) + 1;
            idf.put(term, value);
        }
        return idf;
    }

    /** JD terms the resume doesn't contain at all, ranked by how distinctive they are in the JD. */
    private static List<String> findMissingKeywords(
            Map<String, Double> jdTf, Map<String, Double> resumeTf, Map<String, Double> idf) {

        return jdTf.keySet().stream()
                .filter(term -> !resumeTf.containsKey(term))
                .sorted((a, b) -> Double.compare(
                        jdTf.get(b) * idf.get(b),
                        jdTf.get(a) * idf.get(a)))
                .limit(15)
                .toList();
    }
}
