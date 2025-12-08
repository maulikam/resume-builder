package com.resumebuilder.backend.web.dto;

import java.util.Set;

public class AtsScoreResponse {
    private double score;
    private Set<String> missingKeywords;
    private String preview;
    private String source;
    private SectionCoverage sectionCoverage;

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Set<String> getMissingKeywords() {
        return missingKeywords;
    }

    public void setMissingKeywords(Set<String> missingKeywords) {
        this.missingKeywords = missingKeywords;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public SectionCoverage getSectionCoverage() {
        return sectionCoverage;
    }

    public void setSectionCoverage(SectionCoverage sectionCoverage) {
        this.sectionCoverage = sectionCoverage;
    }

    public static class SectionCoverage {
        private double summary;
        private double experience;
        private double skills;
        private double education;

        public double getSummary() {
            return summary;
        }

        public void setSummary(double summary) {
            this.summary = summary;
        }

        public double getExperience() {
            return experience;
        }

        public void setExperience(double experience) {
            this.experience = experience;
        }

        public double getSkills() {
            return skills;
        }

        public void setSkills(double skills) {
            this.skills = skills;
        }

        public double getEducation() {
            return education;
        }

        public void setEducation(double education) {
            this.education = education;
        }
    }
}
