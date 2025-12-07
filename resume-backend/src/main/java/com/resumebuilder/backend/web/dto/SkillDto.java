package com.resumebuilder.backend.web.dto;

import jakarta.validation.constraints.NotBlank;

public class SkillDto {

    private Long id;

    @NotBlank
    private String name;

    private String proficiency;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProficiency() {
        return proficiency;
    }

    public void setProficiency(String proficiency) {
        this.proficiency = proficiency;
    }
}
