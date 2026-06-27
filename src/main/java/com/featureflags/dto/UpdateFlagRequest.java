package com.featureflags.dto;

import com.featureflags.domain.FlagRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateFlagRequest(
        @NotNull Boolean defaultState,
        @Valid List<FlagRule> rules,
        @Min(0) @Max(100) Integer percentageRollout,
        @NotNull Long version
) {
}
