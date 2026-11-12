package com.resumeanalyzer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtcLocalDateTimeSerializerTest {

    private static ObjectMapper mapper() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDateTime.class, new UtcLocalDateTimeSerializer());
        return new ObjectMapper().registerModule(module);
    }

    @Test
    void writesAUtcTimestampWithATrailingZ() throws Exception {
        String json = mapper().writeValueAsString(Map.of("createdAt", LocalDateTime.of(2026, 11, 11, 12, 38, 5, 123_000_000)));

        assertEquals("{\"createdAt\":\"2026-11-11T12:38:05.123Z\"}", json);
    }

    @Test
    void stillWritesSecondsWhenTheyAreZero() throws Exception {
        String json = mapper().writeValueAsString(Map.of("createdAt", LocalDateTime.of(2026, 11, 11, 12, 38)));

        assertEquals("{\"createdAt\":\"2026-11-11T12:38:00Z\"}", json);
    }

    @Test
    void winsOverAnEarlierRegisteredLocalDateTimeSerializer() throws Exception {
        // Stands in for the default java.time support, which is registered before our own module.
        SimpleModule defaults = new SimpleModule();
        defaults.addSerializer(LocalDateTime.class, new com.fasterxml.jackson.databind.JsonSerializer<>() {
            @Override
            public void serialize(LocalDateTime v, com.fasterxml.jackson.core.JsonGenerator g,
                                  com.fasterxml.jackson.databind.SerializerProvider p) throws java.io.IOException {
                g.writeString("DEFAULT");
            }
        });
        SimpleModule ours = new SimpleModule();
        ours.addSerializer(LocalDateTime.class, new UtcLocalDateTimeSerializer());
        ObjectMapper mapper = new ObjectMapper().registerModule(defaults).registerModule(ours);

        assertEquals("\"2026-11-11T12:38:00Z\"", mapper.writeValueAsString(LocalDateTime.of(2026, 11, 11, 12, 38)));
    }
}