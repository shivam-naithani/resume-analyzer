package com.resumeanalyzer.service;

import com.resumeanalyzer.entity.Resume;
import com.resumeanalyzer.entity.User;
import com.resumeanalyzer.repository.ResumeRepository;
import com.resumeanalyzer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** An uploaded resume must belong to the authenticated user, and bad uploads must save nothing. */
class ResumeOwnershipTest {

    private PdfService pdfService;
    private ResumeRepository resumeRepository;
    private UserRepository userRepository;
    private MultipartFile file;
    private ResumeService service;

    @BeforeEach
    void setUp() {
        pdfService = mock(PdfService.class);
        resumeRepository = mock(ResumeRepository.class);
        userRepository = mock(UserRepository.class);
        file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("cv.pdf");
        service = new ResumeService(pdfService, resumeRepository, userRepository);
    }

    @Test
    void uploadedResumeIsLinkedToTheAuthenticatedUser() throws IOException {
        User user = new User();
        user.setId(1L);
        when(pdfService.extractText(file)).thenReturn("Java Spring Boot developer");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(resumeRepository.save(any(Resume.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.uploadResume(1L, file);

        ArgumentCaptor<Resume> saved = ArgumentCaptor.forClass(Resume.class);
        verify(resumeRepository).save(saved.capture());
        assertSame(user, saved.getValue().getUser());
        assertEquals("Java Spring Boot developer", saved.getValue().getExtractedText());
        assertEquals("cv.pdf", saved.getValue().getOriginalFilename());
    }

    @Test
    void uploadForAUserThatNoLongerExistsIsUnauthorizedAndSavesNothing() throws IOException {
        when(pdfService.extractText(file)).thenReturn("Java Spring Boot developer");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.uploadResume(99L, file));

        assertEquals(401, ex.getStatusCode().value());
        verifyNoMoreInteractions(resumeRepository);
    }

    @Test
    void unreadablePdfIsABadRequestAndNothingIsLookedUpOrSaved() throws IOException {
        when(pdfService.extractText(file)).thenThrow(new IOException("Only PDF files are supported"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.uploadResume(1L, file));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Only PDF files are supported", ex.getReason());
        verifyNoMoreInteractions(userRepository, resumeRepository);
    }
}