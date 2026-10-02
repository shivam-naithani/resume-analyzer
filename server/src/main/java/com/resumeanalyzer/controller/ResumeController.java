package com.resumeanalyzer.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    // tied to the authenticated user's id (available from the JWT via SecurityContext).
    @PostMapping
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("message", "resume upload not implemented yet"));
    }
}
