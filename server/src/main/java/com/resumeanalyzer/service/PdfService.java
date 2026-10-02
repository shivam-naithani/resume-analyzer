package com.resumeanalyzer.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Spring-facing wrapper around PdfExtractor (which is plain Java on purpose —
 * see ScoringEngine/ScoringService for the same pattern). This class's only
 * job is adapting Spring's MultipartFile to a plain InputStream.
 */
@Service
public class PdfService {
    public String extractText(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("No file was uploaded");
        }
        if (!"application/pdf".equals(file.getContentType())) {
            throw new IOException("Only PDF files are supported");
        }
        return PdfExtractor.extractText(file.getInputStream());
    }
}
