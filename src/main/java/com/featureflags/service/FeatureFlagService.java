package com.featureflags.service;

import com.featureflags.config.CacheConfig;
import com.featureflags.dto.CreateFlagRequest;
import com.featureflags.dto.FlagResponse;
import com.featureflags.dto.UpdateFlagRequest;
import com.featureflags.exception.FlagAlreadyExistsException;
import com.featureflags.exception.FlagNotFoundException;
import com.featureflags.model.FeatureFlag;
import com.featureflags.repository.FeatureFlagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FeatureFlagService {

    private static final Logger log = LoggerFactory.getLogger(FeatureFlagService.class);

    private final FeatureFlagRepository repository;
    private final CacheManager cacheManager;

    public FeatureFlagService(FeatureFlagRepository repository, CacheManager cacheManager) {
        this.repository = repository;
        this.cacheManager = cacheManager;
    }

    @Transactional
    public FlagResponse createFlag(CreateFlagRequest request) {
        if (repository.existsById(request.name())) {
            throw new FlagAlreadyExistsException(request.name());
        }
        FeatureFlag flag = new FeatureFlag(
                request.name(),
                request.defaultState(),
                request.rules() != null ? request.rules() : List.of(),
                request.percentageRollout()
        );
        FeatureFlag saved = save(flag);
        putInCache(saved);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FlagResponse> listFlags() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FlagResponse getFlag(String name) {
        return toResponse(getFlagEntity(name));
    }

    public FeatureFlag getFlagEntity(String name) {
        Optional<FeatureFlag> cached = getFromCache(name);
        if (cached.isPresent()) {
            return cached.get();
        }
        FeatureFlag flag = findFlagOrThrow(name);
        putInCache(flag);
        return flag;
    }

    public Optional<FeatureFlag> getFromCacheOnly(String name) {
        return getFromCache(name);
    }

    @Transactional
    public FlagResponse updateFlag(String name, UpdateFlagRequest request) {
        FeatureFlag flag = findFlagOrThrow(name);
        flag.setDefaultState(request.defaultState());
        flag.setRules(request.rules() != null ? request.rules() : List.of());
        flag.setPercentageRollout(request.percentageRollout());
        flag.touch();
        FeatureFlag saved = save(flag);
        putInCache(saved);
        return toResponse(saved);
    }

    @Transactional
    public void deleteFlag(String name) {
        if (!repository.existsById(name)) {
            throw new FlagNotFoundException(name);
        }
        repository.deleteById(name);
        evictFromCache(name);
    }

    public void evictFromCache(String name) {
        Cache cache = cacheManager.getCache(CacheConfig.FLAG_CACHE);
        if (cache != null) {
            cache.evict(name);
            log.debug("Evicted flag '{}' from cache", name);
        }
    }

    public void evictAllFromCache() {
        Cache cache = cacheManager.getCache(CacheConfig.FLAG_CACHE);
        if (cache != null) {
            cache.clear();
            log.debug("Evicted all flags from cache");
        }
    }

    FeatureFlag findFlagOrThrow(String name) {
        return repository.findById(name).orElseThrow(() -> new FlagNotFoundException(name));
    }

    FeatureFlag save(FeatureFlag flag) {
        try {
            return repository.save(flag);
        } catch (DataAccessException ex) {
            log.error("Failed to persist feature flag '{}'", flag.getName(), ex);
            throw ex;
        }
    }

    private Optional<FeatureFlag> getFromCache(String name) {
        Cache cache = cacheManager.getCache(CacheConfig.FLAG_CACHE);
        if (cache == null) {
            return Optional.empty();
        }
        FeatureFlag cached = cache.get(name, FeatureFlag.class);
        return Optional.ofNullable(cached);
    }

    private void putInCache(FeatureFlag flag) {
        Cache cache = cacheManager.getCache(CacheConfig.FLAG_CACHE);
        if (cache != null) {
            cache.put(flag.getName(), flag);
        }
    }

    FlagResponse toResponse(FeatureFlag flag) {
        return new FlagResponse(
                flag.getName(),
                flag.isDefaultState(),
                flag.getRules(),
                flag.getPercentageRollout(),
                flag.getUpdatedAt()
        );
    }
}
