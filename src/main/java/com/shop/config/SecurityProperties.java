package com.shop.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Security settings bound from the {@code app.security.*} section of application.yml.
 */
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(List<String> publicPaths) {

    public SecurityProperties {
        if (publicPaths == null) {
            publicPaths = List.of();
        }
    }
}
