package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.AnalysisResponse;
import com.resumeanalyzer.dto.AnalysisSummary;
import com.resumeanalyzer.dto.CreateAnalysisRequest;
import com.resumeanalyzer.entity.Analysis;
import com.resumeanalyzer.entity.Resume;
import com.resumeanalyzer.repository.AnalysisRepository;
import com.resumeanalyzer.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * One user must never be able to create, read or list another user's analyses.
 * These tests pin the service layer to the ownership-aware repository methods.
 */
class AnalysisOwnershipTest {

    private static final Long OWNER = 1L;
    private static final Long OTHER_USER = 2L;
    private static final Long RESUME_ID = 5L;
    private static final Long ANALYSIS_ID = 9L;

    private ResumeRepository resumeRepository;
    private AnalysisRepository analysisRepository;
    private ScoringService scoringService;
    private AnalysisService service;

    @BeforeEach
    void setUp() {
        resumeRepository = mock(ResumeRepository.class);
        analysisRepository = mock(AnalysisRepository.class);
        scoringService = mock(ScoringService.class);
        service = new AnalysisService(resumeRepository, analysisRepository, scoringService);
    }

    private static Resume resume() {
        Resume resume = new Resume();
        resume.setId(RESUME_ID);
        resume.setExtractedText("Java Spring Boot developer");
        resume.setOriginalFilename("cv.pdf");
        return resume;
    }

    private static Analysis analysis(Long id) {
        Analysis analysis = new Analysis();
        analysis.setId(id);
        analysis.setResume(resume());
        analysis.setJobDescriptionText("Backend developer");
        analysis.setMatchScore(42.0);
        return analysis;
    }

    @Test
    void createAnalysisWorksOnlyForTheResumeOwner() {
        when(resumeRepository.findByIdAndUserId(RESUME_ID, OWNER)).thenReturn(Optional.of(resume()));
        when(scoringService.scoreResumeAgainstJD(any(), any()))
                .thenReturn(new ScoringEngine.MatchResult(42.0, List.of("docker")));
        when(scoringService.analyzeSections(any(), any())).thenReturn(Map.of());
        when(scoringService.generateSuggestions(any(), any(), any())).thenReturn(List.of("tip"));
        when(analysisRepository.save(any(Analysis.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CreateAnalysisRequest request = new CreateAnalysisRequest(RESUME_ID, "Backend developer");

        AnalysisResponse response = service.createAnalysis(OWNER, request);

        assertEquals(RESUME_ID, response.resumeId());
        assertEquals(42.0, response.matchScore());

        // Same resume id, different caller: not found, nothing scored, nothing saved.
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createAnalysis(OTHER_USER, request));
        assertEquals(404, ex.getStatusCode().value());
        verify(resumeRepository).findByIdAndUserId(RESUME_ID, OTHER_USER);
        verify(scoringService, times(1)).scoreResumeAgainstJD(any(), any()); // only the owner's call
        verify(analysisRepository, times(1)).save(any(Analysis.class));      // only the owner's call
    }

    @Test
    void getAnalysisReturnsTheAnalysisOnlyToItsOwner() {
        when(analysisRepository.findByIdAndResume_User_Id(ANALYSIS_ID, OWNER))
                .thenReturn(Optional.of(analysis(ANALYSIS_ID)));

        assertEquals(ANALYSIS_ID, service.getAnalysis(OWNER, ANALYSIS_ID).id());

        // Same analysis id, different caller: 404, indistinguishable from "doesn't exist".
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getAnalysis(OTHER_USER, ANALYSIS_ID));
        assertEquals(404, ex.getStatusCode().value());
        verify(analysisRepository).findByIdAndResume_User_Id(ANALYSIS_ID, OWNER);
        verify(analysisRepository).findByIdAndResume_User_Id(ANALYSIS_ID, OTHER_USER);
    }

    @Test
    void listAnalysesOnlyReturnsTheCallersOwnHistory() {
        when(analysisRepository.findByResume_User_IdOrderByCreatedAtDesc(OWNER))
                .thenReturn(List.of(analysis(9L), analysis(8L)));

        List<AnalysisSummary> mine = service.listAnalyses(OWNER);
        List<AnalysisSummary> theirs = service.listAnalyses(OTHER_USER);

        assertEquals(List.of(9L, 8L), mine.stream().map(AnalysisSummary::id).toList());
        assertTrue(theirs.isEmpty());
        verify(analysisRepository).findByResume_User_IdOrderByCreatedAtDesc(OWNER);
        verify(analysisRepository).findByResume_User_IdOrderByCreatedAtDesc(OTHER_USER);
    }
}