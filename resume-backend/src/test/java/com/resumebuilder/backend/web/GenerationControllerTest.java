package com.resumebuilder.backend.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.resumebuilder.backend.service.ResumeGenerationService;

import static org.mockito.BDDMockito.given;

@WebMvcTest(controllers = GenerationController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("removal")
class GenerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResumeGenerationService resumeGenerationService;

    @Test
    void downloadResumeReturnsPdf() throws Exception {
        byte[] pdf = "%PDF-1.4".getBytes();
        given(resumeGenerationService.download(1L)).willReturn(pdf);

        mockMvc.perform(get("/api/generation/resume/1/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"resume-1.pdf\""))
                .andExpect(content().bytes(pdf));
    }
}
