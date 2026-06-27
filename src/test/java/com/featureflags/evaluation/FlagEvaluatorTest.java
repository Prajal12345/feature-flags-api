package com.featureflags.evaluation;

import com.featureflags.domain.ComparisonOperator;
import com.featureflags.domain.FlagRule;
import com.featureflags.domain.RuleCondition;
import com.featureflags.model.FeatureFlag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FlagEvaluatorTest {

    private FlagEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new FlagEvaluator();
    }

    @Test
    void returnsDefaultStateWhenNoRulesMatch() {
        FeatureFlag flag = new FeatureFlag("beta-ui", false, List.of(), null);

        FlagEvaluator.EvaluationResult result = evaluator.evaluate(flag, Map.of("region", "APAC"));

        assertThat(result.enabled()).isFalse();
        assertThat(result.reason()).isEqualTo("Default state");
    }

    @Test
    void matchesRuleBySubscriptionTier() {
        FeatureFlag flag = new FeatureFlag("premium-feature", false, List.of(
                new FlagRule(1, List.of(
                        new RuleCondition("subscriptionTier", ComparisonOperator.EQUALS, "premium")
                ), true)
        ), null);

        FlagEvaluator.EvaluationResult enabled = evaluator.evaluate(flag, Map.of("subscriptionTier", "premium"));
        FlagEvaluator.EvaluationResult disabled = evaluator.evaluate(flag, Map.of("subscriptionTier", "free"));

        assertThat(enabled.enabled()).isTrue();
        assertThat(enabled.reason()).contains("priority 1");
        assertThat(disabled.enabled()).isFalse();
    }

    @Test
    void evaluatesRulesInPriorityOrder() {
        FeatureFlag flag = new FeatureFlag("regional", false, List.of(
                new FlagRule(2, List.of(
                        new RuleCondition("region", ComparisonOperator.EQUALS, "US")
                ), true),
                new FlagRule(1, List.of(
                        new RuleCondition("subscriptionTier", ComparisonOperator.EQUALS, "enterprise")
                ), false)
        ), null);

        FlagEvaluator.EvaluationResult result = evaluator.evaluate(flag, Map.of(
                "region", "US",
                "subscriptionTier", "enterprise"
        ));

        assertThat(result.enabled()).isFalse();
        assertThat(result.reason()).contains("priority 1");
    }

    @Test
    void supportsInOperatorForRegionList() {
        FeatureFlag flag = new FeatureFlag("eu-rollout", false, List.of(
                new FlagRule(1, List.of(
                        new RuleCondition("region", ComparisonOperator.IN, List.of("EU", "UK"))
                ), true)
        ), null);

        assertThat(evaluator.evaluate(flag, Map.of("region", "EU")).enabled()).isTrue();
        assertThat(evaluator.evaluate(flag, Map.of("region", "US")).enabled()).isFalse();
    }

    @Test
    void appliesDeterministicPercentageRollout() {
        FeatureFlag flag = new FeatureFlag("gradual", false, List.of(), 50);

        Map<String, Object> context = Map.of("userId", "user-123");
        FlagEvaluator.EvaluationResult first = evaluator.evaluate(flag, context);
        FlagEvaluator.EvaluationResult second = evaluator.evaluate(flag, context);

        assertThat(first.enabled()).isEqualTo(second.enabled());
        assertThat(first.reason()).contains("Percentage rollout");
    }

    @Test
    void percentageRolloutIsStableAcrossEvaluations() {
        FeatureFlag flag = new FeatureFlag("stable-rollout", false, List.of(), 30);
        Map<String, Object> context = Map.of("userId", "abc-999");

        boolean first = evaluator.evaluate(flag, context).enabled();
        for (int i = 0; i < 100; i++) {
            assertThat(evaluator.evaluate(flag, context).enabled()).isEqualTo(first);
        }
    }

    @Test
    void differentUsersMayReceiveDifferentRolloutOutcomes() {
        FeatureFlag flag = new FeatureFlag("mixed-rollout", false, List.of(), 50);

        boolean userA = evaluator.evaluate(flag, Map.of("userId", "user-a")).enabled();
        boolean userB = evaluator.evaluate(flag, Map.of("userId", "user-b")).enabled();

        assertThat(userA || userB).isTrue();
    }

    @Test
    void rulesTakePrecedenceOverPercentageRollout() {
        FeatureFlag flag = new FeatureFlag("rule-first", false, List.of(
                new FlagRule(1, List.of(
                        new RuleCondition("subscriptionTier", ComparisonOperator.EQUALS, "premium")
                ), true)
        ), 0);

        FlagEvaluator.EvaluationResult result = evaluator.evaluate(flag, Map.of(
                "subscriptionTier", "premium",
                "userId", "user-1"
        ));

        assertThat(result.enabled()).isTrue();
        assertThat(result.reason()).contains("priority 1");
    }
}
