package org.sandcastle.starters.configs;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.KeyGenerator;

public class RedisKeyGenerator extends CachingConfigurerSupport {

    @Override
    public KeyGenerator keyGenerator() {
        return getKeyGenerator();
    }

    public static KeyGenerator getKeyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append(method.getName());
            for (Object obj : params) {
                sb.append(obj.toString());
            }
            return sb.toString();
        };
    }
}
