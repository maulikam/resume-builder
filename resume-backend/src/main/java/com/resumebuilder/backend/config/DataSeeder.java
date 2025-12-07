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
                template.setLatexContent("""
                        \\documentclass{article}
                        \\usepackage[margin=1in]{geometry}
                        \\begin{document}
                        \\begin{center}
                        {\\LARGE {{fullName}}}\\\\
                        {{email}} \\quad {{phone}}\\\\
                        {{location}}
                        \\end{center}
                        \\vspace{0.5cm}
                        \\textbf{Summary}\\\\
                        {{summary}}
                        \\vspace{0.5cm}
                        \\textbf{Job Target}\\\\
                        {{jobTitle}} at {{jobCompany}}\\\\
                        \\vspace{0.5cm}
                        \\textbf{Job Description}\\\\
                        {{jobContent}}
                        \\vspace{0.5cm}
                        \\textbf{Experience}
                        \\begin{itemize}
                        {{experiences}}
                        \\end{itemize}
                        \\vspace{0.5cm}
                        \\textbf{Skills}
                        \\begin{itemize}
                        {{skills}}
                        \\end{itemize}
                        \\vspace{0.5cm}
                        \\textbf{Education}
                        \\begin{itemize}
                        {{education}}
                        \\end{itemize}
                        \\end{document}
                        """);
                template.setDefaultTemplate(true);
                repository.save(template);
            }
        };
    }
}
