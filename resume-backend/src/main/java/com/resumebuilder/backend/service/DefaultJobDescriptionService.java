package com.resumebuilder.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    private final KeywordExtractor keywordExtractor;

    public DefaultJobDescriptionService(JobDescriptionRepository jobDescriptionRepository,
            JobDescriptionMapper jobDescriptionMapper,
            KeywordExtractor keywordExtractor) {
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.jobDescriptionMapper = jobDescriptionMapper;
        this.keywordExtractor = keywordExtractor;
    }

    @Override
    @Transactional
    public JobDescriptionResponse create(JobDescriptionRequest request) {
        JobDescription entity = jobDescriptionMapper.toEntity(request);
        entity.setExtractedKeywords(keywordExtractor.extract(request.getContent()));
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
    public Page<JobDescriptionResponse> list(String search, Pageable pageable) {
        var page = (search == null || search.isBlank())
                ? jobDescriptionRepository.findAll(pageable)
                : jobDescriptionRepository.findByTitleContainingIgnoreCaseOrCompanyContainingIgnoreCase(search, search, pageable);
        return page.map(jobDescriptionMapper::toResponse);
    }

    private JobDescription find(Long id) {
        return jobDescriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Job description not found: " + id));
    }
}
