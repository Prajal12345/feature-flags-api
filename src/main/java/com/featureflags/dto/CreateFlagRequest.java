package com.featureflags.dto;

import com.featureflags.domain.FlagRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateFlagRequest(
        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9._-]{0,127}$", message = "Invalid flag name format")
        String name,

        @NotNull Boolean defaultState,

        @Valid List<FlagRule> rules,

        @Min(0) @Max(100) Integer percentageRollout
) {
}
