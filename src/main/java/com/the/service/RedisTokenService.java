package com.the.service;

import com.the.exception.InvalidDataException;
import com.the.model.RedisToken;
import com.the.repository.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisTokenService {
    private final RedisTokenRepository redisTokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisToken get(String id) {
        return redisTokenRepository.findById(id)
                .orElseThrow(() -> new InvalidDataException("Token not found"));
    }

    public void save(RedisToken token) {
        redisTokenRepository.save(token);
    }

    public void remove(String id) {
        if(isExists(id)){
            redisTokenRepository.deleteById(id);
        }
    }

    private boolean isExists(String id) {
        if (!redisTokenRepository.existsById(id)) {
            throw new InvalidDataException("Token not exists");
        }
        return true;
    }

    public void blacklistToken(String accessToken, long remainingTimeMillis) {
        if (remainingTimeMillis > 0) {
            redisTemplate.opsForValue().set(accessToken, "blacklisted", remainingTimeMillis, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isTokenBlacklisted(String accessToken) {
        return redisTemplate.hasKey(accessToken);
    }
}
