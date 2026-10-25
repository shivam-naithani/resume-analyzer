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

    @Test
    void rejectsAPdfWithMoreTextThanTheLimit() throws IOException {
        byte[] pdf = buildPdf("This line is long enough to go over a tiny limit");

        IOException ex = assertThrows(IOException.class,
                     () -> PdfExtractor.extractText(new ByteArrayInputStream(pdf), 20));

        assertTrue(ex.getMessage().contains("too much text to be a resume"));
        assertTrue(ex.getMessage().contains("over 20 characters"));
    }

    @Test
    void acceptsAPdfWithinTheLimit() throws IOException {
        byte[] pdf = buildPdf("React", "Spring Boot");

        String text = PdfExtractor.extractText(new ByteArrayInputStream(pdf), 1000);

        assertTrue(text.contains("Spring Boot"));
    }

    @Test
    void rejectsADocumentOverTheDefaultLimitWithAReadableMessage() throws IOException {
        String line = "word ".repeat(40); // 200 characters
        String[] lines = new String[600];   // about 120,000 characters of text
        java.util.Arrays.fill(lines, line);
        byte[] pdf = buildPdf(lines);

        IOException ex = assertThrows(IOException.class,
                () -> PdfExtractor.extractText(new ByteArrayInputStream(pdf)));

        assertEquals("This PDF contains too much text to be a resume (over 100,000 characters). "
                + "Please upload only your resume.", ex.getMessage());
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
