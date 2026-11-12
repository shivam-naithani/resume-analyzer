package com.resumeanalyzer.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    // UTC, to match the UTC timestamps stored in the database. A bean so tests can swap in a fixed clock.
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}