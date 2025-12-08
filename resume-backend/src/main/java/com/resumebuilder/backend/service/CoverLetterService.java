package com.resumebuilder.backend.service;

import com.resumebuilder.backend.web.dto.CoverLetterRequest;
import com.resumebuilder.backend.web.dto.CoverLetterResponse;

public interface CoverLetterService {

    CoverLetterResponse generate(CoverLetterRequest request);

    byte[] download(Long id);
}
