package com.resumebuilder.backend.web.dto;

import jakarta.validation.constraints.NotNull;

public class AiSuggestionRequest {

    @NotNull
    private Long profileId;

    @NotNull
    private Long jobDescriptionId;

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
}
