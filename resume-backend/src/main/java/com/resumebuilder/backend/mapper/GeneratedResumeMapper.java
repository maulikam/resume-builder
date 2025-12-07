package com.resumebuilder.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.resumebuilder.backend.domain.GeneratedResume;
import com.resumebuilder.backend.web.dto.ResumeGenerationResponse;

@Mapper(componentModel = "spring")
public interface GeneratedResumeMapper {

    @Mapping(target = "generatedResumeId", source = "id")
    @Mapping(target = "message", ignore = true)
    ResumeGenerationResponse toResponse(GeneratedResume entity);
}
