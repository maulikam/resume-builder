package com.resumebuilder.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.resumebuilder.backend.domain.ResumeTemplate;
import com.resumebuilder.backend.repository.ResumeTemplateRepository;

@Configuration
@Profile("dev")
public class DataSeeder {

    @Bean
    CommandLineRunner seedTemplates(ResumeTemplateRepository repository) {
        return args -> {
            boolean hasDefault = repository.findFirstByDefaultTemplateTrue().isPresent();
            if (!hasDefault) {
                ResumeTemplate template = new ResumeTemplate();
                template.setName("Default");
                template.setDescription("Minimal LaTeX resume template placeholder");
                template.setLatexContent("% LaTeX template placeholder");
                template.setDefaultTemplate(true);
                repository.save(template);
            }
        };
    }
}
