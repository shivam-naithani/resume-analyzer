package com.resumeanalyzer.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PdfExtractorTest {

    @Test
    void extractsTextFromARealPdf() throws IOException {
        byte[] pdf = buildPdf("React", "Spring Boot", "MySQL");
        String text = PdfExtractor.extractText(new ByteArrayInputStream(pdf));

        assertTrue(text.contains("React"));
        assertTrue(text.contains("MySQL"));
    }

    @Test
    void rejectsAPdfWithNoText() {
        assertThrows(IOException.class, () -> {
            byte[] blankPdf = buildPdf(); // no lines at all
            PdfExtractor.extractText(new ByteArrayInputStream(blankPdf));
        });
    }

    @Test
    void rejectsNonPdfBytes() {
        assertThrows(IOException.class, () ->
            PdfExtractor.extractText(new ByteArrayInputStream("not a pdf".getBytes())));
    }

    private static byte[] buildPdf(String... lines) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(PDType1Font.HELVETICA, 12);
                stream.newLineAtOffset(50, 700);
                for (String line : lines) {
                    stream.showText(line);
                    stream.newLineAtOffset(0, -20);
                }
                stream.endText();
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }
}
