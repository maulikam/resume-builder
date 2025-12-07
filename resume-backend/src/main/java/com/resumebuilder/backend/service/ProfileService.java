package com.resumebuilder.backend.service;

import java.util.List;

import com.resumebuilder.backend.web.dto.ProfileRequest;
import com.resumebuilder.backend.web.dto.ProfileResponse;

public interface ProfileService {

    ProfileResponse createProfile(ProfileRequest request);

    ProfileResponse updateProfile(Long id, ProfileRequest request);

    ProfileResponse getProfile(Long id);

    List<ProfileResponse> listProfiles();

    void deleteProfile(Long id);
}
