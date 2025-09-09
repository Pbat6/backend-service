package com.the.service;

import com.the.exception.InvalidDataException;
import com.the.model.RedisToken;
import com.the.repository.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisTokenService {
    private final RedisTokenRepository redisTokenRepository;

    public void save(RedisToken token) {
        redisTokenRepository.save(token);
    }

    public void remove(String id) {
        isExists(id);
        redisTokenRepository.deleteById(id);
    }

    public boolean isExists(String id) {
        if (!redisTokenRepository.existsById(id)) {
            throw new InvalidDataException("Token not exists");
        }
        return true;
    }
}
