package org.sandcastle.starters.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@ConfigurationProperties(RedisConfigurationProperties.PREFIX)
@EnableConfigurationProperties(RedisConfigurationProperties.class)
public class RedisConfigurationProperties {
    public static final String PREFIX = "spring.redis";
}
