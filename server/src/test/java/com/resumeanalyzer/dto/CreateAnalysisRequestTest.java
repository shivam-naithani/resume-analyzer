package com.resumeanalyzer.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateAnalysisRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsADescriptionAtTheLimit() {
        var request = new CreateAnalysisRequest(1L, "a".repeat(20000));

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsADescriptionOverTheLimit() {
        var violations = validator.validate(new CreateAnalysisRequest(1L, "a".repeat(20001)));

        assertEquals(1, violations.size());
        assertEquals("jobDescriptionText", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void rejectsABlankDescription() {
        var violations = validator.validate(new CreateAnalysisRequest(1L, "   "));

        assertEquals(1, violations.size());
    }
}
