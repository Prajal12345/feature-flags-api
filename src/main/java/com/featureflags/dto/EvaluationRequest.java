package com.featureflags.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record EvaluationRequest(
        @NotEmpty(message = "Context must contain at least one attribute")
        @Size(max = 50, message = "Context may contain at most 50 attributes")
        Map<@NotNull String, @NotNull Object> context
) {
}
