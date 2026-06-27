package com.featureflags.service;

import com.featureflags.config.CacheConfig;
import com.featureflags.domain.ComparisonOperator;
import com.featureflags.domain.FlagRule;
import com.featureflags.domain.RuleCondition;
import com.featureflags.dto.CreateFlagRequest;
import com.featureflags.dto.UpdateFlagRequest;
import com.featureflags.model.FeatureFlag;
import com.featureflags.repository.FeatureFlagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FeatureFlagServiceCacheTest {

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private FeatureFlagRepository repository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        Cache cache = cacheManager.getCache(CacheConfig.FLAG_CACHE);
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    void cachesFlagAfterCreateAndRead() {
        featureFlagService.createFlag(new CreateFlagRequest("dark-mode", true, List.of(), null));

        FeatureFlag firstRead = featureFlagService.getFlagEntity("dark-mode");
        FeatureFlag cached = cacheManager.getCache(CacheConfig.FLAG_CACHE).get("dark-mode", FeatureFlag.class);

        assertThat(cached).isNotNull();
        assertThat(cached.getName()).isEqualTo(firstRead.getName());
        assertThat(cached.isDefaultState()).isTrue();
    }

    @Test
    void updateRefreshesCacheEntry() {
        var created = featureFlagService.createFlag(new CreateFlagRequest("checkout-v2", false, List.of(), null));
        featureFlagService.updateFlag("checkout-v2", new UpdateFlagRequest(true, List.of(
                new FlagRule(1, List.of(
                        new RuleCondition("region", ComparisonOperator.EQUALS, "US")
                ), true)
        ), 10, created.version()));

        FeatureFlag cached = cacheManager.getCache(CacheConfig.FLAG_CACHE).get("checkout-v2", FeatureFlag.class);

        assertThat(cached.isDefaultState()).isTrue();
        assertThat(cached.getRules()).hasSize(1);
        assertThat(cached.getPercentageRollout()).isEqualTo(10);
    }

    @Test
    void deleteEvictsCacheEntry() {
        featureFlagService.createFlag(new CreateFlagRequest("temp-flag", false, List.of(), null));
        featureFlagService.getFlagEntity("temp-flag");

        featureFlagService.deleteFlag("temp-flag");

        FeatureFlag cached = cacheManager.getCache(CacheConfig.FLAG_CACHE).get("temp-flag", FeatureFlag.class);
        assertThat(cached).isNull();
    }

    @Test
    void explicitEvictionRemovesCachedFlag() {
        featureFlagService.createFlag(new CreateFlagRequest("cache-me", true, List.of(), null));
        featureFlagService.getFlagEntity("cache-me");

        featureFlagService.evictFromCache("cache-me");

        FeatureFlag cached = cacheManager.getCache(CacheConfig.FLAG_CACHE).get("cache-me", FeatureFlag.class);
        assertThat(cached).isNull();
    }
}
