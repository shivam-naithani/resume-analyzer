package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.CreateAnalysisRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analyses")
public class AnalysisController {

    @PostMapping
    public ResponseEntity<?> createAnalysis(@Valid @RequestBody CreateAnalysisRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("message", "analysis creation not implemented yet"));
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
