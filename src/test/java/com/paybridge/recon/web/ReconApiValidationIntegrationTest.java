package com.paybridge.recon.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReconApiValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rejectsRunRequestWithoutBatchId() throws Exception {
        mockMvc.perform(post("/api/recon/runs")
                .contentType("application/json")
                .content("{}")
                .with(user("operator").roles("OPERATOR"))
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("batchId: must not be null"));
    }

    @Test
    void rejectsBlankCaseNoteBody() throws Exception {
        mockMvc.perform(post("/api/recon/cases/{caseId}/notes", UUID.randomUUID())
                .contentType("application/json")
                .content("{" + "\"body\":\"   \"}")
                .with(user("operator").roles("OPERATOR"))
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("body: must not be blank"));
    }

    @Test
    void rejectsUnreadableStatusRequestBody() throws Exception {
        mockMvc.perform(patch("/api/recon/cases/{caseId}/status", UUID.randomUUID())
                .contentType("application/json")
                .content("{" + "\"status\":\"UNKNOWN\"}")
                .with(user("operator").roles("OPERATOR"))
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Request body could not be parsed."));
    }
}
