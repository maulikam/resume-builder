package com.resumebuilder.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.resumebuilder.backend.domain.JobDescription;

public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long> {
    Page<JobDescription> findByTitleContainingIgnoreCaseOrCompanyContainingIgnoreCase(String title, String company, Pageable pageable);
}
