package com.resumebuilder.backend.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.resumebuilder.backend.service.JobDescriptionService;
import com.resumebuilder.backend.web.dto.JobDescriptionRequest;
import com.resumebuilder.backend.web.dto.JobDescriptionResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/job-descriptions")
@Validated
public class JobDescriptionController {

    private final JobDescriptionService jobDescriptionService;

    public JobDescriptionController(JobDescriptionService jobDescriptionService) {
        this.jobDescriptionService = jobDescriptionService;
    }

    @PostMapping
    public ResponseEntity<JobDescriptionResponse> create(@Valid @RequestBody JobDescriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobDescriptionService.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<JobDescriptionResponse>> list(
            @RequestParam(value = "q", required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(jobDescriptionService.list(search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDescriptionResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(jobDescriptionService.get(id));
    }
}
