package com.the.service;

import com.the.util.TokenType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public interface JwtService {

    Date extractExpiration(String token, TokenType type);

    String generateAccessToken(UserDetails userDetails);

    String generateRefreshToken(UserDetails user);

    String generateResetToken(UserDetails user);

    String extractUsername(String token, TokenType type);

    boolean isTokenExpired(String token, TokenType type);
}
