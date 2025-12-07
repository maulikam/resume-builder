package com.resumebuilder.backend.web.dto;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

public class JobDescriptionResponse {

    private Long id;
    private String title;
    private String company;
    private String content;
    private Set<String> extractedKeywords = new LinkedHashSet<>();
    private OffsetDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Set<String> getExtractedKeywords() {
        return extractedKeywords;
    }

    public void setExtractedKeywords(Set<String> extractedKeywords) {
        this.extractedKeywords = extractedKeywords;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
