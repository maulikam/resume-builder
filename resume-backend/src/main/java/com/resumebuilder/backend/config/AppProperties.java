package com.resumebuilder.backend.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Latex latex = new Latex();
    private final Ai ai = new Ai();
    private final Security security = new Security();

    public Latex getLatex() {
        return latex;
    }

    public Ai getAi() {
        return ai;
    }

    public Security getSecurity() {
        return security;
    }

    public static class Latex {
        private String outputDir = "./build/latex";

        public String getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(String outputDir) {
            this.outputDir = outputDir;
        }
    }

    public static class Ai {
        /**
         * keyword-extraction strategy: basic or ai.
         */
        private String keywordExtraction = "basic";

        public String getKeywordExtraction() {
            return keywordExtraction;
        }

        public void setKeywordExtraction(String keywordExtraction) {
            this.keywordExtraction = keywordExtraction;
        }
    }

    public static class Security {
        private boolean enabled = false;
        private String username = "resume";
        private String password = "changeme";
        private List<String> roles = List.of("USER");
        private String token = "";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
