package com.resumebuilder.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.resumebuilder.backend.domain.ResumeTemplate;
import com.resumebuilder.backend.domain.UserAccount;
import com.resumebuilder.backend.repository.ResumeTemplateRepository;
import com.resumebuilder.backend.repository.UserAccountRepository;

@Configuration
@Profile("dev")
public class DataSeeder {

    @Bean
    CommandLineRunner seedTemplates(ResumeTemplateRepository repository, UserAccountRepository userRepo) {
        return args -> {
            boolean hasDefault = repository.findFirstByDefaultTemplateTrue().isPresent();
            if (!hasDefault) {
                ResumeTemplate template = new ResumeTemplate();
                template.setName("Default Resume");
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

            boolean hasCover = repository.findByName("Default Cover Letter").isPresent();
            if (!hasCover) {
                ResumeTemplate cover = new ResumeTemplate();
                cover.setName("Default Cover Letter");
                cover.setDescription("Minimal cover letter template");
                cover.setLatexContent("""
                        \\documentclass{letter}
                        \\usepackage[margin=1in]{geometry}
                        \\begin{document}
                        \\begin{letter}{Hiring Manager \\\\ {{jobCompany}}}
                        \\opening{Dear Hiring Manager,}
                        I am excited to apply for the {{jobTitle}} role at {{jobCompany}}. {{summary}}
                        \\closing{Sincerely,\\\\{{fullName}}\\\\{{email}}\\\\{{phone}}}
                        \\end{letter}
                        \\end{document}
                        """);
                repository.save(cover);
            }

            if (userRepo.count() == 0) {
                UserAccount admin = new UserAccount();
                admin.setUsername("admin");
                admin.setPassword("{noop}password");
                admin.setEnabled(true);
                admin.setRoles(new java.util.HashSet<>(java.util.List.of("ADMIN", "USER")));
                userRepo.save(admin);
            }
        };
    }
}
