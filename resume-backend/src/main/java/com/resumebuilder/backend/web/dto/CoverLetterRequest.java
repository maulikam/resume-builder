package com.resumebuilder.backend.web.dto;

import jakarta.validation.constraints.NotNull;

public class CoverLetterRequest {

    @NotNull
    private Long profileId;

    @NotNull
    private Long jobDescriptionId;

    private Long templateId;

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public Long getJobDescriptionId() {
        return jobDescriptionId;
    }

    public void setJobDescriptionId(Long jobDescriptionId) {
        this.jobDescriptionId = jobDescriptionId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }
}
