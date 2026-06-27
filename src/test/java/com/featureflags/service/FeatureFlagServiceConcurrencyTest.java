package com.featureflags.service;

import com.featureflags.domain.ComparisonOperator;
import com.featureflags.domain.FlagRule;
import com.featureflags.domain.RuleCondition;
import com.featureflags.dto.CreateFlagRequest;
import com.featureflags.dto.FlagResponse;
import com.featureflags.dto.UpdateFlagRequest;
import com.featureflags.exception.FlagVersionConflictException;
import com.featureflags.repository.FeatureFlagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class FeatureFlagServiceConcurrencyTest {

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private FeatureFlagRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        featureFlagService.evictAllFromCache();
    }

    @Test
    void rejectsUpdateWithStaleVersion() {
        FlagResponse created = featureFlagService.createFlag(
                new CreateFlagRequest("checkout-v2", false, List.of(), null));

        featureFlagService.updateFlag("checkout-v2", new UpdateFlagRequest(
                true,
                List.of(new FlagRule(1, List.of(
                        new RuleCondition("region", ComparisonOperator.EQUALS, "US")
                ), true)),
                10,
                created.version()));

        assertThatThrownBy(() -> featureFlagService.updateFlag("checkout-v2", new UpdateFlagRequest(
                false,
                List.of(),
                null,
                created.version())))
                .isInstanceOf(FlagVersionConflictException.class);

        FlagResponse current = featureFlagService.getFlag("checkout-v2");
        assertThat(current.defaultState()).isTrue();
        assertThat(current.rules()).hasSize(1);
        assertThat(current.percentageRollout()).isEqualTo(10);
    }
}
