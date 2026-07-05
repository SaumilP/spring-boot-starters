/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.auditlog.config;

import io.github.saumilp.starters.auditlog.actor.ActorResolver;
import io.github.saumilp.starters.auditlog.actor.FallbackActorResolver;
import io.github.saumilp.starters.auditlog.actor.SpringSecurityActorResolver;
import io.github.saumilp.starters.auditlog.aspect.AuditLogAspect;
import io.github.saumilp.starters.auditlog.sink.AuditEventSink;
import io.github.saumilp.starters.auditlog.sink.CompositeAuditEventSink;
import io.github.saumilp.starters.auditlog.sink.JpaAuditEventSink;
import io.github.saumilp.starters.auditlog.sink.LoggingAuditEventSink;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * Spring Boot auto-configuration for the audit-log starter.
 *
 * <p>Registers the following beans when active:
 * <ul>
 *   <li>{@link ActorResolver} — Spring Security-aware when {@code spring-security-core} is
 *       present, falling back to {@link FallbackActorResolver}</li>
 *   <li>{@link LoggingAuditEventSink} — always active unless disabled via properties</li>
 *   <li>{@link JpaAuditEventSink} — active when JPA is on the classpath and
 *       {@code spring.audit-log.jpa-sink.enabled=true}</li>
 *   <li>{@link CompositeAuditEventSink} — collects all {@link AuditEventSink} beans</li>
 *   <li>{@link AuditLogAspect} — the AOP interceptor</li>
 * </ul>
 *
 * <p>All beans are annotated with {@link ConditionalOnMissingBean} so consuming applications
 * can replace any individual component by declaring their own bean of the same type.
 *
 * <p>The entire configuration can be disabled via:
 * <pre>{@code spring.audit-log.enabled=false}</pre>
 *
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "spring.audit-log", name = "enabled",
    havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AuditLogProperties.class)
public class AuditLogAutoConfiguration {

    /** Creates the audit-log auto-configuration. */
    public AuditLogAutoConfiguration() {
    }

    /**
     * Registers the Spring Security-aware {@link ActorResolver} when Spring Security is present.
     *
     * @return a {@link SpringSecurityActorResolver}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(ActorResolver.class)
    @ConditionalOnClass(name = "org.springframework.security.core.context.SecurityContextHolder")
    public ActorResolver springSecurityActorResolver() {
        return new SpringSecurityActorResolver();
    }

    /**
     * Registers the fallback {@link ActorResolver} when Spring Security is not on the classpath.
     *
     * @return a {@link FallbackActorResolver}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(ActorResolver.class)
    public ActorResolver fallbackActorResolver() {
        return new FallbackActorResolver();
    }

    /**
     * Registers the SLF4J {@link LoggingAuditEventSink}.
     *
     * @param props the starter configuration; must not be {@code null}
     * @return a {@link LoggingAuditEventSink}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(LoggingAuditEventSink.class)
    @ConditionalOnProperty(prefix = "spring.audit-log.logging-sink", name = "enabled",
        havingValue = "true", matchIfMissing = true)
    public LoggingAuditEventSink loggingAuditEventSink(AuditLogProperties props) {
        return new LoggingAuditEventSink();
    }

    /**
     * Registers the JPA {@link JpaAuditEventSink} when JPA is present and the sink is enabled.
     *
     * @param repository the JPA repository for persisting audit events; must not be {@code null}
     * @return a {@link JpaAuditEventSink}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(JpaAuditEventSink.class)
    @ConditionalOnClass(name = "org.springframework.data.jpa.repository.JpaRepository")
    @ConditionalOnProperty(prefix = "spring.audit-log.jpa-sink", name = "enabled",
        havingValue = "true")
    public JpaAuditEventSink jpaAuditEventSink(JpaAuditEventSink.AuditEventRepository repository) {
        return new JpaAuditEventSink(repository);
    }

    /**
     * Registers the {@link CompositeAuditEventSink} that delegates to all discovered sinks.
     *
     * @param sinks all {@link AuditEventSink} beans in the application context
     * @return a {@link CompositeAuditEventSink}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(CompositeAuditEventSink.class)
    public CompositeAuditEventSink compositeAuditEventSink(List<AuditEventSink> sinks) {
        return new CompositeAuditEventSink(sinks);
    }

    /**
     * Registers the {@link AuditLogAspect} AOP interceptor.
     *
     * @param compositeSink the composite sink; must not be {@code null}
     * @param actorResolver the actor resolver; must not be {@code null}
     * @return a configured {@link AuditLogAspect}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(AuditLogAspect.class)
    public AuditLogAspect auditLogAspect(CompositeAuditEventSink compositeSink,
                                          ActorResolver actorResolver) {
        return new AuditLogAspect(compositeSink, actorResolver);
    }
}
