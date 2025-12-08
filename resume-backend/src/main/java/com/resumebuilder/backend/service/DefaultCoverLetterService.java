package com.resumebuilder.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.resumebuilder.backend.config.AppProperties;
import com.resumebuilder.backend.domain.GeneratedCoverLetter;
import com.resumebuilder.backend.domain.JobDescription;
import com.resumebuilder.backend.domain.ResumeTemplate;
import com.resumebuilder.backend.domain.UserProfile;
import com.resumebuilder.backend.mapper.GeneratedResumeMapper;
import com.resumebuilder.backend.repository.GeneratedCoverLetterRepository;
import com.resumebuilder.backend.repository.JobDescriptionRepository;
import com.resumebuilder.backend.repository.ResumeTemplateRepository;
import com.resumebuilder.backend.repository.UserProfileRepository;
import com.resumebuilder.backend.web.dto.CoverLetterRequest;
import com.resumebuilder.backend.web.dto.CoverLetterResponse;

import jakarta.persistence.EntityNotFoundException;

@Service
public class DefaultCoverLetterService implements CoverLetterService {

    private final UserProfileRepository userProfileRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ResumeTemplateRepository resumeTemplateRepository;
    private final LatexRenderer latexRenderer;
    private final AppProperties appProperties;
    private final GeneratedResumeMapper generatedResumeMapper;
    private final GeneratedCoverLetterRepository generatedCoverLetterRepository;

    public DefaultCoverLetterService(UserProfileRepository userProfileRepository,
            JobDescriptionRepository jobDescriptionRepository,
            ResumeTemplateRepository resumeTemplateRepository,
            LatexRenderer latexRenderer,
            AppProperties appProperties,
            GeneratedResumeMapper generatedResumeMapper,
            GeneratedCoverLetterRepository generatedCoverLetterRepository) {
        this.userProfileRepository = userProfileRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.resumeTemplateRepository = resumeTemplateRepository;
        this.latexRenderer = latexRenderer;
        this.appProperties = appProperties;
        this.generatedResumeMapper = generatedResumeMapper;
        this.generatedCoverLetterRepository = generatedCoverLetterRepository;
    }

    @Override
    @Transactional
    public CoverLetterResponse generate(CoverLetterRequest request) {
        UserProfile profile = userProfileRepository.findById(request.getProfileId())
                .orElseThrow(() -> new EntityNotFoundException("Profile not found: " + request.getProfileId()));
        JobDescription jobDescription = jobDescriptionRepository.findById(request.getJobDescriptionId())
                .orElseThrow(() -> new EntityNotFoundException("Job description not found: " + request.getJobDescriptionId()));

        ResumeTemplate template = Optional.ofNullable(request.getTemplateId())
                .flatMap(resumeTemplateRepository::findById)
                .or(() -> resumeTemplateRepository.findFirstByDefaultTemplateTrue())
                .orElseThrow(() -> new EntityNotFoundException("Cover letter template not found"));

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("fullName", profile.getFullName());
        placeholders.put("email", profile.getEmail());
        placeholders.put("phone", profile.getPhone());
        placeholders.put("location", profile.getLocation());
        placeholders.put("jobTitle", jobDescription.getTitle());
        placeholders.put("jobCompany", jobDescription.getCompany());
        placeholders.put("jobContent", jobDescription.getContent());
        placeholders.put("summary", profile.getSummary());

        byte[] pdfBytes = latexRenderer.render(template.getLatexContent(), placeholders);
        try {
            Path outputDir = Path.of(appProperties.getLatex().getOutputDir());
            Files.createDirectories(outputDir);
            GeneratedCoverLetter generated = new GeneratedCoverLetter();
            generated.setProfile(profile);
            generated.setJobDescription(jobDescription);
            generated.setTemplate(template);
            generated.setStatus("PENDING");
            generated.setCreatedAt(OffsetDateTime.now());
            generated.setUpdatedAt(OffsetDateTime.now());
            GeneratedCoverLetter saved = generatedCoverLetterRepository.save(generated);

            Path pdfPath = outputDir.resolve("cover-letter-" + saved.getId() + ".pdf");
            Files.write(pdfPath, pdfBytes);

            saved.setStoragePath(pdfPath.toAbsolutePath().toString());
            saved.setStatus("COMPLETED");
            saved.setUpdatedAt(OffsetDateTime.now());
            generatedCoverLetterRepository.save(saved);

            CoverLetterResponse response = new CoverLetterResponse();
            response.setGeneratedId(saved.getId());
            response.setStatus(saved.getStatus());
            response.setMessage("Cover letter generated");
            return response;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write cover letter PDF", e);
        }
    }

    @Override
    public byte[] download(Long id) {
        GeneratedCoverLetter letter = generatedCoverLetterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cover letter not found: " + id));
        if (letter.getStoragePath() == null) {
            throw new IllegalStateException("No PDF available for cover letter " + id);
        }
        try {
            return Files.readAllBytes(Path.of(letter.getStoragePath()));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read cover letter PDF for id " + id, e);
        }
    }
}
