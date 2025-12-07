package com.resumebuilder.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.resumebuilder.backend.domain.GeneratedResume;
import com.resumebuilder.backend.domain.JobDescription;
import com.resumebuilder.backend.domain.ResumeTemplate;
import com.resumebuilder.backend.domain.UserProfile;
import com.resumebuilder.backend.mapper.GeneratedResumeMapper;
import com.resumebuilder.backend.repository.GeneratedResumeRepository;
import com.resumebuilder.backend.repository.JobDescriptionRepository;
import com.resumebuilder.backend.repository.ResumeTemplateRepository;
import com.resumebuilder.backend.repository.UserProfileRepository;
import com.resumebuilder.backend.web.dto.ResumeGenerationRequest;
import com.resumebuilder.backend.web.dto.ResumeGenerationResponse;

import jakarta.persistence.EntityNotFoundException;

@Service
public class DefaultResumeGenerationService implements ResumeGenerationService {

    private final UserProfileRepository userProfileRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ResumeTemplateRepository resumeTemplateRepository;
    private final GeneratedResumeRepository generatedResumeRepository;
    private final GeneratedResumeMapper generatedResumeMapper;
    private final LatexRenderer latexRenderer;
    private final com.resumebuilder.backend.config.AppProperties appProperties;

    public DefaultResumeGenerationService(UserProfileRepository userProfileRepository,
            JobDescriptionRepository jobDescriptionRepository,
            ResumeTemplateRepository resumeTemplateRepository,
            GeneratedResumeRepository generatedResumeRepository,
            GeneratedResumeMapper generatedResumeMapper,
            LatexRenderer latexRenderer,
            com.resumebuilder.backend.config.AppProperties appProperties) {
        this.userProfileRepository = userProfileRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.resumeTemplateRepository = resumeTemplateRepository;
        this.generatedResumeRepository = generatedResumeRepository;
        this.generatedResumeMapper = generatedResumeMapper;
        this.latexRenderer = latexRenderer;
        this.appProperties = appProperties;
    }

    @Override
    @Transactional
    public ResumeGenerationResponse generate(ResumeGenerationRequest request) {
        UserProfile profile = userProfileRepository.findById(request.getProfileId())
                .orElseThrow(() -> new EntityNotFoundException("Profile not found: " + request.getProfileId()));
        JobDescription jobDescription = jobDescriptionRepository.findById(request.getJobDescriptionId())
                .orElseThrow(() -> new EntityNotFoundException("Job description not found: " + request.getJobDescriptionId()));

        ResumeTemplate template = null;
        if (request.getTemplateId() != null) {
            template = resumeTemplateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new EntityNotFoundException("Template not found: " + request.getTemplateId()));
        } else {
            template = resumeTemplateRepository.findFirstByDefaultTemplateTrue().orElse(null);
        }

        GeneratedResume generated = new GeneratedResume();
        generated.setProfile(profile);
        generated.setJobDescription(jobDescription);
        generated.setTemplate(template);
        generated.setStatus("PENDING");
        generated.setCreatedAt(OffsetDateTime.now());
        generated.setUpdatedAt(OffsetDateTime.now());

        GeneratedResume saved = generatedResumeRepository.save(generated);

        try {
            byte[] pdfBytes = latexRenderer.render(template.getLatexContent(), buildPlaceholders(profile, jobDescription));
            Path outputDir = Path.of(appProperties.getLatex().getOutputDir());
            Files.createDirectories(outputDir);
            Path pdfPath = outputDir.resolve("resume-" + saved.getId() + ".pdf");
            Files.write(pdfPath, pdfBytes);

            saved.setStoragePath(pdfPath.toAbsolutePath().toString());
            saved.setStatus("COMPLETED");
            saved.setUpdatedAt(OffsetDateTime.now());
            GeneratedResume stored = generatedResumeRepository.save(saved);
            ResumeGenerationResponse response = generatedResumeMapper.toResponse(stored);
            response.setMessage("Generated resume via LaTeX pipeline.");
            return response;
        } catch (IOException e) {
            saved.setStatus("FAILED");
            saved.setUpdatedAt(OffsetDateTime.now());
            generatedResumeRepository.save(saved);
            throw new IllegalStateException("Failed to write generated PDF", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] download(Long generatedResumeId) {
        GeneratedResume generatedResume = generatedResumeRepository.findById(generatedResumeId)
                .orElseThrow(() -> new EntityNotFoundException("Generated resume not found: " + generatedResumeId));
        if (generatedResume.getStoragePath() == null) {
            throw new IllegalStateException("No PDF available for generated resume " + generatedResumeId);
        }
        try {
            Path pdfPath = Path.of(generatedResume.getStoragePath());
            return Files.readAllBytes(pdfPath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read generated PDF for id " + generatedResumeId, e);
        }
    }

    private Map<String, String> buildPlaceholders(UserProfile profile, JobDescription jobDescription) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("fullName", profile.getFullName());
        placeholders.put("email", profile.getEmail());
        placeholders.put("phone", profile.getPhone());
        placeholders.put("summary", profile.getSummary());
        placeholders.put("location", profile.getLocation());
        placeholders.put("jobTitle", jobDescription.getTitle());
        placeholders.put("jobCompany", jobDescription.getCompany());
        placeholders.put("jobContent", jobDescription.getContent());
        placeholders.put("experiences", profile.getExperiences().stream()
                .map(this::experienceBlock)
                .reduce("", (a, b) -> a + "\n" + b));
        placeholders.put("skills", profile.getSkills().stream()
                .map(skill -> "\\item " + nullSafe(skill.getName()) + skillsProficiency(skill.getProficiency()))
                .reduce("", (a, b) -> a + "\n" + b));
        placeholders.put("education", profile.getEducation().stream()
                .map(ed -> "\\item \\textbf{" + nullSafe(ed.getDegree()) + "} in " + nullSafe(ed.getFieldOfStudy())
                        + " at " + nullSafe(ed.getSchool()) + " (" + nullSafe(ed.getStartDate()) + " - " + nullSafe(ed.getEndDate()) + ")")
                .reduce("", (a, b) -> a + "\n" + b));
        return placeholders;
    }

    private String experienceBlock(com.resumebuilder.backend.domain.Experience exp) {
        String dates = "";
        if (exp.getStartDate() != null || exp.getEndDate() != null) {
            dates = "(" + nullSafe(exp.getStartDate()) + " - " + nullSafe(exp.getEndDate()) + ")";
        }
        String line = "\\item \\textbf{" + nullSafe(exp.getTitle()) + "} at " + nullSafe(exp.getCompany()) + " "
                + nullSafe(exp.getLocation()) + " " + dates;
        if (exp.getDescription() != null && !exp.getDescription().isBlank()) {
            line += "\\\\ " + sanitizeLatex(exp.getDescription());
        }
        return line;
    }

    private String skillsProficiency(String prof) {
        if (prof == null || prof.isBlank()) {
            return "";
        }
        return " (" + prof + ")";
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String sanitizeLatex(String text) {
        return text.replaceAll("([#%&_{}$])", "\\\\$1");
    }
}
