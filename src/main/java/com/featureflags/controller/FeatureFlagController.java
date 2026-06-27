package com.featureflags.controller;

import com.featureflags.dto.CreateFlagRequest;
import com.featureflags.dto.FlagResponse;
import com.featureflags.dto.UpdateFlagRequest;
import com.featureflags.service.FeatureFlagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/flags")
public class FeatureFlagController {

    private final FeatureFlagService featureFlagService;

    public FeatureFlagController(FeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
    }

    @PostMapping
    public ResponseEntity<FlagResponse> createFlag(@Valid @RequestBody CreateFlagRequest request) {
        FlagResponse response = featureFlagService.createFlag(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<FlagResponse> listFlags() {
        return featureFlagService.listFlags();
    }

    @GetMapping("/{name}")
    public FlagResponse getFlag(@PathVariable String name) {
        return featureFlagService.getFlag(name);
    }

    @PutMapping("/{name}")
    public FlagResponse updateFlag(@PathVariable String name, @Valid @RequestBody UpdateFlagRequest request) {
        return featureFlagService.updateFlag(name, request);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteFlag(@PathVariable String name) {
        featureFlagService.deleteFlag(name);
        return ResponseEntity.noContent().build();
    }
}
