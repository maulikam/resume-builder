package com.resumebuilder.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.resumebuilder.backend.domain.GeneratedResume;

public interface GeneratedResumeRepository extends JpaRepository<GeneratedResume, Long> {
    List<GeneratedResume> findTop1ByProfileIdOrderByCreatedAtDesc(Long profileId);
}
