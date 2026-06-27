package com.featureflags.service;

import com.featureflags.dto.EvaluationResponse;
import com.featureflags.evaluation.FlagEvaluator;
import com.featureflags.exception.FlagNotFoundException;
import com.featureflags.model.FeatureFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class EvaluationService {

    private static final Logger log = LoggerFactory.getLogger(EvaluationService.class);

    private final FeatureFlagService featureFlagService;
    private final FlagEvaluator flagEvaluator;

    public EvaluationService(FeatureFlagService featureFlagService, FlagEvaluator flagEvaluator) {
        this.featureFlagService = featureFlagService;
        this.flagEvaluator = flagEvaluator;
    }

    public EvaluationResponse evaluate(String flagName, Map<String, Object> context) {
        Optional<FeatureFlag> flag = loadFlagWithFallback(flagName);
        if (flag.isEmpty()) {
            return new EvaluationResponse(flagName, false, "Database unavailable, default fallback OFF");
        }

        FlagEvaluator.EvaluationResult result = flagEvaluator.evaluate(flag.get(), context);
        return new EvaluationResponse(flagName, result.enabled(), result.reason());
    }

    Optional<FeatureFlag> loadFlagWithFallback(String flagName) {
        try {
            return Optional.of(featureFlagService.getFlagEntity(flagName));
        } catch (DataAccessException ex) {
            log.warn("Database unavailable for flag '{}', attempting cache fallback", flagName, ex);
            Optional<FeatureFlag> cached = featureFlagService.getFromCacheOnly(flagName);
            if (cached.isPresent()) {
                log.info("Serving flag '{}' from cache due to database outage", flagName);
            }
            return cached;
        } catch (RuntimeException ex) {
            if (isNotFound(ex)) {
                throw ex;
            }
            log.warn("Unexpected error loading flag '{}', attempting cache fallback", flagName, ex);
            return featureFlagService.getFromCacheOnly(flagName);
        }
    }

    private boolean isNotFound(RuntimeException ex) {
        return ex instanceof FlagNotFoundException;
    }
}
