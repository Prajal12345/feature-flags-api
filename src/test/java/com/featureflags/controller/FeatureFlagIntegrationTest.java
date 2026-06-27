package com.featureflags.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.featureflags.dto.CreateFlagRequest;
import com.featureflags.dto.EvaluationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FeatureFlagIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAndEvaluateFlag() throws Exception {
        CreateFlagRequest createRequest = new CreateFlagRequest(
                "new-dashboard",
                false,
                List.of(new com.featureflags.domain.FlagRule(
                        1,
                        List.of(new com.featureflags.domain.RuleCondition(
                                "subscriptionTier",
                                com.featureflags.domain.ComparisonOperator.EQUALS,
                                "premium"
                        )),
                        true
                )),
                null
        );

        mockMvc.perform(post("/api/v1/flags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("new-dashboard"));

        EvaluationRequest evalRequest = new EvaluationRequest(Map.of(
                "userId", "user-42",
                "subscriptionTier", "premium",
                "region", "US"
        ));

        mockMvc.perform(post("/api/v1/flags/new-dashboard/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(evalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.reason").value("Matched rule priority 1"));
    }

    @Test
    void rejectsEmptyEvaluationContext() throws Exception {
        mockMvc.perform(post("/api/v1/flags/missing/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"context\": {}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundForMissingFlag() throws Exception {
        mockMvc.perform(get("/api/v1/flags/does-not-exist"))
                .andExpect(status().isNotFound());
    }
}
