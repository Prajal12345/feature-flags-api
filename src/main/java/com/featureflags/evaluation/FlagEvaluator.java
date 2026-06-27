package com.featureflags.evaluation;

import com.featureflags.domain.ComparisonOperator;
import com.featureflags.domain.FlagRule;
import com.featureflags.domain.RuleCondition;
import com.featureflags.model.FeatureFlag;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class FlagEvaluator {

    public EvaluationResult evaluate(FeatureFlag flag, Map<String, Object> context) {
        for (FlagRule rule : flag.getRules()) {
            if (matchesAllConditions(rule.conditions(), context)) {
                return new EvaluationResult(rule.enabled(), "Matched rule priority " + rule.priority());
            }
        }

        Integer rollout = flag.getPercentageRollout();
        if (rollout != null && rollout > 0) {
            String userId = extractUserId(context);
            if (userId != null) {
                boolean inRollout = isInPercentageRollout(userId, flag.getName(), rollout);
                return new EvaluationResult(inRollout, "Percentage rollout " + rollout + "%");
            }
        }

        return new EvaluationResult(flag.isDefaultState(), "Default state");
    }

    boolean matchesAllConditions(List<RuleCondition> conditions, Map<String, Object> context) {
        return conditions.stream().allMatch(condition -> matchesCondition(condition, context));
    }

    boolean matchesCondition(RuleCondition condition, Map<String, Object> context) {
        Object actual = context.get(condition.attribute());
        Object expected = condition.value();

        return switch (condition.operator()) {
            case EQUALS -> Objects.equals(normalize(actual), normalize(expected));
            case NOT_EQUALS -> !Objects.equals(normalize(actual), normalize(expected));
            case IN -> isInCollection(expected, actual);
            case NOT_IN -> !isInCollection(expected, actual);
            case CONTAINS -> actual != null && normalize(actual).contains(normalize(expected));
        };
    }

    boolean isInPercentageRollout(String userId, String flagName, int percentage) {
        if (percentage >= 100) {
            return true;
        }
        if (percentage <= 0) {
            return false;
        }
        int bucket = deterministicBucket(userId, flagName);
        return bucket < percentage;
    }

    int deterministicBucket(String userId, String flagName) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((flagName + ":" + userId).getBytes(StandardCharsets.UTF_8));
            int value = ((hash[0] & 0xFF) << 24)
                    | ((hash[1] & 0xFF) << 16)
                    | ((hash[2] & 0xFF) << 8)
                    | (hash[3] & 0xFF);
            return Math.floorMod(value, 100);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private String extractUserId(Map<String, Object> context) {
        Object userId = context.get("userId");
        if (userId == null) {
            userId = context.get("user_id");
        }
        return userId != null ? userId.toString() : null;
    }

    private boolean isInCollection(Object expected, Object actual) {
        if (expected instanceof Collection<?> collection) {
            return collection.stream().anyMatch(item -> Objects.equals(normalize(item), normalize(actual)));
        }
        if (expected instanceof Object[] array) {
            for (Object item : array) {
                if (Objects.equals(normalize(item), normalize(actual))) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    private String normalize(Object value) {
        return value == null ? "" : value.toString().trim().toLowerCase();
    }

    public record EvaluationResult(boolean enabled, String reason) {
    }
}
