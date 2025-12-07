package com.resumebuilder.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.resumebuilder.backend.domain.ResumeTemplate;

public interface ResumeTemplateRepository extends JpaRepository<ResumeTemplate, Long> {
    Optional<ResumeTemplate> findFirstByDefaultTemplateTrue();
}
