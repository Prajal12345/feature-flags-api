package com.featureflags.dto;

public record EvaluationResponse(
        String flagName,
        boolean enabled,
        String reason
) {
}
