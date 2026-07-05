/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.securityjwt.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Auto-configuration for the security-jwt starter.
 *
 * <p>Registers an opinionated, stateless {@link SecurityFilterChain} for a JWT resource server:
 * bearer-token authentication, secure response headers (HSTS, {@code X-Content-Type-Options},
 * {@code X-Frame-Options}), CORS from {@code spring.security-jwt.cors.*}, CSRF disabled, and every
 * request authenticated except the configured public paths. A {@link JwtDecoder} is built from the
 * configured JWKS or issuer URI when present.
 *
 * <p>All beans are {@link ConditionalOnMissingBean} so an application can supply its own chain or
 * decoder to override the defaults entirely.
 *
 * @author SaumilP
 * @since 1.0.0
 */
@AutoConfiguration(beforeName = {
    "org.springframework.boot.security.autoconfigure.servlet.SecurityAutoConfiguration",
    "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(SecurityJwtProperties.class)
@ConditionalOnProperty(prefix = "spring.security-jwt", name = "enabled",
    havingValue = "true", matchIfMissing = true)
public class SecurityJwtAutoConfiguration {

    /** Creates the security-jwt auto-configuration. */
    public SecurityJwtAutoConfiguration() {
    }

    /**
     * Builds a {@link JwtDecoder} from the configured JWKS or issuer URI.
     *
     * @param properties the starter properties; must not be {@code null}
     * @return a decoder, or {@code null} when neither URI is configured
     */
    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    public JwtDecoder jwtDecoder(SecurityJwtProperties properties) {
        if (StringUtils.hasText(properties.getJwkSetUri())) {
            return NimbusJwtDecoder.withJwkSetUri(properties.getJwkSetUri()).build();
        }
        if (StringUtils.hasText(properties.getIssuerUri())) {
            return JwtDecoders.fromIssuerLocation(properties.getIssuerUri());
        }
        return null;
    }

    /**
     * Builds the CORS configuration source when at least one allowed origin is configured.
     *
     * @param properties the starter properties; must not be {@code null}
     * @return a {@link CorsConfigurationSource}, or {@code null} when CORS is not configured
     */
    @Bean
    @ConditionalOnMissingBean(CorsConfigurationSource.class)
    public CorsConfigurationSource securityJwtCorsConfigurationSource(SecurityJwtProperties properties) {
        SecurityJwtProperties.Cors cors = properties.getCors();
        if (!cors.isConfigured()) {
            return null;
        }
        CorsConfiguration config = new CorsConfiguration();
        cors.getAllowedOrigins().forEach(config::addAllowedOrigin);
        cors.getAllowedOriginPatterns().forEach(config::addAllowedOriginPattern);
        cors.getAllowedMethods().forEach(config::addAllowedMethod);
        cors.getAllowedHeaders().forEach(config::addAllowedHeader);
        config.setAllowCredentials(cors.isAllowCredentials());
        config.setMaxAge(cors.getMaxAgeSeconds());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Builds the opinionated JWT resource-server filter chain.
     *
     * @param http            the HTTP security builder; must not be {@code null}
     * @param properties      the starter properties; must not be {@code null}
     * @param corsSource      the optional CORS source
     * @param decoderProvider the optional JWT decoder
     * @return the built {@link SecurityFilterChain}; never {@code null}
     * @throws Exception if the chain cannot be built
     */
    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain securityJwtFilterChain(
            HttpSecurity http,
            SecurityJwtProperties properties,
            ObjectProvider<CorsConfigurationSource> corsSource,
            ObjectProvider<JwtDecoder> decoderProvider) throws Exception {

        http.csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                String[] publicPaths = properties.getPublicPaths().toArray(String[]::new);
                if (publicPaths.length > 0) {
                    auth.requestMatchers(publicPaths).permitAll();
                }
                auth.anyRequest().authenticated();
            });

        CorsConfigurationSource cors = corsSource.getIfAvailable();
        if (cors != null) {
            http.cors(c -> c.configurationSource(cors));
        }

        JwtDecoder decoder = decoderProvider.getIfAvailable();
        if (decoder != null) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(decoder)));
        }

        SecurityJwtProperties.Headers headers = properties.getHeaders();
        http.headers(h -> {
            if (!headers.isContentTypeOptions()) {
                h.contentTypeOptions(cto -> cto.disable());
            }
            switch (headers.getFrameOptions().toUpperCase()) {
                case "SAMEORIGIN" -> h.frameOptions(f -> f.sameOrigin());
                case "DISABLE" -> h.frameOptions(f -> f.disable());
                default -> h.frameOptions(f -> f.deny());
            }
            if (headers.isHsts()) {
                h.httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(headers.getHstsMaxAgeSeconds())
                    .includeSubDomains(headers.isHstsIncludeSubdomains()));
            } else {
                h.httpStrictTransportSecurity(hsts -> hsts.disable());
            }
        });

        return http.build();
    }
}
