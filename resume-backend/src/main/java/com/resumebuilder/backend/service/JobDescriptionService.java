package com.resumebuilder.backend.service;

import java.util.List;

import com.resumebuilder.backend.web.dto.JobDescriptionRequest;
import com.resumebuilder.backend.web.dto.JobDescriptionResponse;

public interface JobDescriptionService {

    JobDescriptionResponse create(JobDescriptionRequest request);

    JobDescriptionResponse get(Long id);

    List<JobDescriptionResponse> list();
}
