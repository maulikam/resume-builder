package com.resumebuilder.backend.service;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.resumebuilder.backend.domain.JobDescription;
import com.resumebuilder.backend.mapper.JobDescriptionMapper;
import com.resumebuilder.backend.repository.JobDescriptionRepository;
import com.resumebuilder.backend.web.dto.JobDescriptionRequest;
import com.resumebuilder.backend.web.dto.JobDescriptionResponse;

import jakarta.persistence.EntityNotFoundException;

@Service
public class DefaultJobDescriptionService implements JobDescriptionService {

    private final JobDescriptionRepository jobDescriptionRepository;
    private final JobDescriptionMapper jobDescriptionMapper;

    public DefaultJobDescriptionService(JobDescriptionRepository jobDescriptionRepository,
            JobDescriptionMapper jobDescriptionMapper) {
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.jobDescriptionMapper = jobDescriptionMapper;
    }

    @Override
    @Transactional
    public JobDescriptionResponse create(JobDescriptionRequest request) {
        JobDescription entity = jobDescriptionMapper.toEntity(request);
        entity.setExtractedKeywords(extractKeywords(request.getContent()));
        JobDescription saved = jobDescriptionRepository.save(entity);
        return jobDescriptionMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public JobDescriptionResponse get(Long id) {
        return jobDescriptionMapper.toResponse(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobDescriptionResponse> list() {
        return jobDescriptionRepository.findAll()
                .stream()
                .map(jobDescriptionMapper::toResponse)
                .toList();
    }

    private JobDescription find(Long id) {
        return jobDescriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Job description not found: " + id));
    }

    private Set<String> extractKeywords(String content) {
        if (content == null || content.isBlank()) {
            return Set.of();
        }
        String normalized = content.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ");
        return new LinkedHashSet<>(
                Arrays.stream(normalized.split("\\s+"))
                        .filter(token -> token.length() > 2)
                        .limit(50)
                        .toList());
    }
}
