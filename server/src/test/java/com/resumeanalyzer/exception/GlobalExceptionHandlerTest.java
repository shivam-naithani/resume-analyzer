package com.resumeanalyzer.exception;

import com.resumeanalyzer.dto.ApiError;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void unexpectedExceptionReturnsGenericMessageWithoutLeakingDetails() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/analyses");
        RuntimeException ex = new RuntimeException(
                "Connection refused: jdbc:mysql://127.0.0.1:3306/resume_analyzer_dev");

        ResponseEntity<ApiError> response = handler.handleUnexpected(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        ApiError body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.status());
        assertEquals("An unexpected error occurred", body.message());
        assertEquals("/api/analyses", body.path());
        assertNull(body.fieldErrors());
        assertFalse(body.message().contains("jdbc"));
    }
}