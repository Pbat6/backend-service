package com.the.service;

import com.the.util.TokenType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public interface JwtService {
    String extractUserName(String token);

    String generateToken(UserDetails userDetails);

    String generateRefreshToken(UserDetails user);

    String generateResetToken(UserDetails user);

    String extractUsername(String token, TokenType type);

    boolean isValid(String token, TokenType type, UserDetails user);
}
