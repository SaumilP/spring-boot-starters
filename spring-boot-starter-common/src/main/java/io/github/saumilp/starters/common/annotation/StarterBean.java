/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation documenting that the annotated Spring bean originates from a starter
 * auto-configuration, rather than being defined by the consuming application.
 *
 * <p>This annotation carries no runtime behaviour — it is purely informational. Its purpose
 * is to make auto-configured beans visually distinct in code, aiding developers who need to
 * understand which beans are provided by the starter vs. which they have defined themselves.
 *
 * <p>Consuming applications may override any {@code @StarterBean} by declaring their own
 * bean of the same type, as all starter beans are annotated with
 * {@link org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean}.
 *
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StarterBean {
}
