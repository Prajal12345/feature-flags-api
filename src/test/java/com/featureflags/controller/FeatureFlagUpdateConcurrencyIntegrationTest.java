package com.featureflags.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.featureflags.dto.CreateFlagRequest;
import com.featureflags.dto.UpdateFlagRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FeatureFlagUpdateConcurrencyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void returnsConflictWhenUpdateUsesStaleVersion() throws Exception {
        CreateFlagRequest createRequest = new CreateFlagRequest("checkout-v2", false, List.of(), null);

        MvcResult createResult = mockMvc.perform(post("/api/v1/flags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.version").value(0))
                .andReturn();

        long version = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("version").asLong();

        UpdateFlagRequest firstUpdate = new UpdateFlagRequest(true, List.of(), 25, version);
        mockMvc.perform(put("/api/v1/flags/checkout-v2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1));

        UpdateFlagRequest staleUpdate = new UpdateFlagRequest(false, List.of(), null, version);
        mockMvc.perform(put("/api/v1/flags/checkout-v2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(staleUpdate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Feature flag 'checkout-v2' was modified by another request; re-fetch and retry"));

        mockMvc.perform(get("/api/v1/flags/checkout-v2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.defaultState").value(true))
                .andExpect(jsonPath("$.percentageRollout").value(25))
                .andExpect(jsonPath("$.version").value(1));
    }
}
