package com.resumebuilder.backend.service;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ai.keyword-extraction", havingValue = "basic", matchIfMissing = true)
public class BasicKeywordExtractor implements KeywordExtractor {

    @Override
    public Set<String> extract(String content) {
        if (content == null || content.isBlank()) {
            return Set.of();
        }
        String normalized = content.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ");
        return new LinkedHashSet<>(
                Arrays.stream(normalized.split("\\s+"))
                        .filter(token -> token.length() > 2)
                        .limit(50)
                        .toList());
    }
}
