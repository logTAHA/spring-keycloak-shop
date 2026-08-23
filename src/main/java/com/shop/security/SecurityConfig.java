package com.shop.security;

import tools.jackson.databind.ObjectMapper;
import com.shop.config.SecurityProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http, SecurityProperties properties,
            AuthenticationEntryPoint authenticationEntryPoint,
            AccessDeniedHandler accessDeniedHandler
    )
            throws Exception {
        return http
                .csrf(CsrfConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    if (!properties.publicPaths().isEmpty()) {
                        auth.requestMatchers(properties.publicPaths().toArray(String[]::new)).permitAll();
                    }
                    auth.anyRequest().authenticated();
                })
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                ).oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakRoleConverter()))
                ).build();
    }

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint(ProblemDetailWriter problemDetailWriter) {
        return new RestAuthenticationEntryPoint(problemDetailWriter);
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler(ProblemDetailWriter problemDetailWriter) {
        return new RestAccessDeniedHandler(problemDetailWriter);
    }

    @Bean
    ProblemDetailWriter problemDetailWriter(ObjectMapper objectMapper) {
        return new ProblemDetailWriter(objectMapper);
    }
}
