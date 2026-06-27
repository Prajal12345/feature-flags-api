package com.featureflags.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RuleCondition(
        @NotBlank String attribute,
        @NotNull ComparisonOperator operator,
        @NotNull Object value
) {
}
