package com.resumebuilder.backend.web;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.resumebuilder.backend.service.CoverLetterService;
import com.resumebuilder.backend.web.dto.CoverLetterRequest;
import com.resumebuilder.backend.web.dto.CoverLetterResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cover-letters")
@Validated
public class CoverLetterController {

    private final CoverLetterService coverLetterService;

    public CoverLetterController(CoverLetterService coverLetterService) {
        this.coverLetterService = coverLetterService;
    }

    @PostMapping
    public ResponseEntity<CoverLetterResponse> generate(@Valid @RequestBody CoverLetterRequest request) {
        return ResponseEntity.ok(coverLetterService.generate(request));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<ByteArrayResource> download(@PathVariable("id") Long id) {
        byte[] pdf = coverLetterService.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cover-letter-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(new ByteArrayResource(pdf));
    }
}
