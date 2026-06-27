package com.featureflags.model;

import com.featureflags.domain.FlagRule;
import com.featureflags.domain.RuleOrdering;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "feature_flags")
public class FeatureFlag {

    @Id
    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private boolean defaultState;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json", nullable = false)
    private List<FlagRule> rules = new ArrayList<>();

    @Column
    private Integer percentageRollout;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    protected FeatureFlag() {
    }

    public FeatureFlag(String name, boolean defaultState, List<FlagRule> rules, Integer percentageRollout) {
        this.name = name;
        this.defaultState = defaultState;
        this.rules = RuleOrdering.sortByPriority(rules);
        this.percentageRollout = percentageRollout;
        this.updatedAt = Instant.now();
    }

    public String getName() {
        return name;
    }

    public boolean isDefaultState() {
        return defaultState;
    }

    public List<FlagRule> getRules() {
        return rules;
    }

    public Integer getPercentageRollout() {
        return percentageRollout;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setDefaultState(boolean defaultState) {
        this.defaultState = defaultState;
    }

    public void setRules(List<FlagRule> rules) {
        this.rules = RuleOrdering.sortByPriority(rules);
    }

    @PostLoad
    void sortRulesAfterLoad() {
        this.rules = RuleOrdering.sortByPriority(this.rules);
    }

    public void setPercentageRollout(Integer percentageRollout) {
        this.percentageRollout = percentageRollout;
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }
}
