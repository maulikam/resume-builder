package com.resumebuilder.backend.service;

import com.resumebuilder.backend.web.dto.ResumeGenerationRequest;
import com.resumebuilder.backend.web.dto.ResumeGenerationResponse;

public interface ResumeGenerationService {

    ResumeGenerationResponse generate(ResumeGenerationRequest request);
}
