package com.resumebuilder.backend.web;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.resumebuilder.backend.service.AtsService;
import com.resumebuilder.backend.web.dto.AtsScoreResponse;

@RestController
@RequestMapping("/api/ats")
public class AtsController {

    private final AtsService atsService;

    public AtsController(AtsService atsService) {
        this.atsService = atsService;
    }

    @GetMapping("/score/profile/{profileId}/jd/{jdId}")
    public ResponseEntity<AtsScoreResponse> score(@PathVariable Long profileId, @PathVariable Long jdId,
            @org.springframework.web.bind.annotation.RequestParam(value = "resumeId", required = false) Long resumeId) {
        var score = atsService.score(profileId, jdId, resumeId);
        AtsScoreResponse response = new AtsScoreResponse();
        response.setScore(score.getScore());
        response.setMissingKeywords(score.getMissingKeywords());
        response.setPreview(score.getPreview());
        response.setSource(score.getSource());
        if (score.getSectionCoverage() != null) {
            var sc = new AtsScoreResponse.SectionCoverage();
            sc.setSummary(score.getSectionCoverage().getSummary());
            sc.setExperience(score.getSectionCoverage().getExperience());
            sc.setSkills(score.getSectionCoverage().getSkills());
            sc.setEducation(score.getSectionCoverage().getEducation());
            response.setSectionCoverage(sc);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/gap/profile/{profileId}/jd/{jdId}")
    public ResponseEntity<Set<String>> gap(@PathVariable Long profileId, @PathVariable Long jdId,
            @org.springframework.web.bind.annotation.RequestParam(value = "resumeId", required = false) Long resumeId) {
        return ResponseEntity.ok(atsService.gap(profileId, jdId, resumeId));
    }

    @GetMapping("/preview/profile/{profileId}")
    public ResponseEntity<String> preview(@PathVariable Long profileId,
            @org.springframework.web.bind.annotation.RequestParam(value = "resumeId", required = false) Long resumeId) {
        return ResponseEntity.ok(atsService.preview(profileId, resumeId));
    }
}
