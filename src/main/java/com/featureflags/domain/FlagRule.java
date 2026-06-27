package com.featureflags.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FlagRule(
        int priority,
        @NotEmpty @Valid List<RuleCondition> conditions,
        @NotNull Boolean enabled
) {
}
