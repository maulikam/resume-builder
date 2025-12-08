package com.resumebuilder.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.resumebuilder.backend.domain.GeneratedCoverLetter;

public interface GeneratedCoverLetterRepository extends JpaRepository<GeneratedCoverLetter, Long> {
}
