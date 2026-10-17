package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.AnalysisResponse;
import com.resumeanalyzer.dto.CreateAnalysisRequest;
import com.resumeanalyzer.service.AnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analyses")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping
    public ResponseEntity<AnalysisResponse> createAnalysis(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateAnalysisRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(analysisService.createAnalysis(userId, request));
    }

    @GetMapping
    public ResponseEntity<?> listAnalyses() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("message", "analysis history not implemented yet"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAnalysis(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("message", "analysis detail not implemented yet"));
    }
}