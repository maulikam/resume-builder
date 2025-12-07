package com.resumebuilder.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.resumebuilder.backend.domain.Education;

public interface EducationRepository extends JpaRepository<Education, Long> {
}
