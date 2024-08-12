package com.gamzabat.algohub.common.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService{
    private final RedisTemplate<String, Object> redisTemplate;

    public void setValues(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void setValues(String key, String value, Duration duration) {
        redisTemplate.opsForValue().set(key,value,duration);
    }

    public String getValues(String key) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        if(values.get(key) == null) return "";
        return String.valueOf(values.get(key));
    }

    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }
}
