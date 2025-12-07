package com.resumebuilder.backend.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.resumebuilder.backend.config.AppProperties;

@Component
public class LatexRenderer {

    private static final Logger log = LoggerFactory.getLogger(LatexRenderer.class);

    private final AppProperties appProperties;

    public LatexRenderer(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public byte[] render(String templateContent, Map<String, String> placeholders) {
        String filled = applyPlaceholders(templateContent, placeholders);
        Path outputDir = Path.of(appProperties.getLatex().getOutputDir());
        try {
            Files.createDirectories(outputDir);
            Path workingDir = Files.createTempDirectory(outputDir, "resume-");
            Path texFile = workingDir.resolve("resume.tex");
            Files.writeString(texFile, filled, StandardCharsets.UTF_8);

            ProcessBuilder pb = new ProcessBuilder("pdflatex", "-interaction=nonstopmode", texFile.getFileName().toString());
            pb.directory(workingDir.toFile());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                reader.lines().limit(200).forEach(line -> log.debug("pdflatex: {}", line));
            }
            int exit = process.waitFor();
            if (exit != 0) {
                throw new IllegalStateException("pdflatex exited with code " + exit);
            }
            Path pdf = workingDir.resolve("resume.pdf");
            if (!Files.exists(pdf)) {
                throw new IllegalStateException("pdflatex did not produce resume.pdf");
            }
            return Files.readAllBytes(pdf);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            String fallback = minimalPdf("Resume generation failed, fallback placeholder " + UUID.randomUUID());
            log.warn("Falling back to stub PDF: {}", e.getMessage());
            return fallback.getBytes(StandardCharsets.UTF_8);
        }
    }

    private String applyPlaceholders(String template, Map<String, String> placeholders) {
        String result = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue() == null ? "" : entry.getValue());
        }
        return result;
    }

    private String minimalPdf(String message) {
        return """
                %PDF-1.4
                1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj
                2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj
                3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R >> endobj
                4 0 obj << /Length 55 >> stream
                BT /F1 24 Tf 72 700 Td (%s) Tj ET
                endstream endobj
                xref 0 5
                0000000000 65535 f 
                0000000010 00000 n 
                0000000060 00000 n 
                0000000113 00000 n 
                0000000204 00000 n 
                trailer << /Size 5 /Root 1 0 R >>
                startxref
                290
                %%EOF
                """.formatted(message);
    }
}
