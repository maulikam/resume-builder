package com.resumebuilder.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.resumebuilder.backend.domain.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Page<UserProfile> findByFullNameContainingIgnoreCase(String fullName, Pageable pageable);
}
