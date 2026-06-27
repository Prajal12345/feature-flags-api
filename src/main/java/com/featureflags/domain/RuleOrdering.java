package com.featureflags.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class RuleOrdering {

    private RuleOrdering() {
    }

    public static List<FlagRule> sortByPriority(List<FlagRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(rules.stream()
                .sorted(Comparator.comparingInt(FlagRule::priority))
                .toList());
    }
}
