package com.resumeanalyzer.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Framework-free PDF text extraction — same reasoning as ScoringEngine:
 * keep the actual logic testable without booting Spring. PdfService (the
 * @Service wrapper) adapts this to Spring's MultipartFile.
 */
public final class PdfExtractor {

    private PdfExtractor() {}

    public static String extractText(InputStream pdfStream) throws IOException {
        try (PDDocument document = PDDocument.load(pdfStream)) {
            if (document.isEncrypted()) {
                throw new IOException("Cannot extract text from a password-protected PDF");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            if (text == null || text.isBlank()) {
                // Common cause: a scanned/image-only resume with no real text layer.
                throw new IOException("No extractable text found — the PDF may be a scanned image");
            }
            return text;
        }
    }
}
