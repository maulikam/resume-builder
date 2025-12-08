package com.resumebuilder.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.langchain4j.model.chat.ChatLanguageModel;

@Service
public class AiSuggestionService {

    private static final Logger log = LoggerFactory.getLogger(AiSuggestionService.class);

    @Autowired(required = false)
    private ChatModel chatModel;

    @Autowired(required = false)
    private ChatLanguageModel langchainModel;

    public String summarizeProfile(String profileText) {
        return callModel("Summarize the candidate profile in 3 sentences.", profileText);
    }

    public String rewriteBullet(String bullet, String jdText) {
        String prompt = "Rewrite this bullet to better match the job description while keeping it concise.";
        return callModel(prompt, "Bullet: " + bullet + "\nJD: " + jdText);
    }

    public String gapFillSuggestions(String missingKeywords, String context) {
        String prompt = "Suggest 3-5 phrases/bullets to include the missing keywords naturally.";
        return callModel(prompt, "Missing keywords: " + missingKeywords + "\nContext: " + context);
    }

    public String suggestBullets(String profileText, String jdText) {
        String template = """
                Given this candidate info:
                {profile}

                And this job description:
                {jd}

                Generate 3-5 concise bullet points tailored to the job. Return plain text bullets separated by newline.
                """;
        Prompt prompt = new PromptTemplate(template).create(
                java.util.Map.of("profile", profileText, "jd", jdText));
        return doGenerate(prompt.toString(), prompt);
    }

    private String callModel(String instruction, String text) {
        String prompt = instruction + "\nContext:\n" + text;
        return doGenerate(prompt, null);
    }

    private String doGenerate(String plainPrompt, Prompt springPrompt) {
        if (langchainModel != null) {
            try {
                return langchainModel.generate(plainPrompt);
            } catch (Exception e) {
                log.warn("LangChain model call failed: {}", e.getMessage());
            }
        }
        if (chatModel != null) {
            Prompt p = springPrompt != null ? springPrompt
                    : new PromptTemplate("{prompt}").create(java.util.Map.of("prompt", plainPrompt));
            return chatModel.call(p).getResult().getOutput().getText();
        }
        log.warn("AI model unavailable; returning fallback text");
        return "AI not configured.";
    }
}
