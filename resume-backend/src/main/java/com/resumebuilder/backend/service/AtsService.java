package com.resumebuilder.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import com.resumebuilder.backend.domain.JobDescription;
import com.resumebuilder.backend.domain.UserProfile;
import com.resumebuilder.backend.repository.GeneratedResumeRepository;
import com.resumebuilder.backend.repository.JobDescriptionRepository;
import com.resumebuilder.backend.repository.UserProfileRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AtsService {

    private final JobDescriptionRepository jobDescriptionRepository;
    private final UserProfileRepository userProfileRepository;
    private final KeywordExtractor keywordExtractor;
    private final GeneratedResumeRepository generatedResumeRepository;

    public AtsService(JobDescriptionRepository jobDescriptionRepository,
            UserProfileRepository userProfileRepository,
            KeywordExtractor keywordExtractor,
            GeneratedResumeRepository generatedResumeRepository) {
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.userProfileRepository = userProfileRepository;
        this.keywordExtractor = keywordExtractor;
        this.generatedResumeRepository = generatedResumeRepository;
    }

    public AtsScore score(Long profileId, Long jobDescriptionId, Long resumeId) {
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found: " + profileId));
        JobDescription jd = jobDescriptionRepository.findById(jobDescriptionId)
                .orElseThrow(() -> new EntityNotFoundException("Job description not found: " + jobDescriptionId));

        ResumeText resumeText = resolveResumeText(profile, resumeId);
        Set<String> jdKeywords = keywordExtractor.extract(jd.getContent());
        Set<String> resumeTokens = tokenize(resumeText.text());

        Set<String> missing = jdKeywords.stream()
                .filter(k -> !resumeTokens.contains(k.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toCollection(HashSet::new));

        double coverage = jdKeywords.isEmpty() ? 1.0 : (jdKeywords.size() - missing.size()) / (double) jdKeywords.size();

        AtsScore score = new AtsScore();
        score.setPreview(resumeText.text());
        score.setSource(resumeText.source());
        score.setMissingKeywords(missing);
        score.setScore(Math.round(coverage * 1000.0) / 10.0); // percentage with 1 decimal
        score.setSectionCoverage(computeSectionCoverage(profile, jdKeywords));
        return score;
    }

    public Set<String> gap(Long profileId, Long jobDescriptionId, Long resumeId) {
        return score(profileId, jobDescriptionId, resumeId).getMissingKeywords();
    }

    public String preview(Long profileId, Long resumeId) {
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found: " + profileId));
        return resolveResumeText(profile, resumeId).text();
    }

    private String toPlainText(UserProfile profile) {
        StringBuilder sb = new StringBuilder();
        sb.append(profile.getFullName()).append(" ").append(profile.getEmail()).append(" ").append(profile.getSummary()).append("\n");
        profile.getExperiences().forEach(exp -> {
            sb.append(exp.getTitle()).append(" ").append(exp.getCompany()).append(" ")
                    .append(exp.getDescription() == null ? "" : exp.getDescription()).append("\n");
        });
        profile.getSkills().forEach(skill -> sb.append(skill.getName()).append(" ").append(skill.getProficiency()).append("\n"));
        profile.getEducation().forEach(ed -> sb.append(ed.getDegree()).append(" ").append(ed.getFieldOfStudy()).append(" ").append(ed.getSchool()).append("\n"));
        return sb.toString();
    }

    private ResumeText resolveResumeText(UserProfile profile, Long resumeId) {
        if (resumeId != null) {
            return generatedResumeRepository.findById(resumeId)
                    .map(resume -> new ResumeText(readPdfText(resume.getStoragePath()), "pdf"))
                    .orElseGet(() -> new ResumeText(toPlainText(profile), "profile"));
        }
        return generatedResumeRepository.findTop1ByProfileIdOrderByCreatedAtDesc(profile.getId())
                .stream()
                .findFirst()
                .map(resume -> new ResumeText(readPdfText(resume.getStoragePath()), "pdf"))
                .orElseGet(() -> new ResumeText(toPlainText(profile), "profile"));
    }

    private String readPdfText(String path) {
        if (path == null) {
            return "";
        }
        Path pdfPath = Path.of(path);
        if (!Files.exists(pdfPath)) {
            return "";
        }
        try (PDDocument doc = PDDocument.load(pdfPath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        } catch (IOException e) {
            return "";
        }
    }

    private Set<String> tokenize(String text) {
        if (text == null) {
            return Set.of();
        }
        String[] tokens = text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").split("\\s+");
        Set<String> result = new HashSet<>();
        for (String t : tokens) {
            if (!t.isBlank()) {
                result.add(t);
            }
        }
        return result;
    }

    private AtsScore.SectionCoverage computeSectionCoverage(UserProfile profile, Set<String> jdKeywords) {
        AtsScore.SectionCoverage coverage = new AtsScore.SectionCoverage();
        if (jdKeywords.isEmpty()) {
            coverage.setSummary(1.0);
            coverage.setExperience(1.0);
            coverage.setSkills(1.0);
            coverage.setEducation(1.0);
            return coverage;
        }
        coverage.setSummary(sectionCoverage(profile.getSummary(), jdKeywords));
        String expText = profile.getExperiences().stream()
                .map(e -> e.getTitle() + " " + e.getCompany() + " " + e.getDescription())
                .collect(Collectors.joining(" "));
        coverage.setExperience(sectionCoverage(expText, jdKeywords));
        String skillText = profile.getSkills().stream()
                .map(s -> s.getName() + " " + s.getProficiency())
                .collect(Collectors.joining(" "));
        coverage.setSkills(sectionCoverage(skillText, jdKeywords));
        String eduText = profile.getEducation().stream()
                .map(ed -> ed.getDegree() + " " + ed.getFieldOfStudy() + " " + ed.getSchool())
                .collect(Collectors.joining(" "));
        coverage.setEducation(sectionCoverage(eduText, jdKeywords));
        return coverage;
    }

    private double sectionCoverage(String text, Set<String> jdKeywords) {
        Set<String> tokens = tokenize(text);
        long hit = jdKeywords.stream().filter(k -> tokens.contains(k.toLowerCase(Locale.ROOT))).count();
        return jdKeywords.isEmpty() ? 1.0 : (double) hit / jdKeywords.size();
    }

    private record ResumeText(String text, String source) {}
}
