package com.gamzabat.algohub.common.redis;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {
	private final RedisTemplate<String, Object> redisTemplate;

	public void setValues(String key, String value) {
		redisTemplate.opsForValue().set(key, value);
	}

	public void setValues(String key, String value, Duration duration) {
		redisTemplate.opsForValue().set(key, value, duration);
	}

	public String getValues(String key) {
		ValueOperations<String, Object> values = redisTemplate.opsForValue();
		return Optional.ofNullable(values.get((key)))
			.map(Object::toString)
			.orElse(null);
	}

	public void deleteValues(String key) {
		redisTemplate.delete(key);
	}

	public boolean checkExistsValue(String key) {
		return redisTemplate.hasKey(key);
	}
}
