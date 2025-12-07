package com.resumebuilder.backend.service;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.resumebuilder.backend.domain.GeneratedResume;
import com.resumebuilder.backend.domain.JobDescription;
import com.resumebuilder.backend.domain.ResumeTemplate;
import com.resumebuilder.backend.domain.UserProfile;
import com.resumebuilder.backend.mapper.GeneratedResumeMapper;
import com.resumebuilder.backend.repository.GeneratedResumeRepository;
import com.resumebuilder.backend.repository.JobDescriptionRepository;
import com.resumebuilder.backend.repository.ResumeTemplateRepository;
import com.resumebuilder.backend.repository.UserProfileRepository;
import com.resumebuilder.backend.web.dto.ResumeGenerationRequest;
import com.resumebuilder.backend.web.dto.ResumeGenerationResponse;

import jakarta.persistence.EntityNotFoundException;

@Service
public class DefaultResumeGenerationService implements ResumeGenerationService {

    private final UserProfileRepository userProfileRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ResumeTemplateRepository resumeTemplateRepository;
    private final GeneratedResumeRepository generatedResumeRepository;
    private final GeneratedResumeMapper generatedResumeMapper;

    public DefaultResumeGenerationService(UserProfileRepository userProfileRepository,
            JobDescriptionRepository jobDescriptionRepository,
            ResumeTemplateRepository resumeTemplateRepository,
            GeneratedResumeRepository generatedResumeRepository,
            GeneratedResumeMapper generatedResumeMapper) {
        this.userProfileRepository = userProfileRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.resumeTemplateRepository = resumeTemplateRepository;
        this.generatedResumeRepository = generatedResumeRepository;
        this.generatedResumeMapper = generatedResumeMapper;
    }

    @Override
    @Transactional
    public ResumeGenerationResponse generate(ResumeGenerationRequest request) {
        UserProfile profile = userProfileRepository.findById(request.getProfileId())
                .orElseThrow(() -> new EntityNotFoundException("Profile not found: " + request.getProfileId()));
        JobDescription jobDescription = jobDescriptionRepository.findById(request.getJobDescriptionId())
                .orElseThrow(() -> new EntityNotFoundException("Job description not found: " + request.getJobDescriptionId()));

        ResumeTemplate template = null;
        if (request.getTemplateId() != null) {
            template = resumeTemplateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new EntityNotFoundException("Template not found: " + request.getTemplateId()));
        } else {
            template = resumeTemplateRepository.findFirstByDefaultTemplateTrue().orElse(null);
        }

        GeneratedResume generated = new GeneratedResume();
        generated.setProfile(profile);
        generated.setJobDescription(jobDescription);
        generated.setTemplate(template);
        generated.setStatus("COMPLETED");
        generated.setStoragePath("placeholder");
        generated.setCreatedAt(OffsetDateTime.now());
        generated.setUpdatedAt(OffsetDateTime.now());

        GeneratedResume saved = generatedResumeRepository.save(generated);
        ResumeGenerationResponse response = generatedResumeMapper.toResponse(saved);
        response.setMessage("Generated resume placeholder (PDF generation not wired yet).");
        return response;
    }
}
