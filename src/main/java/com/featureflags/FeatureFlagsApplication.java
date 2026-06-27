package com.featureflags;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FeatureFlagsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeatureFlagsApplication.class, args);
    }
}
