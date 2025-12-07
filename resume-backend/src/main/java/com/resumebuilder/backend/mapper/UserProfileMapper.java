package com.resumebuilder.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.resumebuilder.backend.domain.Education;
import com.resumebuilder.backend.domain.Experience;
import com.resumebuilder.backend.domain.Skill;
import com.resumebuilder.backend.domain.UserProfile;
import com.resumebuilder.backend.web.dto.EducationDto;
import com.resumebuilder.backend.web.dto.ExperienceDto;
import com.resumebuilder.backend.web.dto.ProfileRequest;
import com.resumebuilder.backend.web.dto.ProfileResponse;
import com.resumebuilder.backend.web.dto.SkillDto;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "experiences", source = "experiences")
    @Mapping(target = "skills", source = "skills")
    @Mapping(target = "education", source = "education")
    UserProfile toEntity(ProfileRequest request);

    ProfileResponse toResponse(UserProfile entity);

    Experience toEntity(ExperienceDto dto);

    ExperienceDto toDto(Experience entity);

    Skill toEntity(SkillDto dto);

    SkillDto toDto(Skill entity);

    Education toEntity(EducationDto dto);

    EducationDto toDto(Education entity);
}
