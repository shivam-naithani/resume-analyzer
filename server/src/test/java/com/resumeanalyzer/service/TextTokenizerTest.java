package com.resumeanalyzer.service;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextTokenizerTest {

    // The order of the curated terms relative to other words doesn't matter for scoring.
    private static List<String> bag(String text) {
        return TextTokenizer.tokenize(text).stream().sorted().toList();
    }

    private static List<String> same(String keyword, int times) {
        return Collections.nCopies(times, keyword);
    }

    @Test
    void plainTextIsCleanedExactlyAsBefore() {
        assertEquals(List.of("java", "spring", "boot", "developer", "rest", "api", "experience"),
                TextTokenizer.tokenize("Java Spring Boot developer with REST API experience"));
    }

    @Test
    void nullAndBlankTextGiveNoTokens() {
        assertEquals(List.of(), TextTokenizer.tokenize(null));
        assertEquals(List.of(), TextTokenizer.tokenize("   "));
    }

    @Test
    void punctuationStopwordsAndSingleLettersAreStillDropped() {
        assertEquals(List.of("use", "api", "cache"), TextTokenizer.tokenize("Use the API, and a B cache."));
    }

    @Test
    void ciCdIsOneKeywordInEveryCommonSpelling() {
        for (String text : List.of("CI/CD", "CI / CD", "ci-cd", "CICD", "ci cd", "CI/CD pipelines")) {
            List<String> tokens = bag(text);
            assertEquals(true, tokens.contains("ci/cd"), text + " should give ci/cd");
            assertEquals(false, tokens.contains("ci") || tokens.contains("cd"), text + " must not leave fragments");
        }
    }

    @Test
    void cPlusPlusAndCSharpSurvive() {
        assertEquals(List.of("c#", "c++", "f#"), bag("C++, C# and F#"));
        assertEquals(List.of("c++"), bag("C++17"));
    }

    @Test
    void dotNetIsKeptButAspDotNetIsNot() {
        assertEquals(List.of(".net", "core"), bag(".NET Core"));
        assertEquals(List.of("asp", "net"), bag("ASP.NET"));
    }

    @Test
    void javascriptFrameworkSpellingsMapToOneKeyword() {
        assertEquals(same("node.js", 3), bag("Node.js NodeJS node js"));
        assertEquals(same("react", 3), bag("React.js ReactJS react"));
        assertEquals(List.of("vue"), bag("Vue.js"));
        assertEquals(same("next.js", 2), bag("Next.js NextJS"));
        assertEquals(List.of("express"), bag("Express.js"));
    }

    @Test
    void multiWordTermsBecomeOneKeyword() {
        assertEquals(same("problem solving", 3), bag("problem solving, Problem-Solving, problem\nsolving"));
        assertEquals(List.of("machine learning"), bag("Machine Learning"));
        assertEquals(List.of("deep learning"), bag("deep learning"));
        assertEquals(same("data structures", 2), bag("data structure and Data Structures"));
        assertEquals(List.of("object oriented"), bag("object-oriented"));
        assertEquals(List.of("version control"), bag("Version Control"));
    }

    @Test
    void backendFrontendAndFullStackSpellingsMatchEachOther() {
        assertEquals(same("backend", 3), bag("Back-end back end Backend"));
        assertEquals(same("frontend", 2), bag("Front-end frontend"));
        assertEquals(same("full stack", 2), bag("Full-Stack fullstack"));
    }

    @Test
    void shortCloudTermsKeepTheirDigit() {
        assertEquals(List.of("aws", "ec2", "kubernetes", "s3"), bag("AWS S3 and EC2, K8s"));
    }

    @Test
    void termsDoNotMatchInsideLongerWords() {
        assertEquals(List.of("end", "feedback"), bag("feedback end"));
        assertEquals(List.of("abc"), bag("abc++"));
        assertEquals(List.of("network"), bag("network"));
        assertEquals(List.of("reactive"), bag("reactive"));
        assertEquals(List.of("nodejsx"), bag("nodejsx"));
        assertEquals(List.of("problem", "solvers"), bag("problem solvers"));
        assertEquals(List.of("cd", "ci"), bag("ci and cd"));
    }

    @Test
    void repeatedTermsAreCountedEachTime() {
        assertEquals(same("ci/cd", 2), bag("CI/CD, CI/CD"));
    }

    @Test
    void aSentenceMixingCuratedTermsAndPlainWords() {
        assertEquals(
                List.of("c++", "ci/cd", "experience", "node.js", "problem solving", "skills", "strong"),
                bag("Experience with Node.js, C++ and CI/CD; strong problem-solving skills"));
    }
}