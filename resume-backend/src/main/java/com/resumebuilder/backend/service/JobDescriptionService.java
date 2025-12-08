package com.resumebuilder.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.resumebuilder.backend.web.dto.JobDescriptionRequest;
import com.resumebuilder.backend.web.dto.JobDescriptionResponse;

public interface JobDescriptionService {

    JobDescriptionResponse create(JobDescriptionRequest request);

    JobDescriptionResponse get(Long id);

    Page<JobDescriptionResponse> list(String search, Pageable pageable);
}
