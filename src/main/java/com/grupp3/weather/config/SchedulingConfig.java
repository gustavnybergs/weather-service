package com.grupp3.weather.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Denna klass aktiverar @Scheduled annotations i applikationen
}