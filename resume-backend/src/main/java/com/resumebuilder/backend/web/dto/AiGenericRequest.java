package com.resumebuilder.backend.web.dto;

import jakarta.validation.constraints.NotBlank;

public class AiGenericRequest {

    @NotBlank
    private String text;

    @NotBlank
    private String instruction;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }
}
