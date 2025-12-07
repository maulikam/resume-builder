package com.resumebuilder.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.resumebuilder.backend.domain.JobDescription;

public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long> {
}
