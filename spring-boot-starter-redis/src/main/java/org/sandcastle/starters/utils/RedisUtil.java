package org.sandcastle.starters.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationUtils;
import org.springframework.util.Assert;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("all")
public class RedisUtil {
    private final Logger log = LoggerFactory.getLogger(RedisUtil.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisLockUtil redisLockUtil;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate, RedisLockUtil redisLockUtil) {
        this.redisTemplate = redisTemplate;
        this.redisLockUtil = redisLockUtil;
    }

    public void expire(String key, Long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
        }
    }

    public <T> void setExpire(final String key, final T value, final long time, final TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, time, timeUnit);
    }

    public <T> void setExpire(final String key, final T value, final long time, final TimeUnit timeUnit, RedisSerializer<T> valueSerializer) {
        byte[] rawKey = rawKey(key);
        byte[] rawValue = rawValue(value, valueSerializer);

        redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                potentiallyUseSetEx(connection);
                return null;
            }

            public void potentiallyUseSetEx(RedisConnection connection) {
                if (!TimeUnit.MILLISECONDS.equals(timeUnit) || !failsafeInvokeSetEx(connection)) {
                    connection.setEx(rawKey, TimeoutUtils.toSeconds(time, timeUnit), rawValue);
                }
            }

            private boolean failsafeInvokeSetEx(RedisConnection connection) {
                boolean failed = false;
                try {
                    connection.pSetEx(rawKey, time, rawValue);
                } catch (UnsupportedOperationException e) {
                    failed = true;
                }
                return !failed;
            }
        }, true);
    }

    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public Boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            return false;
        }
    }

    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(Arrays.asList(key));
            }
        }
    }

    public <T> T get(String key) {
        return key == null ? null : (T) redisTemplate.opsForValue().get(key);
    }

    public <T> T get(final String key, RedisSerializer<Object> valueSerializer) {
        byte[] rawKey = rawKey(key);
        return (T) redisTemplate.execute(connection -> deserializeValue(connection.get(rawKey), valueSerializer), true);
    }

    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
        }
    }

    public Boolean set(String key, Object value, Long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            return false;
        }
    }

    public Boolean set(String key, Object value, Duration timeout) {
        try {
            Assert.notNull(timeout, "Timeout must not be null!");
            if (TimeoutUtils.hasMillis(timeout)) {
                redisTemplate.opsForValue().set(key, value, timeout.toMillis(), TimeUnit.MILLISECONDS);
            } else {
                redisTemplate.opsForValue().set(key, value, timeout.getSeconds(), TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            return false;
        }
    }

    public Long incr(String key, Long delta) {
        if (delta < 0) {
            throw new RuntimeException("The increment factor must be greater than 0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    public Long decr(String key, Long delta) {
        if (delta < 0) {
            throw new RuntimeException("Decreasing factor must be greater than 0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    public <T> T hget(String key, String item) {
        return (T) redisTemplate.opsForHash().get(key, item);
    }

    public <K, V> Map<K, V> hmget(String key) {
        return (Map<K, V>) redisTemplate.opsForHash().entries(key);
    }

    public <K, V> Boolean hmset(String key, Map<K, V> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            return false;
        }
    }

    public <K, V> Boolean hmset(String key, Map<K, V> map, Long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            return false;
        }
    }

    public <T> Boolean hset(String key, String item, T value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            return false;
        }
    }

    public <T> Boolean hset(String key, String item, T value, Long time) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            return false;
        }
    }

    public <T> void hdel(String key, T... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    public Boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    public Double hincr(String key, String item, Double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    public Double hdecr(String key, String item, Double by) {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }

    public <T> Set<T> sGet(String key) {
        try {
            return (Set<T>) redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            return null;
        }
    }

    public Boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            return false;
        }
    }

    public <T> Long sSet(String key, T... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            return 0L;
        }
    }

    public <T> Long sSetAndTime(String key, Long time, T... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0) {
                expire(key, time);
            }
            return count;
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            return 0L;
        }
    }

    public Long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            return 0L;
        }
    }

    public <T> Long setRemove(String key, T... values) {
        try {
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            return 0L;
        }
    }

    public <T> List<T> lGet(String key, Long start, Long end) {
        try {
            return (List<T>) redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            return null;
        }
    }

    public Long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            return 0L;
        }
    }

    public <T> T lGetIndex(String key, Long index) {
        try {
            return (T) redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            return null;
        }
    }

    public <T> Boolean lSet(String key, T value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            return false;
        }
    }

    public <T> Boolean lSet(String key, T value, Long time) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            return false;
        }
    }

    public <T> Boolean lSet(String key, List<T> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            return false;
        }
    }

    public <T> Boolean lSet(String key, List<T> value, Long time) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            return false;
        }
    }

    public <T> Boolean lUpdateIndex(String key, Long index, T value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            return false;
        }
    }

    public <T> Long lRemove(String key, Long count, T value) {
        try {
            return redisTemplate.opsForList().remove(key, count, value);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            return 0L;
        }
    }

    public <T> T getList(String key, int start, int end, RedisSerializer<T> valueSerializer) {
        byte[] rawKey = rawKey(key);
        return (T) redisTemplate.execute(connection -> deserializeValues(connection.lRange(rawKey, start, end), (RedisSerializer<Object>) valueSerializer), true);
    }

    private byte[] rawKey(Object key) {
        Assert.notNull(key, "non null key required");

        if (key instanceof byte[]) {
            return (byte[]) key;
        }
        RedisSerializer<Object> redisSerializer = (RedisSerializer<Object>) redisTemplate.getKeySerializer();
        return redisSerializer.serialize(key);
    }

    private byte[] rawValue(Object value, RedisSerializer valueSerializer) {
        if (value instanceof byte[]) {
            return (byte[]) value;
        }
        return valueSerializer.serialize(value);
    }

    private List<? extends Object> deserializeValues(List<byte[]> rawValues, RedisSerializer<Object> valueSerializer) {
        if (valueSerializer == null) {
            return rawValues;
        }
        return SerializationUtils.deserialize(rawValues, valueSerializer);
    }

    private Object deserializeValue(byte[] value, RedisSerializer<Object> valueSerializer) {
        if (valueSerializer == null) {
            return value;
        }
        return valueSerializer.deserialize(value);
    }

    public boolean tryLock(String key, int expireTime) {
        final JSONObject lock = new JSONObject();
        try {
            lock.put("id", key);
            // startTime
            lock.put("st", System.currentTimeMillis());
            // keepSeconds
            lock.put("ks", expireTime);
        } catch (JSONException e) {
            log.error("Redis distributed lock write exception：{}", e.getMessage());
            return false;
        }
        return redisLockUtil.tryLock(key, "", expireTime);
    }

    public void unLock(String key) {
        redisLockUtil.releaseLock(key, "");
    }
}
