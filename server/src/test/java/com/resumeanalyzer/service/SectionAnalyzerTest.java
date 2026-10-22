package com.resumeanalyzer.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SectionAnalyzerTest {

    private static final String JD = "Backend developer with Java, Spring Boot, REST APIs, MySQL, Docker and AWS";

    private static final String FULL_RESUME = """
        Jane Doe
        jane@example.com

        EDUCATION
        B.Tech in Computer Science, 2027

        TECHNICAL SKILLS
        Java, Spring Boot, MySQL, React, Git

        EXPERIENCE
        Backend intern building REST APIs and improving response time by 30 percent

        PROJECTS
        Resume Analyzer built with Spring Boot and React
        CRM System with role based dashboards

        CERTIFICATIONS
        Docker fundamentals certificate
        AWS cloud practitioner certificate
        """;

    // Real layout that exposed the Skills = 0 and Experience-leak bugs.
    private static final String AARAV_RESUME = """
        Aarav Sharma
        SUMMARY
        Computer Science undergraduate graduating in 2027, interested in backend engineering.
        EDUCATION
        B.Tech, Computer Science and Engineering — North City Institute of Technology, New Delhi | 2023–2027
        Relevant coursework: Data Structures and Algorithms, Database Management Systems, Operating Systems, Computer
        Networks, Object-Oriented Programming.
        TECHNICAL SKILLS
        Languages: Java, SQL, JavaScript
        Backend: Spring Boot, Spring Security, REST APIs, JWT, Maven
        Database: MySQL, relational schema design, SQL queries
        Testing & Tools: JUnit 5, Postman, Git, GitHub, IntelliJ IDEA, VS Code
        Concepts: OOP, DSA, exception handling, authentication and authorization
        PROJECTS
        Resume Analyzer | Java, Spring Boot, React, MySQL
        • Developed REST endpoints for resume uploads and job-description analysis.
        EXPERIENCE
        Software Development Intern — ExampleTech Solutions | May 2026–July 2026
        • Assisted with implementing and testing backend REST endpoints under mentor guidance.
        • Wrote SQL queries to validate records and troubleshoot data issues.
        • Used Git feature branches and pull requests to submit incremental changes.
        ACHIEVEMENTS & ACTIVITIES
        • Practiced data structures and algorithms in Java, including arrays, hash maps, binary search, trees, and recursion.
        • Built project documentation and API test collections to make local setup and testing reproducible.
        """;

    @SuppressWarnings("unchecked")
    private static Map<String, Object> section(Map<String, Object> result, String name) {
        return (Map<String, Object>) result.get(name);
    }

    @Test
    void detectsAllFourSectionsInAStandardResume() {
        var result = SectionAnalyzer.analyze(FULL_RESUME, JD);

        for (String name : new String[] {"education", "skills", "experience", "projects"}) {
            assertEquals(true, section(result, name).get("present"), name + " should be present");
        }
    }

    @Test
    void sectionEndsAtTheNextHeading() {
        var result = SectionAnalyzer.analyze(FULL_RESUME, JD);

        // Only the two project lines (8 + 6 words); the Certifications text must not leak in.
        assertEquals(14, section(result, "projects").get("wordCount"));
    }

    @Test
    void missingSectionIsReportedAsAbsent() {
        String resume = """
            Jane Doe

            Education
            B.Sc Computer Science, 2025

            Skills & Tools
            Java, Docker, SQL
            """;

        var result = SectionAnalyzer.analyze(resume, JD);

        assertEquals(false, section(result, "projects").get("present"));
        assertEquals(0, section(result, "projects").get("wordCount"));
        assertEquals(false, section(result, "experience").get("present"));
        assertEquals(true, section(result, "skills").get("present"));
    }

    @Test
    void recognisesInlineHeadingWithColon() {
        var result = SectionAnalyzer.analyze("John Smith\nSkills: Java, Spring Boot, MySQL, Docker, AWS", JD);

        assertEquals(true, section(result, "skills").get("present"));
        assertEquals(6, section(result, "skills").get("wordCount"));
    }

    @Test
    void keywordCoverageReflectsHowManyJdKeywordsAppearInTheSection() {
        // JD keywords: java, spring, boot, mysql, docker (5 distinct terms)
        String jd = "Java Spring Boot MySQL Docker";
        String resume = "Skills\nJava Spring Boot Kubernetes Terraform";

        var result = SectionAnalyzer.analyze(resume, jd);

        // 3 of 5 JD keywords found in the skills section
        assertEquals(60.0, section(result, "skills").get("keywordCoverage"));
    }

    @Test
    void educationIsCheckedForPresenceOnly() {
        var result = SectionAnalyzer.analyze(FULL_RESUME, JD);

        assertFalse(section(result, "education").containsKey("keywordCoverage"));
    }

    @Test
    void emptyOrNullInputDoesNotThrowAndReportsEverythingAbsent() {
        for (String resume : new String[] {"", null}) {
            var result = SectionAnalyzer.analyze(resume, JD);
            for (String name : new String[] {"education", "skills", "experience", "projects"}) {
                assertEquals(false, section(result, name).get("present"));
            }
        }
    }

    // ---- New tests for the fixes ----

    @Test
    void languagesLineInsideSkillsDoesNotEndTheSection() {
        var result = SectionAnalyzer.analyze(AARAV_RESUME, JD);

        // All five skills lines: 4 + 9 + 7 + 12 + 8 words.
        assertEquals(40, section(result, "skills").get("wordCount"));
    }

    @Test
    void standaloneLanguagesHeadingStillEndsSkills() {
        String resume = "SKILLS\nJava, Spring\nLANGUAGES\nHindi, English\nPROJECTS\nCRM System with dashboards";

        var result = SectionAnalyzer.analyze(resume, JD);

        assertEquals(2, section(result, "skills").get("wordCount"));
    }

    @Test
    void achievementsAndActivitiesDoesNotLeakIntoExperience() {
        var result = SectionAnalyzer.analyze(AARAV_RESUME, JD);

        // Only the heading line + 3 bullets of the internship.
        assertEquals(45, section(result, "experience").get("wordCount"));
    }

    @Test
    void unknownHeadingContainingASectionWordEndsThePreviousSection() {
        String resume = """
            EDUCATION
            B.Tech Computer Science ADGIPS 2027
            AWARDS & HONOURS
            Won hackathon first prize at college fest
            """;

        var result = SectionAnalyzer.analyze(resume, JD);

        assertEquals(5, section(result, "education").get("wordCount"));
    }

    @Test
    void capitalisedCompanyNameInsideExperienceIsNotTreatedAsAHeading() {
        String resume = """
            EXPERIENCE
            SPIDERS TECH SERVICES
            Full Stack Development Intern
            Built REST APIs with Spring Boot and MySQL
            """;

        var result = SectionAnalyzer.analyze(resume, JD);

        // 3 + 4 + 8 words: the company name stays part of the section.
        assertEquals(15, section(result, "experience").get("wordCount"));
    }

    @Test
    void recognisesSpacedOutHeadingsFromPdfExtraction() {
        String resume = "E D U C A T I O N\nB.Tech Computer Science 2027 ADGIPS\n"
                + "W O R K   E X P E R I E N C E\nBackend intern building REST APIs with Spring Boot\n";

        var result = SectionAnalyzer.analyze(resume, JD);

        assertEquals(5, section(result, "education").get("wordCount"));
        assertEquals(true, section(result, "experience").get("present"));
    }

    // ---- Tests for the review fixes ----

    @Test
    void titleCaseJobTitleWithASectionWordIsNotAHeading() {
        String resume = """
            EXPERIENCE
            Volunteer Coordinator
            Organised campus events for students
            """;

        var result = SectionAnalyzer.analyze(resume, JD);

        // 2 + 5 words: "Volunteer Coordinator" must stay inside Experience.
        assertEquals(7, section(result, "experience").get("wordCount"));
    }

    @Test
    void bulletedLineIsNeverAHeading() {
        String resume = """
            SKILLS
            Java, Spring
            • Leadership
            • Teamwork
            """;

        var result = SectionAnalyzer.analyze(resume, JD);

        assertEquals(6, section(result, "skills").get("wordCount"));
    }

    @Test
    void hyphenatedHeadingEndsThePreviousSection() {
        String resume = """
            PROJECTS
            CRM System with dashboards
            EXTRA-CURRICULAR ACTIVITIES
            Chess club captain
            """;

        var result = SectionAnalyzer.analyze(resume, JD);

        assertEquals(4, section(result, "projects").get("wordCount"));
    }

    @Test
    void slashHeadingIsRecognised() {
        String resume = """
            EDUCATION
            B.Tech Computer Science 2027
            SKILLS/TOOLS
            Java Spring Docker
            """;

        var result = SectionAnalyzer.analyze(resume, JD);

        assertEquals(4, section(result, "education").get("wordCount"));
        assertEquals(3, section(result, "skills").get("wordCount"));
    }

    @Test
    void singularAndAlternativeHeadingNamesAreRecognised() {
        var edu = SectionAnalyzer.analyze("EDUCATIONAL QUALIFICATION\nB.Tech Computer Science 2027", JD);
        assertEquals(4, section(edu, "education").get("wordCount"));

        var skills = SectionAnalyzer.analyze("PROGRAMMING LANGUAGES\nJava, Python", JD);
        assertEquals(2, section(skills, "skills").get("wordCount"));

        var exp = SectionAnalyzer.analyze("Experience - Internships\nBackend intern building REST APIs", JD);
        assertEquals(5, section(exp, "experience").get("wordCount"));
    }

    @Test
    void strengthsHeadingEndsThePreviousSection() {
        String resume = """
            PROJECTS
            CRM System with dashboards
            STRENGTHS
            Teamwork and leadership
            """;

        var result = SectionAnalyzer.analyze(resume, JD);

        assertEquals(4, section(result, "projects").get("wordCount"));
    }

    @Test
    void dashDecoratedHeadingIsStillAHeading() {
        String resume = """
            EDUCATION
            B.Tech Computer Science 2027
            — EXPERIENCE —
            Backend intern building REST APIs
            - SKILLS -
            Java Spring Docker
            """;

        var result = SectionAnalyzer.analyze(resume, JD);

        assertEquals(4, section(result, "education").get("wordCount"));
        assertEquals(5, section(result, "experience").get("wordCount"));
        assertEquals(3, section(result, "skills").get("wordCount"));
    }

    @Test
    void addedAliasesAreRecognised() {
        var skills = SectionAnalyzer.analyze("TECHNICAL EXPERTISE\nJava Spring MySQL", JD);
        assertEquals(3, section(skills, "skills").get("wordCount"));

        String resume = "PROJECTS\nCRM System with dashboards\nCo-Curricular Activities\nChess club captain\n";
        assertEquals(4, section(SectionAnalyzer.analyze(resume, JD), "projects").get("wordCount"));
    }

    @Test
    void extractSectionsReturnsTheRawTextOfEachSection() {
        var sections = SectionAnalyzer.extractSections(FULL_RESUME);

        assertTrue(sections.get("skills").contains("Spring Boot"));
        assertTrue(sections.get("projects").contains("CRM System"));
        assertFalse(sections.get("projects").contains("Docker fundamentals")); // certifications excluded
    }

    @Test
    void extractSectionsGivesEmptyTextForMissingSectionsAndNullInput() {
        var sections = SectionAnalyzer.extractSections(null);

        assertEquals(java.util.List.of("education", "skills", "experience", "projects"),
                new java.util.ArrayList<>(sections.keySet()));
        assertEquals("", sections.get("projects"));
    }
}