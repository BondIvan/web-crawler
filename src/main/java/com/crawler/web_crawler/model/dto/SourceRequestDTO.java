package com.crawler.web_crawler.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

public record SourceRequestDTO(
        @NotNull @NotBlank(message = "URL cannot be empty")
        String url,

        @NotNull @NotBlank
        @Pattern(regexp = "^\\S+ \\S+ \\S+ \\S+ \\S+ \\S+$", message = "Invalid cron expression")
        String schedule,

        @NotNull
        Map<String, String> selectors
) {}
