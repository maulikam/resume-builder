package com.resumebuilder.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.resumebuilder.backend.web.dto.ProfileRequest;
import com.resumebuilder.backend.web.dto.ProfileResponse;

public interface ProfileService {

    ProfileResponse createProfile(ProfileRequest request);

    ProfileResponse updateProfile(Long id, ProfileRequest request);

    ProfileResponse getProfile(Long id);

    Page<ProfileResponse> listProfiles(String search, Pageable pageable);

    void deleteProfile(Long id);
}
