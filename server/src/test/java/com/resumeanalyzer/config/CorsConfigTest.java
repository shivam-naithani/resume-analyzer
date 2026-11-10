package com.resumeanalyzer.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CorsConfigTest {

    private static CorsConfiguration configurationFor(String origins) {
        UrlBasedCorsConfigurationSource source =
                (UrlBasedCorsConfigurationSource) new CorsConfig(origins).corsConfigurationSource();
        return source.getCorsConfigurations().get("/**");
    }

    @Test
    void usesASingleConfiguredOrigin() {
        assertEquals(List.of("http://localhost:5173"), CorsConfig.parseOrigins("http://localhost:5173"));
    }

    @Test
    void splitsACommaSeparatedListAndTrimsSpaces() {
        assertEquals(
                List.of("https://app.example.com", "http://localhost:5173"),
                CorsConfig.parseOrigins(" https://app.example.com , http://localhost:5173 "));
    }

    @Test
    void stripsTrailingSlashesBecauseBrowsersSendNone() {
        assertEquals(List.of("https://app.example.com"), CorsConfig.parseOrigins("https://app.example.com/"));
    }

    @Test
    void ignoresEmptyEntries() {
        assertEquals(List.of("https://a.example.com"), CorsConfig.parseOrigins("https://a.example.com,, ,"));
    }

    @Test
    void refusesToStartWithoutAnyOrigin() {
        assertThrows(IllegalStateException.class, () -> CorsConfig.parseOrigins(""));
        assertThrows(IllegalStateException.class, () -> CorsConfig.parseOrigins(" , "));
        assertThrows(IllegalStateException.class, () -> new CorsConfig("/"));
    }

    @Test
    void appliesTheConfiguredOriginsAndAllowsTheMethodsTheAppUses() {
        CorsConfiguration configuration = configurationFor("https://app.example.com");

        assertEquals(List.of("https://app.example.com"), configuration.getAllowedOrigins());
        assertTrue(configuration.getAllowedMethods().containsAll(
                List.of("GET", "POST", "DELETE", "OPTIONS")));
        assertEquals(List.of("*"), configuration.getAllowedHeaders());
    }
}