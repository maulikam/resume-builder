package com.resumebuilder.backend.web.dto;

import java.time.OffsetDateTime;

public class ResumeGenerationResponse {

    private Long generatedResumeId;
    private String status;
    private String message;
    private OffsetDateTime createdAt;

    public Long getGeneratedResumeId() {
        return generatedResumeId;
    }

    public void setGeneratedResumeId(Long generatedResumeId) {
        this.generatedResumeId = generatedResumeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
