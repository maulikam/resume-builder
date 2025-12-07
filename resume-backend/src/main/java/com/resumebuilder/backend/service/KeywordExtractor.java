package com.resumebuilder.backend.service;

import java.util.Set;

public interface KeywordExtractor {
    Set<String> extract(String content);
}
