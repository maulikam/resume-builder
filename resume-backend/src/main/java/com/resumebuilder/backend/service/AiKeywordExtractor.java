package com.resumebuilder.backend.service;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ai.keyword-extraction", havingValue = "ai")
public class AiKeywordExtractor implements KeywordExtractor {

    private static final Logger log = LoggerFactory.getLogger(AiKeywordExtractor.class);

    @Autowired(required = false)
    private ChatModel chatModel;

    @Override
    public Set<String> extract(String content) {
        if (chatModel == null) {
            log.warn("AI keyword extraction enabled, but ChatModel not available; returning empty set");
            return new LinkedHashSet<>();
        }
        String template = """
                Extract up to 15 important keywords from the following job description.
                Return a comma-separated list of keywords only.
                Job Description:
                {jd}
                """;
        Prompt prompt = new PromptTemplate(template).create(Map.of("jd", content));
        String response = chatModel.call(prompt).getResult().getOutput().getText();
        LinkedHashSet<String> keywords = new LinkedHashSet<>();
        if (response != null) {
            for (String token : response.split(",")) {
                String t = token.trim();
                if (!t.isEmpty()) {
                    keywords.add(t);
                }
            }
        }
        return keywords;
    }
}
