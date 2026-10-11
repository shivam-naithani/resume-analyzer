package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.ResumeResponse;
import com.resumeanalyzer.service.ResumeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping
    public ResponseEntity<ResumeResponse> uploadResume(
            @AuthenticationPrincipal Long userId,
            @RequestParam("file") MultipartFile file) {

        ResumeResponse response = resumeService.uploadResume(userId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
