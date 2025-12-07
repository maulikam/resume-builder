package com.resumebuilder.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.resumebuilder.backend.domain.Experience;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {
}
