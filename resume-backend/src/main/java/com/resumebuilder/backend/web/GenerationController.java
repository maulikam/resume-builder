package com.resumebuilder.backend.web;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.resumebuilder.backend.service.ResumeGenerationService;
import com.resumebuilder.backend.web.dto.ResumeGenerationRequest;
import com.resumebuilder.backend.web.dto.ResumeGenerationResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/generation")
@Validated
public class GenerationController {

    private final ResumeGenerationService resumeGenerationService;

    public GenerationController(ResumeGenerationService resumeGenerationService) {
        this.resumeGenerationService = resumeGenerationService;
    }

    @PostMapping("/resume")
    public ResponseEntity<ResumeGenerationResponse> generateResume(@Valid @RequestBody ResumeGenerationRequest request) {
        return ResponseEntity.ok(resumeGenerationService.generate(request));
    }

    @GetMapping("/resume/{id}/download")
    public ResponseEntity<ByteArrayResource> downloadResume(@PathVariable("id") Long id) {
        byte[] pdfBytes = resumeGenerationService.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(new ByteArrayResource(pdfBytes));
    }
}
