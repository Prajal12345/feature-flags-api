package com.featureflags.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuleOrderingTest {

    @Test
    void sortsRulesByPriorityOnWrite() {
        List<FlagRule> unsorted = List.of(
                new FlagRule(3, List.of(new RuleCondition("region", ComparisonOperator.EQUALS, "US")), true),
                new FlagRule(1, List.of(new RuleCondition("userId", ComparisonOperator.EQUALS, "a")), false),
                new FlagRule(2, List.of(new RuleCondition("subscriptionTier", ComparisonOperator.EQUALS, "premium")), true)
        );

        List<FlagRule> sorted = RuleOrdering.sortByPriority(unsorted);

        assertThat(sorted).extracting(FlagRule::priority).containsExactly(1, 2, 3);
    }
}
