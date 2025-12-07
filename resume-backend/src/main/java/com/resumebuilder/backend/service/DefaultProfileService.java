package com.resumebuilder.backend.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.resumebuilder.backend.domain.UserProfile;
import com.resumebuilder.backend.mapper.UserProfileMapper;
import com.resumebuilder.backend.repository.UserProfileRepository;
import com.resumebuilder.backend.web.dto.ProfileRequest;
import com.resumebuilder.backend.web.dto.ProfileResponse;

import jakarta.persistence.EntityNotFoundException;

@Service
public class DefaultProfileService implements ProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;

    public DefaultProfileService(UserProfileRepository userProfileRepository, UserProfileMapper userProfileMapper) {
        this.userProfileRepository = userProfileRepository;
        this.userProfileMapper = userProfileMapper;
    }

    @Override
    @Transactional
    public ProfileResponse createProfile(ProfileRequest request) {
        UserProfile profile = userProfileMapper.toEntity(request);
        bindRelations(profile);
        profile.setCreatedAt(OffsetDateTime.now());
        profile.setUpdatedAt(OffsetDateTime.now());
        UserProfile saved = userProfileRepository.save(profile);
        return userProfileMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(Long id, ProfileRequest request) {
        UserProfile existing = findById(id);
        existing.setFullName(request.getFullName());
        existing.setEmail(request.getEmail());
        existing.setPhone(request.getPhone());
        existing.setSummary(request.getSummary());
        existing.setLocation(request.getLocation());
        existing.setUpdatedAt(OffsetDateTime.now());

        UserProfile updatedValues = userProfileMapper.toEntity(request);
        existing.setExperiences(updatedValues.getExperiences());
        existing.setSkills(updatedValues.getSkills());
        existing.setEducation(updatedValues.getEducation());
        bindRelations(existing);

        UserProfile saved = userProfileRepository.save(existing);
        return userProfileMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long id) {
        return userProfileMapper.toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileResponse> listProfiles() {
        return userProfileRepository.findAll()
                .stream()
                .map(userProfileMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteProfile(Long id) {
        if (!userProfileRepository.existsById(id)) {
            throw new EntityNotFoundException("Profile not found: " + id);
        }
        userProfileRepository.deleteById(id);
    }

    private UserProfile findById(Long id) {
        return userProfileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found: " + id));
    }

    private void bindRelations(UserProfile profile) {
        if (profile.getExperiences() != null) {
            profile.getExperiences().forEach(e -> e.setProfile(profile));
        }
        if (profile.getSkills() != null) {
            profile.getSkills().forEach(s -> s.setProfile(profile));
        }
        if (profile.getEducation() != null) {
            profile.getEducation().forEach(ed -> ed.setProfile(profile));
        }
    }
}
