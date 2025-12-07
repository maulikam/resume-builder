package com.resumebuilder.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.resumebuilder.backend.domain.Skill;

public interface SkillRepository extends JpaRepository<Skill, Long> {
}
