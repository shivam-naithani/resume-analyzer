package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.AnalysisResponse;
import com.resumeanalyzer.dto.AnalysisSummary;
import com.resumeanalyzer.dto.CreateAnalysisRequest;
import com.resumeanalyzer.entity.Analysis;
import com.resumeanalyzer.entity.Resume;
import com.resumeanalyzer.repository.AnalysisRepository;
import com.resumeanalyzer.repository.ResumeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class AnalysisService {

    private final ResumeRepository resumeRepository;
    private final AnalysisRepository analysisRepository;
    private final ScoringService scoringService;

    public AnalysisService(ResumeRepository resumeRepository,
                           AnalysisRepository analysisRepository,
                           ScoringService scoringService) {
        this.resumeRepository = resumeRepository;
        this.analysisRepository = analysisRepository;
        this.scoringService = scoringService;
    }

    public AnalysisResponse createAnalysis(Long userId, CreateAnalysisRequest request) {
        // Same 404 whether the resume doesn't exist or belongs to someone else,
        // so callers can't probe which resume IDs exist.
        Resume resume = resumeRepository.findByIdAndUserId(request.resumeId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found"));

        ScoringEngine.MatchResult result =
                scoringService.scoreResumeAgainstJD(resume.getExtractedText(), request.jobDescriptionText());

        Analysis analysis = new Analysis();
        analysis.setResume(resume);
        analysis.setJobDescriptionText(request.jobDescriptionText());
        analysis.setMatchScore(result.score());
        analysis.setMissingKeywords(result.missingKeywords());
        analysis.setSectionScores(Map.of());

        return toResponse(analysisRepository.save(analysis));
    }

    @Transactional(readOnly = true)
    public List<AnalysisSummary> listAnalyses(Long userId) {
        return analysisRepository.findByResume_User_IdOrderByCreatedAtDesc(userId).stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public AnalysisResponse getAnalysis(Long userId, Long id) {
        // Same 404 for "doesn't exist" and "belongs to someone else".
        Analysis analysis = analysisRepository.findByIdAndResume_User_Id(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Analysis not found"));
        return toResponse(analysis);
    }

    private AnalysisResponse toResponse(Analysis a) {
        return new AnalysisResponse(
                a.getId(),
                a.getResume().getId(),
                a.getJobDescriptionText(),
                a.getMatchScore(),
                a.getMissingKeywords(),
                a.getSectionScores(),
                a.getCreatedAt());
    }

    private AnalysisSummary toSummary(Analysis a) {
        return new AnalysisSummary(
                a.getId(),
                a.getResume().getId(),
                a.getResume().getOriginalFilename(),
                a.getMatchScore(),
                a.getCreatedAt());
    }
}