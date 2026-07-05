/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.securityjwt.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the security-jwt starter, bound from
 * {@code spring.security-jwt.*}.
 *
 * <pre>{@code
 * spring:
 *   security-jwt:
 *     enabled: true
 *     jwk-set-uri: https://issuer.example.com/.well-known/jwks.json
 *     public-paths:
 *       - /actuator/health
 *       - /public/**
 *     cors:
 *       allowed-origins: [ "https://app.example.com" ]
 *       allow-credentials: true
 * }</pre>
 *
 * @author SaumilP
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "spring.security-jwt")
public class SecurityJwtProperties {

    /** Whether the opinionated security filter chain is registered. */
    private boolean enabled = true;

    /** JWKS endpoint used to validate JWT signatures. Takes precedence over {@code issuerUri}. */
    private String jwkSetUri;

    /** OIDC issuer location; the JWKS and validation rules are discovered from it. */
    private String issuerUri;

    /** Ant-style paths that bypass authentication (permit-all). */
    private final List<String> publicPaths = new ArrayList<>();

    /** CORS settings. */
    private final Cors cors = new Cors();

    /** Secure-header settings. */
    private final Headers headers = new Headers();

    /** Creates an instance with default values. */
    public SecurityJwtProperties() {
    }

    /** {@return whether the starter is enabled} */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the starter is enabled.
     *
     * @param enabled {@code false} to disable the opinionated filter chain
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** {@return the JWKS URI, or {@code null} if unset} */
    public String getJwkSetUri() {
        return jwkSetUri;
    }

    /**
     * Sets the JWKS URI.
     *
     * @param jwkSetUri the JWKS endpoint URL
     */
    public void setJwkSetUri(String jwkSetUri) {
        this.jwkSetUri = jwkSetUri;
    }

    /** {@return the issuer URI, or {@code null} if unset} */
    public String getIssuerUri() {
        return issuerUri;
    }

    /**
     * Sets the issuer URI.
     *
     * @param issuerUri the OIDC issuer location
     */
    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    /** {@return the mutable list of permit-all paths} */
    public List<String> getPublicPaths() {
        return publicPaths;
    }

    /** {@return the CORS settings} */
    public Cors getCors() {
        return cors;
    }

    /** {@return the secure-header settings} */
    public Headers getHeaders() {
        return headers;
    }

    /**
     * CORS settings applied to the filter chain. CORS is only wired when at least one allowed
     * origin (or origin pattern) is configured.
     *
     * @author SaumilP
     * @since 1.0.0
     */
    public static class Cors {

        /** Exact allowed origins (e.g. {@code https://app.example.com}). */
        private final List<String> allowedOrigins = new ArrayList<>();

        /** Allowed origin patterns (e.g. {@code https://*.example.com}). */
        private final List<String> allowedOriginPatterns = new ArrayList<>();

        /** Allowed HTTP methods. */
        private final List<String> allowedMethods =
            new ArrayList<>(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        /** Allowed request headers. */
        private final List<String> allowedHeaders = new ArrayList<>(List.of("*"));

        /** Whether credentials (cookies, authorization headers) are allowed. */
        private boolean allowCredentials = false;

        /** Pre-flight cache duration in seconds. */
        private long maxAgeSeconds = 3600;

        /** Creates an instance with default values. */
        public Cors() {
        }

        /** {@return the mutable list of allowed origins} */
        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        /** {@return the mutable list of allowed origin patterns} */
        public List<String> getAllowedOriginPatterns() {
            return allowedOriginPatterns;
        }

        /** {@return the mutable list of allowed methods} */
        public List<String> getAllowedMethods() {
            return allowedMethods;
        }

        /** {@return the mutable list of allowed headers} */
        public List<String> getAllowedHeaders() {
            return allowedHeaders;
        }

        /** {@return whether credentials are allowed} */
        public boolean isAllowCredentials() {
            return allowCredentials;
        }

        /**
         * Sets whether credentials are allowed.
         *
         * @param allowCredentials {@code true} to allow credentials
         */
        public void setAllowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }

        /** {@return the pre-flight cache duration in seconds} */
        public long getMaxAgeSeconds() {
            return maxAgeSeconds;
        }

        /**
         * Sets the pre-flight cache duration in seconds.
         *
         * @param maxAgeSeconds the max-age in seconds
         */
        public void setMaxAgeSeconds(long maxAgeSeconds) {
            this.maxAgeSeconds = maxAgeSeconds;
        }

        /**
         * Reports whether any CORS origin is configured.
         *
         * @return {@code true} if an origin or origin pattern is present
         */
        public boolean isConfigured() {
            return !allowedOrigins.isEmpty() || !allowedOriginPatterns.isEmpty();
        }
    }

    /**
     * Secure-header settings applied to every response.
     *
     * @author SaumilP
     * @since 1.0.0
     */
    public static class Headers {

        /** Whether to emit HTTP Strict-Transport-Security. */
        private boolean hsts = true;

        /** {@code max-age} for the HSTS header, in seconds. */
        private long hstsMaxAgeSeconds = 31_536_000L;

        /** Whether to include subdomains in the HSTS policy. */
        private boolean hstsIncludeSubdomains = true;

        /** {@code X-Frame-Options} policy: {@code DENY}, {@code SAMEORIGIN}, or {@code DISABLE}. */
        private String frameOptions = "DENY";

        /** Whether to emit {@code X-Content-Type-Options: nosniff}. */
        private boolean contentTypeOptions = true;

        /** Creates an instance with default values. */
        public Headers() {
        }

        /** {@return whether HSTS is emitted} */
        public boolean isHsts() {
            return hsts;
        }

        /**
         * Sets whether HSTS is emitted.
         *
         * @param hsts {@code false} to omit the HSTS header
         */
        public void setHsts(boolean hsts) {
            this.hsts = hsts;
        }

        /** {@return the HSTS {@code max-age} in seconds} */
        public long getHstsMaxAgeSeconds() {
            return hstsMaxAgeSeconds;
        }

        /**
         * Sets the HSTS {@code max-age}.
         *
         * @param hstsMaxAgeSeconds the max-age in seconds
         */
        public void setHstsMaxAgeSeconds(long hstsMaxAgeSeconds) {
            this.hstsMaxAgeSeconds = hstsMaxAgeSeconds;
        }

        /** {@return whether subdomains are included in the HSTS policy} */
        public boolean isHstsIncludeSubdomains() {
            return hstsIncludeSubdomains;
        }

        /**
         * Sets whether subdomains are included in the HSTS policy.
         *
         * @param hstsIncludeSubdomains {@code true} to include subdomains
         */
        public void setHstsIncludeSubdomains(boolean hstsIncludeSubdomains) {
            this.hstsIncludeSubdomains = hstsIncludeSubdomains;
        }

        /** {@return the {@code X-Frame-Options} policy} */
        public String getFrameOptions() {
            return frameOptions;
        }

        /**
         * Sets the {@code X-Frame-Options} policy.
         *
         * @param frameOptions {@code DENY}, {@code SAMEORIGIN}, or {@code DISABLE}
         */
        public void setFrameOptions(String frameOptions) {
            this.frameOptions = frameOptions;
        }

        /** {@return whether {@code X-Content-Type-Options: nosniff} is emitted} */
        public boolean isContentTypeOptions() {
            return contentTypeOptions;
        }

        /**
         * Sets whether {@code X-Content-Type-Options: nosniff} is emitted.
         *
         * @param contentTypeOptions {@code false} to omit the header
         */
        public void setContentTypeOptions(boolean contentTypeOptions) {
            this.contentTypeOptions = contentTypeOptions;
        }
    }
}
