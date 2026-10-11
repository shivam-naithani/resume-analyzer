package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.ResumeResponse;
import com.resumeanalyzer.entity.Resume;
import com.resumeanalyzer.entity.User;
import com.resumeanalyzer.repository.ResumeRepository;
import com.resumeanalyzer.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Service
public class ResumeService {

    private final PdfService pdfService;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    public ResumeService(PdfService pdfService, ResumeRepository resumeRepository, UserRepository userRepository) {
        this.pdfService = pdfService;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
    }

    public ResumeResponse uploadResume(Long userId, MultipartFile file) {
        String extractedText;
        try {
            extractedText = pdfService.extractText(file);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User no longer exists"));

        Resume resume = new Resume();
        resume.setUser(user);
        resume.setExtractedText(extractedText);
        resume.setOriginalFilename(file.getOriginalFilename());

        Resume saved = resumeRepository.save(resume);
        return new ResumeResponse(saved.getId(), saved.getOriginalFilename(), saved.getUploadedAt());
    }
}