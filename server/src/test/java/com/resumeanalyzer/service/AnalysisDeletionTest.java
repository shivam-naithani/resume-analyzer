package com.resumeanalyzer.service;

import com.resumeanalyzer.entity.Analysis;
import com.resumeanalyzer.entity.Resume;
import com.resumeanalyzer.repository.AnalysisRepository;
import com.resumeanalyzer.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Deleting an analysis is owner-only, and it removes the resume too, but only when no other
 * analysis still uses that resume.
 */
class AnalysisDeletionTest {

    private static final Long OWNER = 1L;
    private static final Long OTHER_USER = 2L;
    private static final Long RESUME_ID = 5L;
    private static final Long ANALYSIS_ID = 9L;

    private ResumeRepository resumeRepository;
    private AnalysisRepository analysisRepository;
    private AnalysisService service;
    private Resume resume;
    private Analysis analysis;

    @BeforeEach
    void setUp() {
        resumeRepository = mock(ResumeRepository.class);
        analysisRepository = mock(AnalysisRepository.class);
        service = new AnalysisService(resumeRepository, analysisRepository, mock(ScoringService.class));

        resume = new Resume();
        resume.setId(RESUME_ID);
        analysis = new Analysis();
        analysis.setId(ANALYSIS_ID);
        analysis.setResume(resume);
    }

    @Test
    void deletesTheAnalysisAndThenItsResumeWhenNothingElseUsesIt() {
        when(analysisRepository.findByIdAndResume_User_Id(ANALYSIS_ID, OWNER)).thenReturn(Optional.of(analysis));
        when(analysisRepository.existsByResume_IdAndIdNot(RESUME_ID, ANALYSIS_ID)).thenReturn(false);

        service.deleteAnalysis(OWNER, ANALYSIS_ID);

        // Analysis first: it holds the foreign key to the resume.
        InOrder order = inOrder(analysisRepository, resumeRepository);
        order.verify(analysisRepository).delete(analysis);
        order.verify(resumeRepository).delete(resume);
    }

    @Test
    void keepsTheResumeWhenAnotherAnalysisStillUsesIt() {
        when(analysisRepository.findByIdAndResume_User_Id(ANALYSIS_ID, OWNER)).thenReturn(Optional.of(analysis));
        when(analysisRepository.existsByResume_IdAndIdNot(RESUME_ID, ANALYSIS_ID)).thenReturn(true);

        service.deleteAnalysis(OWNER, ANALYSIS_ID);

        verify(analysisRepository).delete(analysis);
        verify(resumeRepository, never()).delete(any(Resume.class));
    }

    @Test
    void anotherUserGetsA404AndNothingIsDeleted() {
        when(analysisRepository.findByIdAndResume_User_Id(ANALYSIS_ID, OWNER)).thenReturn(Optional.of(analysis));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.deleteAnalysis(OTHER_USER, ANALYSIS_ID));

        assertEquals(404, ex.getStatusCode().value());
        verify(analysisRepository).findByIdAndResume_User_Id(ANALYSIS_ID, OTHER_USER);
        verify(analysisRepository, never()).delete(any(Analysis.class));
        verify(resumeRepository, never()).delete(any(Resume.class));
        verify(analysisRepository, never()).existsByResume_IdAndIdNot(any(), any());
    }

    @Test
    void anUnknownAnalysisIsA404() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.deleteAnalysis(OWNER, 404L));

        assertEquals(404, ex.getStatusCode().value());
        verify(analysisRepository, never()).delete(any(Analysis.class));
        verify(resumeRepository, never()).delete(any(Resume.class));
    }

    @Test
    void theStillInUseCheckIgnoresTheAnalysisBeingDeleted() {
        when(analysisRepository.findByIdAndResume_User_Id(ANALYSIS_ID, OWNER)).thenReturn(Optional.of(analysis));

        service.deleteAnalysis(OWNER, ANALYSIS_ID);

        verify(analysisRepository).existsByResume_IdAndIdNot(RESUME_ID, ANALYSIS_ID);
    }
}