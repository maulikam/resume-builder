package com.resumebuilder.backend.web.dto;

public class CoverLetterResponse {

    private Long generatedId;
    private String status;
    private String message;

    public Long getGeneratedId() {
        return generatedId;
    }

    public void setGeneratedId(Long generatedId) {
        this.generatedId = generatedId;
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
}
