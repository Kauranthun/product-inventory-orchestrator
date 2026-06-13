package com.tutorial.camel.demo.config;

import org.springframework.stereotype.Component;

@Component
public class AppConfig {
    public static final String BASE_URL = "http://localhost:8082";
    public static final String OUTPUT_DIR = "output/responses";
    public static final String TIMESTAMP_FORMAT = "yyyyMMdd-HHmmss";
    public static final String ENTITY_NAME = "product";

    private AppConfig() {
    }
}