package com.resumeanalyzer.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Framework-free PDF text extraction — same reasoning as ScoringEngine:
 * keep the actual logic testable without booting Spring. PdfService (the
 * @Service wrapper) adapts this to Spring's MultipartFile.
 */
public final class PdfExtractor {

    // A resume is a few pages. Anything far beyond this is not a resume, and the text
    // must fit the database column with room to spare.
    public static final int MAX_TEXT_LENGTH = 100_000;

    private PdfExtractor() {}

    public static String extractText(InputStream pdfStream) throws IOException {
        return extractText(pdfStream, MAX_TEXT_LENGTH);
    }

    static String extractText(InputStream pdfStream, int maxLength) throws IOException {
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
            if (text.length() > maxLength) {
                throw new IOException(String.format(Locale.ROOT,
                        "This PDF contains too much text to be a resume (over %,d characters). "
                                + "Please upload only your resume.", maxLength));
            }
            return text;
        }
    }
}