package com.featureflags.dto;

import com.featureflags.domain.FlagRule;

import java.time.Instant;
import java.util.List;

public record FlagResponse(
        String name,
        boolean defaultState,
        List<FlagRule> rules,
        Integer percentageRollout,
        Instant updatedAt,
        Long version
) {
}
