package com.resumebuilder.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.resumebuilder.backend.domain.JobDescription;
import com.resumebuilder.backend.web.dto.JobDescriptionRequest;
import com.resumebuilder.backend.web.dto.JobDescriptionResponse;

@Mapper(componentModel = "spring")
public interface JobDescriptionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "extractedKeywords", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    JobDescription toEntity(JobDescriptionRequest request);

    JobDescriptionResponse toResponse(JobDescription entity);
}
