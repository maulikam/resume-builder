package com.resumebuilder.backend.web;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.resumebuilder.backend.repository.JobDescriptionRepository;
import com.resumebuilder.backend.repository.UserProfileRepository;
import com.resumebuilder.backend.service.AiSuggestionService;
import com.resumebuilder.backend.web.dto.AiGenericRequest;
import com.resumebuilder.backend.web.dto.AiSuggestionRequest;
import com.resumebuilder.backend.web.dto.AiSuggestionResponse;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ai")
@Validated
public class AiController {

    private final AiSuggestionService aiSuggestionService;
    private final UserProfileRepository userProfileRepository;
    private final JobDescriptionRepository jobDescriptionRepository;

    public AiController(AiSuggestionService aiSuggestionService,
            UserProfileRepository userProfileRepository,
            JobDescriptionRepository jobDescriptionRepository) {
        this.aiSuggestionService = aiSuggestionService;
        this.userProfileRepository = userProfileRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
    }

    @PostMapping("/suggest-bullets")
    public ResponseEntity<AiSuggestionResponse> suggest(@Valid @RequestBody AiSuggestionRequest request) {
        var profile = userProfileRepository.findById(request.getProfileId())
                .orElseThrow(() -> new EntityNotFoundException("Profile not found: " + request.getProfileId()));
        var jd = jobDescriptionRepository.findById(request.getJobDescriptionId())
                .orElseThrow(() -> new EntityNotFoundException("Job description not found: " + request.getJobDescriptionId()));
        String profileText = buildProfileText(profile);
        String suggestions = aiSuggestionService.suggestBullets(profileText, jd.getContent());
        AiSuggestionResponse response = new AiSuggestionResponse();
        response.setSuggestions(suggestions);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/summarize-profile")
    public ResponseEntity<AiSuggestionResponse> summarize(@Valid @RequestBody AiGenericRequest request) {
        String output = aiSuggestionService.summarizeProfile(request.getText());
        AiSuggestionResponse resp = new AiSuggestionResponse();
        resp.setSuggestions(output);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/rewrite-bullet")
    public ResponseEntity<AiSuggestionResponse> rewrite(@Valid @RequestBody AiGenericRequest request) {
        String output = aiSuggestionService.rewriteBullet(request.getText(), request.getInstruction());
        AiSuggestionResponse resp = new AiSuggestionResponse();
        resp.setSuggestions(output);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/gap-fill")
    public ResponseEntity<AiSuggestionResponse> gapFill(@Valid @RequestBody AiGenericRequest request) {
        String output = aiSuggestionService.gapFillSuggestions(request.getInstruction(), request.getText());
        AiSuggestionResponse resp = new AiSuggestionResponse();
        resp.setSuggestions(output);
        return ResponseEntity.ok(resp);
    }

    private String buildProfileText(com.resumebuilder.backend.domain.UserProfile profile) {
        StringBuilder sb = new StringBuilder();
        sb.append(profile.getFullName()).append(" ").append(profile.getEmail()).append(" ").append(profile.getSummary()).append("\n");
        profile.getExperiences().forEach(exp -> {
            sb.append(exp.getTitle()).append(" at ").append(exp.getCompany()).append(" ").append(exp.getLocation())
                    .append(" ").append(exp.getDescription() == null ? "" : exp.getDescription()).append("\n");
        });
        profile.getSkills().forEach(skill -> sb.append(skill.getName()).append(" ").append(skill.getProficiency()).append("\n"));
        profile.getEducation().forEach(ed -> sb.append(ed.getDegree()).append(" ").append(ed.getFieldOfStudy()).append(" ").append(ed.getSchool()).append("\n"));
        return sb.toString();
    }
}
