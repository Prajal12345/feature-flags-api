package com.featureflags.controller;

import com.featureflags.dto.EvaluationRequest;
import com.featureflags.dto.EvaluationResponse;
import com.featureflags.service.EvaluationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/flags")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping("/{name}/evaluate")
    public EvaluationResponse evaluate(
            @PathVariable String name,
            @Valid @RequestBody EvaluationRequest request) {
        return evaluationService.evaluate(name, request.context());
    }
}
